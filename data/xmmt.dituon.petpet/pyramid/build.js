const fs = require('fs');
const { cos, sin, tan, sqrt, PI } = Math;

/*
 * 部分代码参考自
 * https://github.com/MemeCrafters/meme-generator/blob/0aa5d2da98da9fd3441f896d28098ca4ef148677/meme_generator/memes/pyramid/__init__.py
 * MIT License
 */

function pyramid() {
    const imgW = 300;
    const imgH = 300;
    const fov = 45 * PI / 180;
    const z = sqrt(imgW ** 2 + imgH ** 2) / 2 / tan(fov / 2);
    const a = 180;
    const h = 180;
    const rh = Math.floor(sqrt(a ** 2 + h ** 2));

    function rotateY(theta) {
        const mat = [
            [cos(theta), 0, sin(theta)],
            [0, 1, 0],
            [-sin(theta), 0, cos(theta)]
        ];

        const orgs = [
            [-a / 2, -h / 2, 0],
            [a / 2, -h / 2, 0],
            [a / 2, h / 2, a / 2],
            [-a / 2, h / 2, a / 2]
        ];

        const dsts = orgs.map(org => {
            const dst = [
                mat[0][0] * org[0] + mat[0][1] * org[1] + mat[0][2] * org[2],
                mat[1][0] * org[0] + mat[1][1] * org[1] + mat[1][2] * org[2],
                mat[2][0] * org[0] + mat[2][1] * org[1] + mat[2][2] * org[2]
            ];
            return [
                Math.floor(dst[0] * z / (z - dst[2])),
                Math.floor(dst[1] * z / (z - dst[2]))
            ];
        });

        const minX = Math.min(...dsts.map(d => d[0]));
        const minY = Math.min(...dsts.map(d => d[1]));

        dsts.forEach(dst => {
            dst[0] -= minX;
            dst[1] -= minY;
        });

        const xOffset = Math.floor(imgW / 2 + minX);
        const yOffset = Math.floor(imgH / 2 + minY);

        dsts.forEach(dst => {
            dst[0] += xOffset;
            dst[1] += yOffset;
        });

        const [lt, rt, rb, lb] = dsts;
        return [lt, lb, rb, rt, [0, 0]];
    }

    const frameNumPerImage = 15;
    const frameNum = 4 * frameNumPerImage;
    const thetaStep = 90 / frameNumPerImage;

    const outs = [[], [], [], []];
    for (let i = 0; i < frameNum; i++) {
        const imgIdx1 = Math.floor(i / frameNumPerImage);
        const imgIdx2 = (imgIdx1 + 1) % 4;
        const theta1 = (i % frameNumPerImage) * thetaStep;
        const theta2 = theta1 - 90;

        const pos1 = rotateY(theta1 * PI / 180);
        const pos2 = rotateY(theta2 * PI / 180);

        outs[imgIdx1][i] = pos1;
        outs[imgIdx2][i] = pos2;
    }

    return outs;
}

function main() {
    const out = pyramid();
    const crop = [[['50%', 0], [0, '100%'], ['100%', '100%']]];

    const result = JSON.stringify({
        type: 'gif',
        metadata: {
            apiVersion: 101,
            alias: ['四棱锥', '金字塔']
        },
        elements: [
            {
                type: 'image',
                key: 'to',
                crop: crop,
                coords: out[0]
            },
            {
                type: 'image',
                key: 'from',
                crop: crop,
                coords: out[1]
            },
            {
                type: 'image',
                key: 'to',
                crop: crop,
                coords: out[2]
            },
            {
                type: 'image',
                key: 'from',
                crop: crop,
                coords: out[3]
            }
        ],
        canvas: {
            width: 300,
            height: 300,
            length: 15
        },
        delay: 65
    }, null, 0);

    fs.writeFileSync('template.json', result);
}

main();
