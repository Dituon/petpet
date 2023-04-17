import {dom, domInput} from "../util/dom.js"
import './video-loader.css'

class VideoLoader {
    #video
    #element
    color
    frameCount = 1
    threshold = 128
    #previewArea = dom('div', {class: 'preview-bar'})
    #mainCanvas
    #frameList = []
    #callback
    constructor(video, element) {
        this.#video = video
        this.#element = element
        const canvas = getFrameCanvas(video)
        canvas.className = 'main-preview'
        this.#mainCanvas = canvas
        element.appendChild(canvas)
        element.appendChild(this.#previewArea)

        element.appendChild(domInput('视频帧数', {
            type: 'number',
            value: this.frameCount,
            min: 1
        }, {
            event: 'change',
            fun: e => this.frameCount = e.target.value
        }))

        element.appendChild(domInput('颜色容差', {
            type: 'number',
            value: this.threshold,
            min: 0,
            max: 255
        }, {
            event: 'change',
            fun: e => this.threshold = e.target.value
        }))

        element.appendChild(dom('button', {
            html: '取色'
        }, {
            event: 'click',
            fun: e => chooseColor(canvas, c => {
                e.target.innerHTML = '取色: ' + c
                e.target.style.color = `rgb(${c})`
                this.color = c
            })
        }))

        element.appendChild(dom('button', {
            html: '预览'
        }, {
            event: 'click',
            fun: e => this.process()
        }))
    }

    async process() {
        const interval = this.#video.duration / this.frameCount
        this.#previewArea.innerHTML = ''
        const r = this.color[0]
        const g = this.color[1]
        const b = this.color[2]

        for (let i = 0; i < this.frameCount; i++) {
            this.#video.currentTime = i * interval
            await new Promise(res => this.#video.oncanplay = res)
            const canvas = getFrameCanvas(this.#video)
            this.#previewArea.appendChild(canvas)
            canvas.addEventListener('click', () => {
                this.#mainCanvas.getContext('2d').drawImage(canvas, 0, 0)
            })
            this.#frameList[i] = canvas

            if (!this.color) continue
            const imageData = canvas.getContext("2d").getImageData(0, 0, canvas.width, canvas.height)
            const pixels = imageData.data

            for (let j = 0; j < pixels.length; j += 4) {
                const pixelColor = [pixels[j], pixels[j + 1], pixels[j + 2]]
                const distance = Math.sqrt(
                    (pixelColor[0] - r) ** 2 + (pixelColor[1] - g) ** 2 + (pixelColor[2] - b) ** 2
                )
                if (distance < this.threshold) {
                    pixels[j + 3] = 0
                }
            }

            canvas.getContext("2d").putImageData(imageData, 0, 0)
        }

        if (!this.initFlag) this.#element.appendChild(dom('button', {
            html: 'Next'
        }, {
            event: 'click',
            fun: async e => this.#callback(await this.getImages())
        }))

        this.initFlag = true
    }

    async waitImages(){
        return await new Promise(res => this.#callback = res)
    }

    async getImages() {
        const promiseList = []
        const imgList = this.#frameList.map(c => {
            const img = document.createElement('img')
            img.src = c.toDataURL()
            promiseList.push(new Promise(res => img.onload = res))
            return img
        })
        await Promise.all(promiseList)
        return imgList
    }
}

/**
 * 加载视频
 * @param { File } file
 * @param {HTMLDivElement} [element]
 * @return {Promise<HTMLImageElement[]>}
 */
export async function loadVideo(file, element) {
    element = element ?? document.body.appendChild(dom())
    element.innerHTML = ''

    const video = dom('video')
    video.src = URL.createObjectURL(file) + '#t=0.0001'

    return new Promise(res => {
        video.addEventListener("loadeddata", async () => {
            const loader = new VideoLoader(video, element)
            res(await loader.waitImages())
        })
    })
}

function getFrameCanvas(video) {
    const canvas = dom('canvas')
    canvas.width = video.videoWidth
    canvas.height = video.videoHeight
    canvas.getContext('2d').drawImage(video, 0, 0)
    return canvas
}

async function chooseColor(canvas, callback) {
    const img = new Image()
    img.src = canvas.toDataURL()

    let isMouseDown = false;
    let mouseX = 0;
    let mouseY = 0;

    canvas.addEventListener("click", function (event) {
        isMouseDown = true
        mouseX = event.offsetX
        mouseY = event.offsetY
        updatePixelColor()
    })

    canvas.addEventListener("mousemove", function (event) {
        mouseX = event.offsetX
        mouseY = event.offsetY
    })

    function updatePixelColor() {
        const pixelData = ctx.getImageData(mouseX, mouseY, 1, 1).data;
        callback && callback([
            pixelData[0],
            pixelData[1],
            pixelData[2],
            pixelData[3]
        ])
    }

    const ctx = canvas.getContext('2d')

    function drawCrosshair() {
        const ctx = canvas.getContext('2d')
        const lineWidth = 1
        const lineLength = Math.max(canvas.width, canvas.height)
        const halfLineLength = lineLength / 2
        ctx.strokeStyle = '#fff'

        ctx.beginPath()
        ctx.moveTo(mouseX - halfLineLength, mouseY)
        ctx.lineTo(mouseX - 5, mouseY)
        ctx.moveTo(mouseX + 5, mouseY)
        ctx.lineTo(mouseX + halfLineLength, mouseY)
        ctx.lineWidth = lineWidth
        ctx.stroke()

        ctx.beginPath()
        ctx.moveTo(mouseX, mouseY - halfLineLength)
        ctx.lineTo(mouseX, mouseY - 5)
        ctx.moveTo(mouseX, mouseY + 5)
        ctx.lineTo(mouseX, mouseY + halfLineLength)
        ctx.lineWidth = lineWidth
        ctx.stroke()
    }

    function animate() {
        ctx.clearRect(0, 0, canvas.width, canvas.height)
        ctx.drawImage(img, 0, 0, canvas.width, canvas.height)
        drawCrosshair()
        requestAnimationFrame(animate)
    }

    animate()
}