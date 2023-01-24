import config from "../../config.js"
import { TemplateModal, TemplateChooser } from "../template"
import { Loading } from "../ui"

import "./app.css"

/**
 * @typedef { 'FROM' | 'TO' | 'BOT' | 'GROUP' } AvatarType
 *
 * @typedef { { key: string, types: AvatarType[] } } TemplateDTO
 * 
 * @typedef { { version: number, petData: TemplateDTO[] } } PetDataDTO
 */

export default class {
    /** @type { HTMLDivElement } */
    #parentElement
    /** @type { string } */
    #url
    /** @type { TemplateChooser } */
    #template

    /** @param { string } id */
    constructor(id) {
        this.#parentElement = document.getElementById(id)
        this.#template = new TemplateChooser()

        this.#parentElement.append(this.#template.dom)
        this.#init()
    }

    async #init() {
        for await (const urlRes of
            config.server.map(s => fetch(s).catch(() => null))
        ) {
            if (!urlRes?.ok) continue
            this.#url = urlRes.url
            this.#template.data = await urlRes.json()
            break
        }
        if (!this.#url) this.#template.loading.error()
    }
}