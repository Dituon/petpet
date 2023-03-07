/**
 * @author Dituon
 * @version 0.0.2
 */

/**
 * @typedef { Object<any, any> } DOMParam
 * @property { string } html
 */

/**
 * @param { K } tagName
 * @param { Record<keyof HTMLElementTagNameMap[K], string> & {html: string} } [paramObj = {html: ''}]
 * @param { Record<E, (this:HTMLElementTagNameMap[K], ev: WindowEventMap[E]) => void> } [listenerMap]
 * @return { HTMLElementTagNameMap[K] }
 * @template {keyof HTMLElementTagNameMap} K
 * @template {keyof WindowEventMap} E
 */
export const dom = (tagName = 'div', paramObj = {html: ''}, listenerMap = {}) => {
    const dom = document.createElement(tagName)
    const {html, ...attr} = paramObj
    dom.innerHTML = html ?? ''

    Object.entries(listenerMap).forEach(item => dom.addEventListener(item[0], item[1]))

    if (!attr) return dom
    const attrArr = Object.entries(attr)
    attrArr.forEach(attrItem => dom.setAttribute(attrItem[0], attrItem[1]))
    return dom
}

/**
 * @param { string } text
 * @param { Function } callback
 * @param { boolean } [checked=false]
 * @return { HTMLDivElement }
 */
export const domCheckbox = (text, callback, checked = false) => {
    const checkbox = dom()
    const checkboxSpan = dom('span', {html: text})
    const checkboxInput = dom(
        'input',
        checked ? {type: 'checkbox', checked: true} : {type: 'checkbox'},
        {change: callback}
    )
    checkbox.append(checkboxSpan, checkboxInput)
    return checkbox
}

/**
 * @param { Function } [callback]
 * @param { {value: string, text: string} } optionObj
 * @return { HTMLSelectElement }
 */
export const domSelect = (callback = () => {
}, ...optionObj) => {
    return dom('select', {
        html: optionObj.map(op => `<option value="${op.value}">${op.text ?? op.value}</option>`).join('')
    }, {
        change: callback
    })
}

/**
 * @param { string } text
 * @param { DOMParam } [paramObj = {type: 'text'}]
 * @param { DOMListener } [listeners]
 * @return { HTMLElement }
 */
export const domInput = (text, paramObj = {type: 'text'}, ...listeners) => {
    const input = dom()
    const inputSpan = dom('span', {html: text})
    const inputElement = dom(
        'input',
        paramObj,
        ...listeners
    )
    input.append(inputSpan, inputElement)
    input.input = inputElement
    return input
}

export function createInputGroup() {
    const inputGroup = dom('div', {class: 'input-group'})
    inputGroup.append(...Array.from(arguments))
    return inputGroup
}

export function createRadioButtonGroup(text, nodeTexts = []) {
    const el = dom('div', {class: 'radio-btn-group'})
    el.append(text)
    for (let {key, value, checked = false} of nodeTexts) {
        el.append(dom('div', {
            html: `<label>
                    <input type="radio" name="${text}" value="${key}" ${checked ? 'checked' : ''}>
                    <div class="radio-btn"> ${value}</div>
                </label>`
        }))
    }
    return el
}
