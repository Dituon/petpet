import {dom} from "../ui/index.js";

const stopPropagation = e => {
    e.stopPropagation()
    e.preventDefault()
}

export default class {
    type
    /** @type {HTMLLabelElement} */
    #element
    #file
    #callbcak

    /** @param {AvatarType} type */
    constructor(type) {
        this.type = type
        this.#element = dom('label', {type})
        this.#element.addEventListener('dragenter', stopPropagation, false)
        this.#element.addEventListener('dragover', stopPropagation, false)
        this.#element.addEventListener("drop", e => {
            stopPropagation(e)
            this.chooseFiles(e.dataTransfer.files)
        }, false)
        const fileEle = dom('input', {
            type: 'file',
            accept: 'image/*'
        }, {
            change: e => this.chooseFiles(e.target.files)
        })
        this.#element.appendChild(fileEle)
    }

    get dom() {
        return this.#element
    }

    /** @param {(AvatarItem)=>void} callback */
    set onchange(callback) {
        this.#callbcak = callback
    }

    get file() {
        return this.#file
    }

    /** @param { FileList } files */
    chooseFiles(files) {
        let file = files.item(0)
        if (!file) return
        if (!file.type.startsWith('image')) {
            throw new Error("仅支持图片格式")
        }

        this.#element.style.backgroundImage = `url(${URL.createObjectURL(file)})`
        this.#element.style.backgroundSize = 'cover'
        this.#file = file
        this.#callbcak && this.#callbcak(this)
    }
}