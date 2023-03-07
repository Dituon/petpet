import {dom} from "./dom.js"
import './loading.css'
import './ui.css'

export class Loading {
    /** @type { HTMLElement } */
    #element
    /** @type { HTMLDivElement } */
    #maskElement

    /** @param { HTMLDivElement } element */
    constructor(element) {
        this.#element = element
    }

    show() {
        if (this.#maskElement) return
        const mask = dom('div', {class: 'loading'})
        mask.append(dom(), dom(), dom())
        this.#element.appendChild(mask)
        mask.addEventListener('click', this.#onclick)
        this.#maskElement = mask
    }

    hide() {
        if (!this.#maskElement) return
        this.#maskElement.remove()
        this.#maskElement = null
    }

    error() {
        let errText = '加载失败'
        this.#maskElement.innerHTML = `<span>${errText}</span>`
        throw new Error(errText)
    }

    #onclick(e){
        e.stopPropagation()
    }
}

export const mask = new class {
    /** @type { HTMLElement } */
    element
    /** @type { boolean } */
    showing
    timer

    constructor() {
        this.element = dom('div', {id: 'mask'})
        document.body.appendChild(this.element)
        this.showing = false
    }

    /**
     * @param {Function} callback
     */
    set onclick(callback) {
        if (this.timer) return
        this.element.addEventListener('click', callback)
    }

    show() {
        if (this.timer) {
            clearTimeout(this.timer)
            this.timer = null
        }
        this.element.style.pointerEvents = 'auto'
        this.element.style.display = 'block'
        setTimeout(() => this.element.style.opacity = '0.5', 10)
    }

    hide() {
        this.element.style.opacity = '0'
        this.element.style.pointerEvents = 'none'
        this.timer = setTimeout(() => this.element.style.display = 'none', 1000)
    }

    togger() {
        this.showing ? this.hide() : this.show()
    }
}