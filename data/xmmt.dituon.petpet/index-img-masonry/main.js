const thisId = register(info => {
    return {
        hidden: true,
        inRandomList: false,
        defaultTemplateWeight: 10
    }
})

const itemWidth = 360
const padding = 10
const iconWidth = itemWidth - padding * 2
const defaultIcon = "./no_image.jpg"
const cachePath = "./cache"

const col = 3
const row = 9

let previewCount = 0

on("load", e => {
    if (e.defaultTemplate !== thisId) return
    previewCount = createImagePreview(e.templates)
})

on("bot_send", e => {
    if (previewCount === 0) {
        e.response("正在生成模板预览中...请稍后再试")
        return
    }
    for (let i = 0; i < previewCount; i++) {
        e.responseImage(`${cachePath}/${i}.png`)
    }
})

/**
 * @param {PetpetTemplateInfo[]} templates
 * @return {number} generated image count
 */
function createImagePreview(templates) {
    let isUpdate = false
    // 为每个模板生成预览
    const cachedImages = []
    for (let template of templates) {
        let path = `${cachePath}/${template.id}.png`
        if (isFileExists(path)) {
            cachedImages.push(path)
            continue
        }

        if (!isUpdate) {
            log.info("正在生成 Petpet 预览图, 请耐心等待...")
            isUpdate = true
        }

        let i = 0
        const elements = [
            {
                type: 'image',
                id: "icon",
                src: template.preview || defaultIcon,
                coords: [padding, padding, iconWidth, 0]
            },
            {
                type: 'text',
                id: "title",
                text: template.id,
                coords: [padding, `icon_height+${padding}`],
                align: "left",
                baseline: "top",
                size: 36
            }
        ]
        for (let a of template.metadata.alias || []) {
            elements.push({
                type: 'text',
                text: a,
                id: `alia_${i++}`,
                coords: [
                    `${padding * i}${aliaXOffsetExpr(i - 1)}`,
                    `icon_height+${padding + 36}`
                ],
                align: "left",
                size: 24
            })
        }
        const img = generate({
            type: 'image',
            elements: elements,
            canvas: {
                width: itemWidth,
                height: `icon_height+${padding * 2 + 36 + 24}`,
                color: '#fff'
            }
        })

        img.save(path)
        cachedImages.push(path)
    }

    // 合成并排版预览图
    const batchSize = col * row;
    const cacheLen = Math.ceil(cachedImages.length / batchSize)
    let isCacheExists = false
    for (let i = 0; i < cacheLen; i++) {
        if (isFileExists(`${cachePath}/${i}.png`)) {
            isCacheExists = true
            break
        }
    }
    if (!isUpdate && isCacheExists) {
        return cacheLen
    }

    let fi = 0
    for (let batchStart = 0; batchStart < cachedImages.length; batchStart += batchSize) {
        const colHeightsExpr = []
        for (let i = 0; i < col; i++) {
            colHeightsExpr.push("0")
        }
        const batchElements = [];

        const batchImages = cachedImages.slice(batchStart, batchStart + batchSize);

        for (let index = 0; index < batchImages.length; index++) {
            let src = batchImages[index];
            const colIndex = index % col
            const yOffsetExpr = colHeightsExpr[colIndex]
            const x = colIndex * itemWidth
            const heightVariable = `element_${index}_height`
            colHeightsExpr[colIndex] = `${yOffsetExpr}+${heightVariable}`

            batchElements.push({
                type: 'image',
                src: src,
                coords: [x, yOffsetExpr, itemWidth, '100ch']
            })
        }
        const img = generate({
            type: 'image',
            elements: batchElements,
            canvas: {
                width: itemWidth * col,
                height: createMaxExpression(colHeightsExpr.slice(-col)),
            }
        })
        let path = `${cachePath}/${fi++}.png`
        img.save(path)
    }

    if (isUpdate) {
        log.info("Petpet 预览图生成完毕")
    }

    return fi
}

function aliaXOffsetExpr(index) {
    let offsetExpr = ""
    for (let j = 0; j < index; j++) {
        offsetExpr += `+alia_${j}_width`
    }
    return offsetExpr
}

function createMaxExpression(arr) {
    if (arr.length === 1) return arr[0]
    return `max(${arr[0]}, ${createMaxExpression(arr.slice(1))})`;
}
