import {SuperGif} from "../util/libgif.min.js";

/**
 * 加载GIF
 * @param { HTMLImageElement } image
 * @return {Promise<HTMLImageElement[]>}
 */
export async function loadGif(image) {
    document.body.appendChild(image)
    let gif = new SuperGif({gif: image})
    await new Promise(resolve => gif.load(resolve))
    document.querySelector('.jsgif').remove()
    /** @type { HTMLImageElement[] } */
    const frameList = []
    for (let i = 0; i < gif.get_length(); i++) {
        gif.move_to(i)
        let frameImage = new Image()
        frameImage.src = gif.get_canvas().toDataURL('image/png', 1)
        frameList.push(frameImage)
    }
    return frameList
}