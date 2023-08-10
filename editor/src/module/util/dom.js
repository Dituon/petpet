/**
 * @author Dituon
 * @version 0.0.1
 */

/**
 * @typedef { object } DOMListener
 * @property { string } event
 * @property { Function } fun
 */

/**
 * @typedef { object } DOMParam
 * @property { string } html
 */

/**
 * @param { K } [tagName = 'div']
 * @param { Record<keyof HTMLElementTagNameMap[K], string> & {html: string} } [paramObj = {html: ''}]
 * @param { DOMListener } [listeners]
 * @return { HTMLElementTagNameMap[K] }
 * @template {keyof HTMLElementTagNameMap} K
 */
export const dom = (tagName = 'div', paramObj = {html: ''}, ...listeners) => {
    const dom = document.createElement(tagName)
    const {html, ...attr} = paramObj
    dom.innerHTML = html ?? ''

    listeners.forEach(listener => dom.addEventListener(listener.event, listener.fun))

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
        {event: 'change', fun: callback}
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
        event: 'change',
        fun: callback
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

export function createRadioButtonGroup(text,name, nodeTexts = []) {
    const el = dom('div', {class: 'radio-btn-group'})
    el.append(text)
    for (let {key, value,checked=false} of nodeTexts) {
        el.append(dom('div', {
            html: `        <label>
                    <input type="radio" name="${name}" value="${key}" ${checked?'checked':''}>
                    <div class="radio-btn"> ${value}</div>
                </label>`
        }))
    }
    return el
}
