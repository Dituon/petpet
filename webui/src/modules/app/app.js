import config from "../../config.js"
import {TemplateChooser} from "../template"
import {AvatarUploader} from "../uploader";

import "./app.css"

/** @typedef { 'FROM' | 'TO' | 'BOT' | 'GROUP' } AvatarType */
/** @typedef { { key: string, alias: string[], types: AvatarType[] } } TemplateDTO */
/** @typedef { { version: number, petData: TemplateDTO[], url: string } } PetDataDTO */

export default class {
    /** @type { HTMLDivElement } */
    #parentElement
    /** @type { string } */
    #url
    /** @type { TemplateDTO } */
    #template
    /** @type { TemplateChooser } */
    #templateChooser
    /** @type { AvatarUploader } */
    #uploader

    /** @param { string } id */
    constructor(id) {
        this.#constructorAsync(id)
    }

    async #constructorAsync(id) {
        this.#parentElement = document.getElementById(id)
        this.#templateChooser = new TemplateChooser()

        this.#parentElement.appendChild(this.#templateChooser.dom)
        await this.#init()
        const template = await this.#templateChooser.showModal()
        this.#templateChooser.onchange = async t => this.#updateTemplate(await t)

        this.#updateTemplate(template)
    }

    async #init() {
        for (const url of config.server) {
            const data = await fetch(url + '/petpet').then(d => d.json())
            if (!data) continue
            data.url = url
            this.#url = url
            this.#templateChooser.data = data
            break
        }
        if (!this.#url) this.#templateChooser.loading.error()
        return true
    }

    /** @param {TemplateDTO} template */
    #updateTemplate = async template => {
        if (!template) return
        this.#template = template
        if (!this.#uploader) {
            this.#uploader = new AvatarUploader()
            this.#uploader.onchange = this.generate
            this.#parentElement.appendChild(this.#uploader.dom)
        }

        this.#uploader.types = [...new Set(template.types)]
        this.generate()
    }

    generate = async () => {
        if (!this.#uploader.ready) return

        const formData = new FormData()
        formData.append('key', this.#template.key)
        for (const item of this.#uploader.data){
            formData.append(item.name, item.file, item.name)
        }

        const data = await fetch(this.#url + '/petpet', {
            body: formData,
            method: 'post'
        })

        const img = document.createElement('img')
        img.src = URL.createObjectURL(await data.blob())
        document.body.appendChild(img)
    }
}