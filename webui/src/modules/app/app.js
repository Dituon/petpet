import config from "../../config.js"
import {TemplateChooser} from "../template"

import "./app.css"
import AvatarUploader from "../uploader/avatar-uploader.js";

/** @typedef { 'FROM' | 'TO' | 'BOT' | 'GROUP' } AvatarType */
/** @typedef { { key: string, alias: string[], types: AvatarType[] } } TemplateDTO */
/** @typedef { { version: number, petData: TemplateDTO[], url: string } } PetDataDTO */

export default class {
    /** @type { HTMLDivElement } */
    #parentElement
    /** @type { string } */
    #url
    /** @type { TemplateChooser } */
    #template
    /** @type { AvatarUploader } */
    #uploader

    /** @param { string } id */
    constructor(id) {
        this.#constructorAsync(id)
    }

    async #constructorAsync(id) {
        this.#parentElement = document.getElementById(id)
        this.#template = new TemplateChooser()

        this.#parentElement.appendChild(this.#template.dom)
        await this.#init()
        const template = await this.#template.showModal()
        this.#template.onchange = async t => this.#updateTemplate(await t)

        this.#updateTemplate(template)
    }

    async #init() {
        for (const url of config.server) {
            const data = await fetch(url + '/petpet').then(d => d.json())
            if (!data) continue
            data.url = url
            this.#url = url
            this.#template.data = data
            break
        }
        if (!this.#url) this.#template.loading.error()
        return true
    }

    /** @param {TemplateDTO} template */
    #updateTemplate = async (template) => {
        if (!template) return
        if (!this.#uploader) {
            this.#uploader = new AvatarUploader()
            this.#parentElement.appendChild(this.#uploader.dom)
        }


    }
}