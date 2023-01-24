import { dom, Loading } from "../ui"
import TemplateModal from "./template-modal.js"

/** @typedef { import('../app/app.js').PetDataDTO } PetDataDTO */

export default class {
    data
    #chooserElement
    #modal
    /** @type { Loading } */
    loading

    /** @param { PetDataDTO } [data] */
    constructor(data) {
        this.data = data
        this.#chooserElement = dom(
            'div',
            { id: 'template-chooser', html: '未选择' }
        )
        this.#modal = new TemplateModal(this.#chooserElement, data)
        this.loading = new Loading(this.#chooserElement)
        if (!data) this.loading.show()
    }

    /** @param { PetDataDTO } data */
    set data(data) {
        this.loading.hide()
        this.url = data
    }

    get dom() {
        const root = dom()
        root.append(
            dom('h3', { html: 'Step 1: 选择模板' }),
            this.#chooserElement
        )
        return root
    }
}