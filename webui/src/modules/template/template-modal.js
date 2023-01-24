import './template.css'

/** @typedef { import('../app/app.js').PetDataDTO } PetDataDTO */

export default class {
    #injectElement
    data

    /** 
     * @param { HTMLElement } injectElement
     * @param { PetDataDTO } [data]
     */
    constructor(injectElement, data) {
        this.#injectElement = injectElement
        this.data = data

        
    }

    show(){
        if (!this.data) throw new Error('no data')
    }
}