/** @typedef { 'IMG' | 'GIF' } TempType */

import {dom, domCheckbox, domInput} from "./dom.js"
import {AvatarModel} from "./avatar.js"
import {TextModel} from "./text.js"
import JSZip from "jszip"
import {saveAs} from "file-saver"

/**
 * @typedef { object } PetDataDTO
 * @property { TempType } key
 * @property { AvatarDTO[] } avatar
 * @property { TextDTO[] } text
 * @property { string[] } alias
 * @property { boolean } inRandomList
 * @property { number | undefined } delay
 */

export class Editor {
    /** @type { TempType } */
    type
    /** @type { fabric.Canvas } */
    canvas
    /** @type { HTMLImageElement } */
    #background
    /** @type { Set<AvatarModel> } */
    #avatarSet = new Set()
    /** @type { Set<TextModel> } */
    #textSet = new Set()
    /** @type { HTMLDivElement } */
    #modalListElement
    /** @type { HTMLDivElement } */
    #posElement
    /** @type { string } */
    id = 'example'
    /** @type { string[] } */
    alias = []
    inRandomList = true
    fps = 15
    #frameLength = 1

    /**
     * @param { HTMLElement } parentElement
     * @param { HTMLImageElement } images
     */
    constructor(parentElement, ...images) {
        this.imageList = images

        const infoElement = dom('div', {class: 'info bar'})
        const bgSizeElement = dom('span', {html: `背景: ${images[0].width} * ${images[0].height}`})
        const posElement = dom('span', {html: '未选择对象'})
        this.#posElement = posElement
        infoElement.append(bgSizeElement, posElement)

        const canvasElement = dom('canvas')
        parentElement.append(
            infoElement,
            canvasElement
        )

        const fabricCanvas = new fabric.Canvas(canvasElement)
        fabricCanvas.setWidth(images[0].width)
        fabricCanvas.setHeight(images[0].height)
        this.canvas = fabricCanvas
        this.background = images[0]

        const DTOArea = dom('textarea')

        const settingBarElement = dom('div', {class: 'bar toolbar'})
        const aliaChange = e => e.target.size = e.target.length > 4 ? e.target.length * 1.5 : 2
        let idInput = domInput('ID', {
            type: 'text',
            size: 6,
            placeholder: '唯一标识'
        }, {
            event: 'change',
            fun: e => this.id = e.target.value.trim()
        });
        let aliasInput = domInput('别名', {
            type: 'text',
            size: 2,
            placeholder: '多别名用空格分割'
        }, {
            event: 'keyup',
            fun: aliaChange
        }, {
            event: 'keydown',
            fun: aliaChange
        }, {
            event: 'change',
            fun: e => this.alias = e.target.value.trim().split(' ')
        });
        idInput.classList.add("id-input")
        aliasInput.classList.add("alias-input")
        let randomListCheckBox = domCheckbox('在随机表列中', e => this.inRandomList = e.target.checked, this.inRandomList);
        randomListCheckBox.classList.add("random-list-checkbox")
        settingBarElement.append(
            dom('div', {html: 'addAvatar',class:'add-avatar btn'}, {
                event: 'click',
                fun: e => this.addAvatar()
            }),
            dom('div', {html: 'addText', class:'add-text btn',}, {
                event: 'click',
                fun: e => this.addText()
            }),
            dom('div', {html: 'buildData',class:"btn buildData"}, {
                event: 'click',
                fun: e => DTOArea.innerHTML = JSON.stringify(this.DTO, null, 4)
            }),
            idInput,
            aliasInput,
            randomListCheckBox
        )

        const download = dom('div', {html: '下载',class:'btn'}, {
            event: 'click',
            fun: () => this.download()
        })

        const modalListElement = dom('div', {class: 'bar'})
        this.#modalListElement = modalListElement

        parentElement.append(
            settingBarElement,
            modalListElement
        )

        this.type = 'IMG'
        if (images.length <= 1) {
            parentElement.appendChild(DTOArea)
            settingBarElement.appendChild(download)
            return
        }

        this.#frameLength = images.length
        this.type = 'GIF'
        const backgroundListElement = dom('div', {class: 'gif-bar'})
        let i = 0
        images.forEach(image => {
            image.frameIndex = i++
            image.addEventListener('click', () => this.background = image)
        })
        backgroundListElement.append(...images)
        parentElement.appendChild(backgroundListElement)

        settingBarElement.append(
            dom('input', {
                type: 'number',
                value: 15,
                size: 4
            }, {
                event: 'change',
                fun: e => this.fps = e.target.value
            }),
            download
        )

        parentElement.appendChild(DTOArea)
    }

    /** @param { HTMLImageElement } image */
    set background(image) {
        if (this.#background) this.#background.className = ''
        let frameIndex = image.frameIndex ?? 0
        image.className = 'checked'
        this.#background = image
        this.#avatarSet.forEach(avatar => avatar.frameIndex = frameIndex)
        this.canvas.backgroundImage = new fabric.Image(this.#background)
        this.canvas.renderAll()
    }

    addAvatar() {
        const avatar = new AvatarModel(this.canvas)
        avatar.frameLength = this.#frameLength
        this.#avatarSet.add(avatar)
        avatar.removeCallback = a => this.#avatarSet.delete(a)
        this.#modalListElement.appendChild(avatar.DOM)
        avatar.listener = () => this.#posElement.innerText = JSON.stringify(avatar.getPos())
    }

    addText() {
        const text = new TextModel(this.canvas)
        this.#textSet.add(text)
        text.removeCallback = t => this.#textSet.delete(t)
        this.#modalListElement.appendChild(text.DOM)
        text.listener = () => this.#posElement.innerText = JSON.stringify(text.pos)
    }

    /** @return { PetDataDTO } */
    get DTO() {
        return {
            type: this.type,
            avatar: [...this.#avatarSet].map(a => a.DTO),
            text: [...this.#textSet].map(t => t.DTO),
            alias: this.alias,
            inRandomList: this.inRandomList,
            delay: this.type === 'GIF' ? Math.round(1000 / this.fps) : undefined
        }
    }

    download() {
        const zip = new JSZip()
        const root = zip.folder(this.id)
        let i = 0
        this.imageList.forEach(img => {
            const raw = img.src.split(';base64,')[1]
            root.file(`${i++}.png`, raw, {base64: true})
        })
        root.file('data.json', JSON.stringify(this.DTO))
        zip.generateAsync({type: 'blob'}).then(content => {
            saveAs(content, `${this.id}.zip`)
        })
    }
}
