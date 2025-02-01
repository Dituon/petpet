const fs = require("fs")

const width = 282
const height = 282

const seed = 46

const explosionStartIndex = 18
const explosionLength = 12
const scale = 0.018

const imageCoords = []
const explosionCoords = new Array(explosionLength).fill([0, 0, "100vw", "100vh"])
const opacityList = []
for (let i = 0; i < explosionStartIndex; i++) {
    opacityList[i] = Math.round(((0.36 / explosionLength) * i) * 100) / 100
}

function RNG(seed) {
    // LCG using GCC's constants
    this.m = 0x80000000; // 2**31;
    this.a = 1103515245;
    this.c = 12345;

    this.state = seed
}
RNG.prototype.nextInt = function () {
    this.state = (this.a * this.state + this.c) % this.m;
    return this.state;
}
RNG.prototype.nextFloat = function () {
    return this.nextInt() / (this.m - 1);
}
const random = new RNG(seed)

for (let i = 0; i < explosionStartIndex; i++) {
    let zoom = scale * i
    let padding = Math.round(100 * zoom)
    const randomOffset = () => 0.2 * (2 * random.nextFloat() - 1) * padding
    let offsetX = Math.round(randomOffset())
    let offsetY = Math.round(randomOffset())
    imageCoords[i] = [
        `${-padding + offsetX}vw`,
        `${-padding + offsetY}vh`,
        `${100 + padding * 2 + offsetX}vw`,
        `${100 + padding * 2 + offsetY}vh`
    ]
}

const step = 3
const delays = []
for (let i = 0; i < explosionStartIndex; i++) {
    delays[i] = 110 - step * i
}
for (let i = 0; i < explosionLength; i++) {
    delays[explosionStartIndex + i] = 140
}

const template = {
    type: "gif",
    metadata: {
        alias: ["红温", "爆炸"],
        preview: "./8.png",
        apiVersion: 101
    },
    elements: [
        {
            type: "image",
            key: "to",
            coords: imageCoords,
            end: explosionStartIndex - 1
        },
        {
            type: "image",
            src: "./",
            coords: explosionCoords,
            fit: "cover",
            start: explosionStartIndex
        },
        {
            type: "image",
            src: "./red.png",
            coords: [
                "0vw",
                "0vh",
                "100vw",
                "100vh"
            ],
            opacity: opacityList,
            end: explosionStartIndex - 1
        }
    ],
    canvas: {
        width: `min(${width}/to_width, ${height}/to_height) * to_width`,
        height: `min(${width}/to_width, ${height}/to_height) * to_height`
    },
    delay: delays
}

fs.writeFileSync("./template.json", JSON.stringify(template))