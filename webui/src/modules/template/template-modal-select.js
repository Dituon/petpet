import './template.css'
import './modal-select.css'
import {dom, mask} from "../ui/index.js";

export default class {
    /** @type {HTMLElement} */
    #element
    #data
    /** @type {HTMLDivElement[]} */
    #dataDomList
    /** @type {HTMLDivElement} */
    #dataListDom
    /** @type {()=>void} */
    #setDataCallback
    /** @type {(TemplateDTO)=>void} */
    #selectCallback

    /**
     * @param { PetDataDTO } [data]
     */
    constructor(data) {
        if (!data) return
        this.data = data
    }

    /** @param {PetDataDTO} data */
    set data(data) {
        this.#element = dom('div', {class: 'modal-select'})
        this.#data = data
        this.#dataDomList = []

        const dataList = dom('div', {class: 'data-list'})
        this.#dataListDom = dataList
        for (const template of data.petData) {
            const key = template.key
            const baseUrl = `${data.url}/preview/${key}`
            const templateDom = dom(
                'div',
                {
                    html: `
                    <img src="${baseUrl}.gif" alt="${key}" loading="lazy" onerror="this.src = '${baseUrl}.png'">
                    <h3>${key}${template.alias.map(s=>`<span>${s}</span>`).join('')}</h3>`
                }, {
                    click: e => {
                        this.#selectCallback(template)
                        this.hide()
                    }
                }
            )
            templateDom.info = template
            this.#dataDomList.push(templateDom)
        }

        dataList.append(...this.#dataDomList)

        const inputEle = dom(
            'input',
            {
                placeholder: '🔍 type to search'
            },
            {
                change: e => this.search(e.target.value.trim())
            })

        this.#element.append(inputEle, dataList)
        mask.onclick = () => {
            this.hide()
            this.#selectCallback(null)
        }
        this.#setDataCallback && this.#setDataCallback()
        this.#element.classList.add('hide')

        document.body.appendChild(this.#element)
    }

    hide() {
        mask.hide()
        this.#element.classList.add('hide')
    }

    /** @return {Promise<TemplateDTO>} */
    async show() {
        // if (!this.#data) throw new Error('no data')
        if (!this.#data) await this.#waitData()
        mask.show()
        this.#element && this.#element.classList.remove('hide')
        return new Promise(res => this.#selectCallback = res)
    }

    async #waitData() {
        if (this.#data) return
        return new Promise(res => this.#setDataCallback = res)
    }

    /** @param {string} word */
    search(word) {
        this.#dataListDom.innerHTML = ''
        this.#dataListDom.append(...this.#dataDomList.filter(d => {
            const {key, alias} = d.info
            return key.includes(word) || alias.find(k => k.includes(word))
        }))
    }
}