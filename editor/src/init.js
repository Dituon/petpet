import {Editor} from "./module/app/editor.js"
import {loadGif} from "./module/loader/gif-loader.js"
import {loadVideo} from "./module/loader/video-loader.js";

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
    if (file.type.startsWith('video/')) {
        loadVideo(file, editorElement).then(imgList => new Editor(editorElement, ...imgList))
    } else if (!file.type.startsWith('image')) {
        throw new Error("不支持的格式: " + file.type)
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