import {dom, Loading} from "../ui"
import TemplateModal from "./template-modal-select.js"

export default class {
    #data
    #element
    /** @type {TemplateModal} */
    #modal
    /** @type { Loading } */
    loading
    #onChangeCallback

    /** @param { PetDataDTO } [data] */
    constructor(data) {
        this.#data = data
        this.#element = dom(
            'div',
            { id: 'template-chooser', html: '未选择' }
        )
        dom('div')
        this.#element.addEventListener('click',async () => {
            const template = this.showModal()
            this.#onChangeCallback && this.#onChangeCallback(template)
        })
        this.#modal = new TemplateModal(data)
        this.loading = new Loading(this.#element)
        if (!data) this.loading.show()
    }

    /** @param { PetDataDTO } data */
    set data(data) {
        this.loading.hide()
        this.#data = data
        this.#modal.data = data
    }

    get dom() {
        const root = dom()
        root.append(
            dom('h3', { html: 'Step 1: 选择模板' }),
            this.#element
        )
        return root
    }

    /** @return {Promise<TemplateDTO>} */
    async showModal(){
        const template = await this.#modal.show()
        if (template) this.#element.innerHTML = template.key
        return template
    }

    /** @param {(TemplateDTO)=>void} callback */
    set onchange(callback){
        this.#onChangeCallback = callback
    }
}