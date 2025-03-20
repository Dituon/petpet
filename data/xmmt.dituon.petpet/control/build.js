const fs = require("fs")

const width = 480
const height = 480

const seed = 30

const length = 17
const startIndex = 3

const imageCoords = []

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

for (let i = 0; i < length; i++) {
    if (i < startIndex) {
        imageCoords[i] = [
            0, 0, `100vw`, `100vh`
        ]
        continue
    }

    const randomOffset = () => 0.2 * (2 * random.nextFloat() - 1)
    let offsetX = Math.round(randomOffset() * 12)
    let offsetY = Math.round(randomOffset() * 8)
    imageCoords[i] = [
        `${offsetX}vw`,
        `${offsetY}vh`,
        `100vw`,
        `100vh`
    ]
}

fs.writeFileSync("./template.json", JSON.stringify({
    type: "gif",
    metadata: {
        alias: ["遥控", "开关"],
        tags: ["nsfw"],
        preview: "./3.png",
        inRandomList: false,
        apiVersion: 101
    },
    elements: [
        {
            type: "image",
            key: "to",
            fit: "cover",
            coords: imageCoords
        },
        {
            type: "image",
            src: "./",
            coords: [0, 0, "100vw", "100vh"]
        }
    ],
    canvas: {
        width: `min(${width}/to_width, ${height}/to_height) * to_width`,
        height: `min(${width}/to_width, ${height}/to_height) * to_height`
    },
    delay: 70
}))

for (let i = 0; i < length; i++) {
    if (i < startIndex) {
        imageCoords[i] = [
            0, 0, width, height
        ]
        continue
    }

    const randomOffset = () => 0.2 * (2 * random.nextFloat() - 1)
    let offsetX = Math.round(randomOffset() * 12 * 5)
    let offsetY = Math.round(randomOffset() * 8 * 5)
    imageCoords[i] = [
        offsetX, offsetY, width, height
    ]
}

fs.writeFileSync("./data.json", JSON.stringify({
    type: "GIF",
    avatar: [
        {
            type: "TO",
            fit: "COVER",
            pos: imageCoords,
            avatarOnTop: false
        }
    ],
    alias: ["遥控", "开关"],
    inRandomList: false,
    delay: 70
}))

