import {dom} from "../ui/index.js";
import './uploader.css'
import AvatarItem from "./avatar-item.js";
const TYPES = ['FROM', 'TO', 'BOT', 'GROUP']

export default class {
    static types = TYPES
    /** @type {HTMLDivElement} */
    #element
    /** @type {Map<string, AvatarItem>} */
    #itemMap
    constructor() {
        this.#element = dom('div', {id: 'avatar-uploader'})
        this.#itemMap = new Map(TYPES.map(t => [t, new AvatarItem(t)]))

        this.#element.append(...[...this.#itemMap.values()].map(i => i.dom))
    }

    /** @param {AvatarType[]} types */
    set types(types) {

    }

    get dom() {
        const root = dom()
        root.append(
            dom('h3', {html: 'Step 2: 上传图片'}),
            this.#element
        )
        return root
    }
}