import {dom} from "../ui/index.js";
import './uploader.css'
import AvatarItem from "./avatar-item.js";

const TYPES = ['FROM', 'TO', 'BOT', 'GROUP']
const typeNameMap = new Map(TYPES.map(t => [t, t.toLowerCase() + 'Avatar']))

export default class {
    static types = TYPES
    /** @type {HTMLDivElement} */
    #element
    /** @type {Map<string, AvatarItem>} */
    #itemMap
    /** @type {AvatarItem[]} */
    #frameItems

    constructor() {
        this.#element = dom('div', {id: 'avatar-uploader'})
        this.#itemMap = new Map(TYPES.map(t => [t, new AvatarItem(t)]))
        this.#itemMap.values()

        this.types = null
    }

    /** @param {AvatarType[] | null} types */
    set types(types) {
        if (!types || types.length === 0) {
            this.#element.innerHTML = '无头像参数'
            return
        }
        this.#element.innerHTML = ''
        this.#frameItems = types.map(t => this.#itemMap.get(t))
        this.#element.append(...this.#frameItems.map(i => i.dom))
    }

    get dom() {
        const root = dom()
        root.append(
            dom('h3', {html: 'Step 2: 上传图片'}),
            this.#element
        )
        return root
    }

    get ready(){
        return this.#frameItems.some(i => i.file)
    }

    set onchange(callback) {
        const itemCallback = () => this.ready && callback(this);
        [...this.#itemMap.values()].forEach(i => i.onchange = itemCallback)
    }

    get data() {
        return this.#frameItems.map(i => ({
            name: typeNameMap.get(i.type),
            file: i.file
        }))
    }
}