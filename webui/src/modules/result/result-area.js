import {dom, Loading} from "../ui/index.js";
import './result.css'

export default class {
    /** @type {HTMLDivElement} */
    #parentElement
    /** @type {HTMLDivElement} */
    #element
    /** @type {HTMLImageElement} */
    #imageEle
    /** @type {Loading} */
    #loading

    constructor() {
        this.#parentElement = dom()
        this.#element = dom('div', {id: 'result-area'})
        this.#parentElement.append(
            dom('h3', {html: 'Result: 生成结果'}),
            this.#element
        )

        this.#imageEle = new Image()
        this.#hideImg()
        this.#element.appendChild(this.#imageEle)
        this.#loading = new Loading(this.#element)
        this.#loading.show()
    }

    set promise(imgPromise) {
        (async () => {
            this.#loading.show()
            try {
                const data = await imgPromise
                this.#imageEle.src = URL.createObjectURL(await data.blob())
                this.#showImg()
                this.#loading.hide()
            } catch (e) {
                this.#loading.error()
                throw new Error(e)
            }
        })()
    }

    #hideImg() {
        this.#imageEle.classList.add('hide')
    }

    #showImg() {
        this.#imageEle.classList.remove('hide')
    }

    get dom() {
        return this.#parentElement
    }
}