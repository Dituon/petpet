/**
 * @typedef { 'FROM' | 'TO' | 'GROUP' | 'BOT' | 'RANDOM' } AvatarType
 * @typedef { 'MIRROR' | 'FLIP' | 'GRAY' | 'BINARIZATION' } AvatarStyle
 * @typedef { 'ZOOM' | 'DEFORM' } AvatarPosType
 */

import {fabric} from "fabric"
import {actionHandler, anchorWrapper, polygonPositionHandler} from "./deform.js"
import {createInputGroup, createRadioButtonGroup, dom, domCheckbox, domSelect} from "./dom.js"
import {Model} from "./model.js"

/**
 * @typedef { object } AvatarDTO
 * @property { AvatarType } type
 * @property { AvatarPosType } posType
 * @property { number[4][] | number[2][5][] } pos
 * @property { AvatarStyle[] } style
 * @property { number } opacity
 * @property { boolean } rotate
 * @property { boolean } round
 * @property { boolean } avatarOnTop
 *
 */

export class AvatarModel extends Model {
    static defaultPosArray = () => [0, 0, 0, 0]
    static defaultDeformPosArray = () => [[0, 0], [0, 0], [0, 0], [0, 0], [0, 0]]

    /** @type { fabric.Image } */
    avatar
    /** @type { Map<number, number[4]> } */
    #posMap = new Map()
    #deform = false
    #round = false
    #onTop = true
    #opacity = 1.0
    #rotate = false
    /** @type { AvatarType } */
    type = 'TO'
    /** @type { Set<AvatarStyle> } */
    #styleSet = new Set()
    /** @type { Map<number, number[2][5]> } */
    #deformPosMap = new Map()
    /** @type { fabric.Polygon } */
    #polygon
    #frameIndex = 0
    #frameLength = 1
    #filterMap = new Map()

    /**
     * @param { fabric.Canvas } canvas
     * @param { string } [url = './avatar.jpg']
     */
    constructor(canvas, url = './avatar.jpg') {
        super(canvas)
        fabric.Image.fromURL(url, a => {
            this.avatar = a
            a.scale(0.2)
            a.setControlsVisibility({mtr: false})
            a.uniformScaling = true

            a.on('moving', () => {
                this.listener(this)
                this.#posMap.set(this.#frameIndex, buildPos())
            }).on('scaling', () => {
                this.listener(this)
                this.#posMap.set(this.#frameIndex, buildPos())
            })
            this.canvas.add(a)
            this.canvas.renderAll()
        })

        /** @returns { number[4] } */
        const buildPos = () => [
            Math.round(this.avatar.left),
            Math.round(this.avatar.top),
            Math.round(this.avatar.getScaledWidth()),
            Math.round(this.avatar.getScaledHeight())
        ]
    }

    /** @param { number } length */
    set frameLength(length) {
        this.#frameLength = length
    }

    /** @param { number } index */
    set frameIndex(index) {
        this.#frameIndex = index < 0 ? 0 : index
    }

    /** @return { number[4][] | number[2][5][] } */
    get posList() {
        const arr = new Array(this.#frameLength).fill(
            this.#deform ? AvatarModel.defaultDeformPosArray() : AvatarModel.defaultPosArray(),
        )
        const map = this.#deform ? this.#deformPosMap : this.#posMap
        map.forEach((pos, index) => arr[index] = pos)
        console.log(arr)
        return arr
    }

    /**
     * @param { number } frameIndex >=0
     * @return { number[4] | number[2][5] }
     */
    getPos(frameIndex = -1) {
        if (frameIndex < 0) return this.getPos(this.#frameIndex)
        return this.#deform ?
            (this.#deformPosMap.get(frameIndex) ?? AvatarModel.defaultDeformPosArray())
            : (this.#posMap.get(frameIndex) ?? AvatarModel.defaultPosArray())
    }

    /** @param { boolean } boolean */
    set deform(boolean) {
        this.#deform = boolean
        if (!boolean) {
            this.#deformPosMap = new Map()
            this.canvas.remove(this.#polygon)
            this.canvas.add(this.avatar)
            this.canvas.renderAll()
            return
        }

        const a = this.avatar
        this.canvas.remove(a)

        const points = [{
            x: a.left, y: a.top
        }, {
            x: a.left, y: a.getScaledHeight() + a.top
        }, {
            x: a.getScaledWidth() + a.left, y: a.getScaledHeight() + a.top
        }, {
            x: a.getScaledWidth() + a.left, y: a.top
        }]
        this.#polygon = new fabric.Polygon(points, {
            fill: '#FFF0F5',
            strokeWidth: 2,
            stroke: '#FFB6C1',
            objectCaching: false,
            transparentCorners: false,
            absolutePositioned: true
        })
        const polygon = this.#polygon
        this.canvas.add(polygon)

        this.canvas.setActiveObject(polygon)
        const lastControl = polygon.points.length - 1
        polygon.cornerStyle = 'circle'
        polygon.cornerColor = 'rgba(40,40,255,0.5)'
        polygon.controls = polygon.points.reduce((acc, point, index) => {
            acc['p' + index] = new fabric.Control({
                positionHandler: polygonPositionHandler,
                actionHandler: anchorWrapper(index > 0 ? index - 1 : lastControl, actionHandler),
                actionName: 'modifyPolygon',
                pointIndex: index
            })
            return acc
        }, {})
        polygon.hasBorders = false

        this.#posMap.forEach((index, pos) => { //xywh转换为deform格式
            this.#deformPosMap.set(index, [
                [pos[0], pos[1]],
                [pos[0], pos[1] + pos[3]],
                [pos[0] + pos[2], pos[1] + pos[3]],
                [pos[0] + pos[2], pos[1]],
                [0, 0]
            ])
        })

        /** @return { number[2][5] } */
        const buildPos = () => {
            const newPosArr = []
            const x = Math.round(polygon.left)
            const y = Math.round(polygon.top)
            for (let i = 0; i < 4; i++) {
                const absolutePoint = fabric.util.transformPoint({
                    x: (polygon.points[i].x - polygon.pathOffset.x),
                    y: (polygon.points[i].y - polygon.pathOffset.y)
                }, polygon.calcTransformMatrix())
                newPosArr[i] = [Math.round(absolutePoint.x - x), Math.round(absolutePoint.y - y)]
            }
            newPosArr[4] = [x, y]
            return newPosArr
        }

        this.#polygon.on('moved', () => {
            this.listener(this)
            this.#deformPosMap.set(this.#frameIndex, buildPos())
        }).on('modified', () => {
            this.listener(this)
            this.#deformPosMap.set(this.#frameIndex, buildPos())
        })
    }

    /** @param { number | string } opacity 0.0-1.0 */
    set opacity(opacity) {
        opacity = parseFloat(opacity)
        if (opacity < 0 || opacity > 1) throw new Error()
        this.#opacity = opacity
        this.avatar.opacity = opacity
        this.canvas.renderAll()
    }

    /** @param { boolean } boolean */
    set rotate(boolean) {
        this.#rotate = boolean
    }

    /** @param { boolean } boolean */
    set onTop(boolean) {
        this.#onTop = boolean
        this.avatar.opacity = boolean ? 1 : 0.5
        this.avatar.set(boolean ? {
            strokeWidth: 0
        } : {
            stroke: 'white',
            strokeWidth: 4
        })
        this.canvas.renderAll()
    }

    /** @param { AvatarStyle[] } list */
    set styleList(list) {
        this.#styleSet = new Set(list)
        this.avatar.set('flipX', list.includes('MIRROR'))
        this.avatar.set('flipY', list.includes('FLIP'))

        if (list.includes('GRAY')) {
            this.#filterMap.has('GRAY') || this.#filterMap.set('GRAY', new fabric.Image.filters.Grayscale())
        } else {
            this.#filterMap.delete('GRAY')
        }

        if (list.includes('BINARIZATION')) {
            this.#filterMap.has('BINARIZATION') || this.#filterMap.set('BINARIZATION', new fabric.Image.filters.Binarization())
        } else {
            this.#filterMap.delete('BINARIZATION')
        }

        this.avatar.applyFilters([...this.#filterMap.values()])
        this.canvas.renderAll()
    }

    /** @param { AvatarStyle } style */
    addStyle(style) {
        this.styleList = [...this.#styleSet.add(style)]
    }

    /** @param { AvatarStyle } style */
    deleteStyle(style) {
        this.#styleSet.delete(style)
        this.styleList = [...this.#styleSet]
    }

    /** @param { boolean } boolean */
    set round(boolean) {
        this.#round = boolean

        if (!boolean) {
            this.avatar.set("clipPath", null)
            this.canvas.renderAll()
            return boolean
        }

        const roundedCorners = (avatar, radius) => new fabric.Rect({
            width: avatar.width,
            height: avatar.height,
            rx: radius / avatar.scaleX,
            ry: radius / avatar.scaleY,
            left: -avatar.width / 2,
            top: -avatar.height / 2
        })
        this.avatar.set("clipPath", roundedCorners(this.avatar,
            this.avatar.width < this.avatar.height ? (this.avatar.width / 2) : (this.avatar.height / 2)
        ))
        this.canvas.renderAll()
    }

    remove() {
        this.canvas.remove(this.#deform ? this.#polygon : this.avatar)
        this.canvas.renderAll()
        this.dom.remove()
        this.removeCallback(this)
    }

    /** @return { HTMLDivElement } */
    get DOM() {
      const content=dom('div',{
          class:'form-content'
      })
        const deform = domCheckbox('Deform', e => this.deform = e.target.checked)
        const round = domCheckbox('Round', e => this.round = e.target.checked)
        const rotate = domCheckbox('Rotate', e => this.rotate = e.target.checked)
        const onTop = domCheckbox('OnTop', e => this.onTop = e.target.checked, true)
        const opacity = dom('input', {
            type: 'range',
            max: 1.0,
            min: 0.0,
            step: 0.01,
            value: 1.0
        }, {
            event: 'change',
            fun: e => {
                this.opacity = e.target.value
            }
        })
        const type = createInputGroup(createRadioButtonGroup('type',['TO', 'FROM', 'GROUP', 'BOT', 'RANDOM'].map((key,index) => {
          const  checked=(!index)
            return {key,value:key,checked}
        })))
        // const type = domSelect(
        //     e => this.type = e.target.value,
        //     ...['TO', 'FROM', 'GROUP', 'BOT', 'RANDOM'].map(t => ({value: t}))
        // )

        const styleList = createInputGroup()
        const styles = ['MIRROR', 'FLIP', 'GRAY', 'BINARIZATION']
        styles.forEach(style => {
            const checkbox = domCheckbox(style, e =>
                e.target.checked ? this.addStyle(style) : this.deleteStyle(style)
            )
            styleList.appendChild(checkbox)
        })

        const deleteDom = dom('div', {html: '移除', class: 'remove'}, {
            event: 'click',
            fun: () => this.remove()
        })

        const parent = dom('div',{class:"avatar-form form"})
        content.append( createInputGroup(deform,round,rotate,onTop), styleList, type,
            createInputGroup('不透明度:',opacity)
            , createInputGroup(dom('div',{class:'separate'}),deleteDom))
        parent.append(content)
        this.dom = parent
        return parent
    }

    /** @return { AvatarDTO } */
    get DTO() {
        return {
            type: this.type,
            posType: this.#deform ? 'DEFORM' : 'ZOOM',
            pos: this.#frameLength <= 1 ? this.posList.flat(1) : this.posList,
            style: [...this.#styleSet],
            opacity: this.#opacity,
            rotate: this.#rotate,
            round: this.#round,
            avatarOnTop: this.#onTop
        }
    }
}
