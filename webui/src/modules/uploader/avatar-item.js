import {dom} from "../ui/index.js";

const stopPropagation = e => {
    e.stopPropagation()
    e.preventDefault()
}

/** @param { FileList } files */
const chooseFiles = files => {
    let file = files.item(0)
    if (!file) return
    if (!file.type.startsWith('image')) {
        throw new Error("仅支持图片格式")
    }

    const reader = new FileReader()
    const image = new Image()
    reader.readAsDataURL(file)
    reader.onload = () => image.src = reader.result.toString()

    image.onload = () => {

    }
}

export default class {
    #element

    /** @param {AvatarType} type */
    constructor(type) {
        this.#element = dom()
        this.#element.addEventListener('dragenter', stopPropagation, false)
        this.#element.addEventListener('dragover', stopPropagation, false)
        this.#element.addEventListener("drop", e => {
            stopPropagation(e)
            chooseFiles(e.dataTransfer.files)
        }, false)
    }

    get dom(){
        return this.#element
    }
}