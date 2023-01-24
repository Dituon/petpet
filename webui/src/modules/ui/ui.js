import { dom } from "./dom.js"
import './loading.css'

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
        const mask = dom('div', { class: 'loading' })
        mask.append(dom(), dom(), dom())
        this.#element.appendChild(mask)
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
}