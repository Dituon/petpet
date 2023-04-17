import {fabric} from "fabric"

//二值化滤镜
export default function (){
    fabric.Image.filters.Binarization = fabric.util.createClass(fabric.Image.filters.BaseFilter, {
        type: 'Binarization',
        fragmentSource: `
        precision highp float;
        uniform sampler2D uTexture;
        varying vec2 vTexCoord;
        void main() {
            vec4 color = texture2D(uTexture, vTexCoord);
            float average = (color.r + color.b + color.g) / 3.0;
            if (average > 0.5) {
                gl_FragColor = vec4(1.0, 1.0, 1.0, color.a);
            } else {
                gl_FragColor = vec4(0.0, 0.0, 0.0, color.a);
            }
        }`,
        applyTo2d: function (options) {
            let imageData = options.imageData,
                data = imageData.data,
                i = 0,
                len = data.length,
                value

            for (; i < len; i += 4) {
                let r = data[i]
                let g = data[i + 1]
                let b = data[i + 2]
                const sum = (r + g + b) / 3
                if (sum < 255 / 2) {
                    value = 255
                } else {
                    value = 0
                }
                data[i] = value
                data[i + 1] = value
                data[i + 2] = value
            }
        }
    })
    fabric.Image.filters.Binarization.fromObject = fabric.Image.filters.BaseFilter.fromObject
}
