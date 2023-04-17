/** @abstract */
export class Model{
    /** @type { fabric.Canvas } */
    canvas
    /** @type { Function }*/
    listener = m => {}
    /** @type { Function }*/
    removeCallback = m => {}
    /**
     *  @type { HTMLDivElement }
     *  @protected
     */
    dom

    /** @param { fabric.Canvas } canvas */
    constructor(canvas) {
        this.canvas = canvas
    }
}