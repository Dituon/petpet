import {Editor} from "./editor.js"
import {SuperGif} from "../lib/libgif.min.js"

const stopPropagation = e => {
    e.stopPropagation()
    e.preventDefault()
}

const dropElement = document.getElementById("drop-area")

/** @param { FileList } files */
const chooseFiles = files => {
    let file = files.item(0)
    if (!file) return
    try {
        initBackground(file)
        dropElement.classList.add('hide')
    } catch (e) {
        alert(e)
    }
}

//拖拽上传
dropElement.addEventListener("dragenter", stopPropagation, false)
dropElement.addEventListener("dragover", stopPropagation, false)
dropElement.addEventListener("drop", e => chooseFiles(e.dataTransfer.files), false)

//选择上传
/** @type { HTMLInputElement } */
const fileElement = document.getElementById('file')
fileElement.addEventListener('change', () => chooseFiles(fileElement.files))

const editorElement = document.getElementById('editor')

/**
 * 加载图片
 * @param { File } file
 * @throw
 */
function initBackground(file) {
    if (!file.type.startsWith('image')) {
        throw new Error("仅支持图片格式")
    }

    const reader = new FileReader()
    const image = new Image()
    reader.readAsDataURL(file)
    reader.onload = () => image.src = reader.result.toString()

    image.onload = () => {
        if (file.type === 'image/gif') {
            loadGif(image).then(frameList => new Editor(editorElement, ...frameList))
        } else {
            new Editor(editorElement, image)
        }
    }
}

/**
 * 加载GIF
 * @param { HTMLImageElement } image
 * @return {Promise<HTMLImageElement[]>}
 */
async function loadGif(image) {
    document.body.appendChild(image)
    let gif = new SuperGif({gif: image})
    await new Promise(resolve => gif.load(resolve))
    document.querySelector('.jsgif').remove()
    /** @type { HTMLImageElement[] } */
    const frameList = []
    for (let i = 0; i < gif.get_length(); i++) {
        gif.move_to(i)
        let frameImage = new Image()
        frameImage.src = gif.get_canvas().toDataURL('image/png', 1)
        frameList.push(frameImage)
    }
    return frameList
}