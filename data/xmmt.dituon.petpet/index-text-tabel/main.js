/**
 * tabel: 表格模式, 对尾部进行排版与填充
 * list:  列表模式, 不填充尾部
 * @type { "tabel" | "list" }
 */
const layoutType = "list"

/**
 * normal: 不处理ascii字符
 * math:   会将ascii字符替换为等宽数学字符(U+1D670), 例如 𝚖𝚢-𝚙𝚎𝚝𝚙𝚎𝚝
 * @type { "normal" | "math" }
 */
const textStyle = "normal"

/**
 * slilt: 对于溢出的新行在同列换行
 * ```text
 * | right_symm | 对称, 右对称 |
 * | etry       | , 左右对称   |
 * ```
 * syaggered: 交错换行, 目前仅支持两列
 * ```text
 * | right_symmetry          |
 * |    对称, 右对称, 左右对称 |
 * ```
 * @type { "split" | "syaggered" }
 */
const breakStyle = 'syaggered'

// 连接别名的分隔符
const aliaDelimiter = ', '

// 表格左侧长度
const tabelLeftLength = 16
// 表格左侧长度
const tabelRightLength = 22
// 表格前缀
const linePerfix = '│ '
// 表格分隔符
const lineDelimiter = ' │ '
// 表格后缀
const lineSuffix = ' │'
// 表格溢出新行前缀 (参与到长度计算, 请确保兼容性)
const overflowNewLinePrefix = "- "

// 列表前缀
const listPerfix = '> '
// 列表左侧长度
const listHeadLength = 22

// 换行符
const newLine = '\n'

// 分组数量
const groupCount = 4
// 分组前缀
const groupPrefix = '模板范围 '
const otherGroupPrefix = '其他模板: '

// ==== 以下为脚本固定常量与正文，请勿随意修改，除非您完全了解其作用 ====

// 分组范围
const groupStartCode = 97
const groupEndCode = 122
const groupRangeSize = Math.ceil((groupEndCode - groupStartCode + 1) / groupCount)

// see calculate-ascii-length.json
const asciiSizeMap = [1, 1.054, 1.47, 2.154, 1.979, 3.003, 2.937, 0.867, 1.13, 1.13, 1.536, 2.503, 0.813, 1.461, 0.813, 1.443, 1.979, 1.979, 1.979, 1.979, 1.979, 1.979, 1.979, 1.979, 1.979, 1.979, 0.813, 0.813, 2.503, 2.503, 2.503, 1.63, 3.479, 2.377, 2.117, 2.259, 2.572, 1.855, 1.792, 2.509, 2.611, 0.994, 1.337, 2.142, 1.732, 3.298, 2.744, 2.75, 2.066, 2.75, 2.205, 1.949, 1.937, 2.521, 2.283, 3.434, 2.178, 2.036, 2.093, 1.13, 1.404, 1.13, 2.503, 1.515, 0.997, 1.867, 2.157, 1.693, 2.16, 1.916, 1.172, 2.16, 2.078, 0.901, 0.904, 1.837, 0.901, 3.163, 2.081, 2.148, 2.157, 2.16, 1.289, 1.575, 1.259, 2.081, 1.771, 2.666, 1.711, 1.786, 1.66, 1.13, 0.91, 1.13, 2.503]
const emojiSize = 4.753
const cjkSize = 3.373
const mathFontSize = 2.12
let indexTexts

const thisId = register(info => {
    return {
        hidden: true,
        inRandomList: false,
        defaultTemplateWeight: 100
    }
})

on("load", e => {
    if (thisId !== e.defaultTemplate) return
    indexTexts = buildIndexString(e.templates)
})

on("bot_send", e => {
    e.responseInForward(true)
    for (const indexText of indexTexts) {
        e.response(indexText)
        e.responseNewParagraph()
    }
})

/**
 * @param { PetpetTemplateInfo[] } templates 
 * @returns {string[]}
 */
function buildIndexString(templates) {
    let resultArr = new Array(groupCount).fill('')
    let other = ''

    for (const template of templates) {
        const metadata = template.metadata
        if (!metadata || metadata.hidden) {
            continue
        }

        const alias = metadata.alias || []
        const aliasStr = alias.length === 0 ? '-' : join(alias, aliaDelimiter)

        const firstCodePoint = template.id[0].toLowerCase().codePointAt(0)
        const group = Math.floor((firstCodePoint - groupStartCode) / groupRangeSize)
        let line
        switch (layoutType) {
            case "tabel":
                line = tabelLine([item(template.id, tabelLeftLength), item(aliasStr, tabelRightLength)]) + newLine
                break
            case "list":
                line = listPerfix
                    + padRight(template.id, listHeadLength, 0, true).content
                    + ' ' + aliasStr + newLine
                break
        }
        if (group < 0) {
            other += line
        } else {
            resultArr[group] += line
        }
    }

    resultArr = resultArr.filter(s => s.length > 0)
        .map((s, i) => {
            const start = i * groupRangeSize + groupStartCode
            return groupPrefix + String.fromCodePoint(start - 32)
                + ' - '
                + String.fromCodePoint(Math.min(groupEndCode, start + groupRangeSize - 1) - 32)
                + ': ' + newLine + s
        })
    if (other) {
        resultArr.push(otherGroupPrefix + other)
    }
    return resultArr
}

function tabelLine(items) {
    switch (breakStyle) {
        case "split": return lineSplit(items)
        case "syaggered": return lineSyaggered(items)
        default: throw Error("breakStyle 变量参数错误")
    }
}

function lineSyaggered(items) {
    if (items.length !== 2) {
        throw Error("syaggered break item length must == 2");
    }

    let result = '';
    const padder0 = padRight(items[0][0], items[0][1]);
    const padder1 = padLeft(items[1][0], items[1][1], padder0.offset);

    let lineLen = items[1][1];
    if (padder0.overflow || padder1.overflow) {
        lineLen = items[0][1] + items[1][1] + lineDelimiter.length;
        result += linePerfix + syaggerePadder(items[0][0], lineLen, 0, true) + lineSuffix + newLine + linePerfix;
    } else {
        result += linePerfix + padder0.content + lineDelimiter;
    }

    if (padder1.overflow) {
        const prefix = padder0.overflow ? '' : overflowNewLinePrefix;
        result += syaggerePadder(prefix + items[1][0], lineLen, padder0.overflow ? 0 : padder0.offset, false) + lineSuffix;
    } else {
        if (padder0.overflow) {
            result += syaggerePadder(overflowNewLinePrefix + items[1][0], lineLen, 0, false) + lineSuffix;
        } else {
            result += padder1.content + lineSuffix;
        }
    }

    return result;
}

function syaggerePadder(str, len, offset = 0, isLeft = true) {
    let padFun = isLeft ? padRight : padLeft
    let padder = padFun(str, len, offset)
    let result = padder.content
    if (padder.overflow) {
        result += newLine + linePerfix
            + syaggerePadder(padder.overflow, len, padder.offset, isLeft)
            + lineSuffix
    }
    return result
}

function lineSplit(items) {
    let result = linePerfix
    let offset = 0
    let overflows = []
    for (let i = 0; i < items.length; i++) {
        const item = items[i]
        const padder = padRight(item[0], item[1], offset)
        offset = padder.offset
        if (padder.overflow) overflows[i] = padder.overflow
        result += padder.content

        if (i != items.length - 1) {
            result += lineDelimiter
        }
    }
    result += lineSuffix
    if (overflows.length > 0) {
        const overflowItems = items.map((it, i) => item(overflows[i] || ' ', it[1]))
        result += newLine + lineSplit(overflowItems)
    }
    return result
}

function item(str, length) {
    return [str, length]
}

function prePad(str, length) {
    if (textStyle === "math") {
        str = convertToMathCharacters(str)
    }

    let charIndex = 0
    let displayLen = 0
    let emojis = ''
    const chars = Array.from(str)
    for (let i = 0; i < chars.length; i++) {
        const codePoint = chars[i].codePointAt(0)
        let add = 0
        if (isMathCaracter(codePoint)) { // 因为默认替换的原因, 数学字符命中率最高
            if (emojis) {
                add += emojiLen(emojis) * emojiSize
                emojis = ''
            }
            add += mathFontSize // Full-width Math characters
        } else if (isEmoji(codePoint)) {
            emojis += String.fromCodePoint(codePoint)
        } else {
            if (codePoint >= 0x0020 && codePoint <= 0x7F) {
                add = asciiSizeMap[codePoint - 32] || 0 // ASCII characters
            } else if (codePoint >= 0x80 && codePoint <= 0x1FFF) {
                add = 1 // Half-width characters
            } else if (codePoint >= 0x2000 && codePoint <= 0xFF60) {
                add = cjkSize // Full-width CJK characters
            } else if (codePoint >= 0xFF61 && codePoint <= 0xFF9F) {
                add = 1 // Half-width katakana
            } else if (codePoint >= 0xFFA0) {
                add = 2 // Full-width characters
            }
            if (emojis) {
                add += emojiLen(emojis) * emojiSize
                emojis = ''
            }
        }
        if (i === chars.length - 1 && emojis) {
            add += emojiLen(emojis) * emojiSize
            emojis = ''
        }
        if (Math.round(displayLen) + add > length) {
            break
        }
        displayLen += add
        charIndex++
    }
    return { str, displayLen, chars, charIndex }
}

function padLeft(str, length, offset = 0) {
    const p = prePad(str, length)
    str = p.str
    const resultLen = Math.round(p.displayLen + offset)
    const spaces = ' '.repeat(Math.max(length - resultLen, 0))
    if (p.chars.length <= p.charIndex) {
        return {
            content: spaces + str,
            offset: p.displayLen - resultLen
        }
    } else {
        let result = p.chars.slice(0, p.charIndex).join('')
        return {
            content: spaces + str,
            offset: p.displayLen - resultLen,
            overflow: str.substring(result.length)
        }
    }
}

function padRight(str, length, offset = 0, allowOverflow = false) {
    const p = prePad(str, length)
    str = p.str
    const resultLen = Math.round(p.displayLen + offset)
    const spaces = ' '.repeat(Math.max(length - resultLen, 0))
    if (p.chars.length <= p.charIndex || allowOverflow) {
        return {
            content: str + spaces,
            offset: p.displayLen - resultLen
        }
    } else {
        let result = p.chars.slice(0, p.charIndex).join('')
        return {
            content: result + spaces,
            offset: p.displayLen - resultLen,
            overflow: str.substring(result.length)
        }
    }
}

// by https://blog.jonnew.com/posts/poo-dot-length-equals-two
function emojiLen(str) {
    const joiner = "\u200D"
    const split = str.split(joiner)
    let count = 0

    for (const s of split) {
        let num = 0
        let filtered = ""
        for (let i = 0; i < s.length; i++) {
            const charCode = s.charCodeAt(i)
            if (charCode < 0xFE00 || charCode > 0xFE0F) {
                filtered += s[i]
            }
        }
        num = Array.from(filtered).length
        count += num
    }
    return Math.ceil(count / split.length)
}


function isEmoji(code) {
    return (
        (code === 0x200D) ||
        (code >= 0x1F600 && code <= 0x1F64F) || // Emoticons
        (code >= 0x1F300 && code <= 0x1F5FF) || // Misc Symbols and Pictographs
        (code >= 0x1F680 && code <= 0x1F6FF) || // Transport and Map
        (code >= 0x1F1E6 && code <= 0x1F1FF) || // Regional country flags
        (code >= 0x2600 && code <= 0x26FF) ||   // Misc symbols
        (code >= 0x2700 && code <= 0x27BF) ||   // Dingbats
        (code >= 0xE0020 && code <= 0xE007F) || // Tags
        (code >= 0xFE00 && code <= 0xFE0F) ||   // Variation Selectors
        (code >= 0x1F900 && code <= 0x1F9FF) || // Supplemental Symbols and Pictographs
        (code >= 0x1F018 && code <= 0x1F270) || // Various Asian characters
        (code >= 0x238C && code <= 0x2454) ||   // Misc items
        (code >= 0x20D0 && code <= 0x20FF)      // Combining Diacritical Marks for Symbols
    )
}

function isMathCaracter(code) {
    return (code >= 0x1D670 && code <= 0x1D6A3) || // A - z
        (code >= 0x1D7F6 && code <= 0x1D7FF) // 0 - 9
}

function convertToMathCharacters(input) {
    let result = ''
    for (const ch of input) {
        const code = ch.charCodeAt(0)
        if (code >= 65 && code <= 90) {
            result += String.fromCodePoint(0x1D670 + (code - 65))
        } else if (code >= 97 && code <= 122) {
            result += String.fromCodePoint(0x1D68A + (code - 97))
        } else if (code >= 48 && code <= 57) {
            result += String.fromCodePoint(0x1D7F6 + (code - 48))
        } else {
            result += ch
        }
    }
    return result
}

// ==== 以下内容为 Nashorn 兼容性方案，请勿将其视为无用代码而删除，除非您确认此脚本可在指定引擎上正常运行 ====

function join(array, separator) {
    if (array.join) {
        return array.join(separator)
    }
    let result = ''
    for (let i = 0; i < array.length; i++) {
        if (i > 0) {
            result += separator
        }
        result += array[i]
    }
    return result
}

/**
 * https://github.com/efwGrp/nashorn-ext-for-es6
 * The String.fromCodePoint() static method returns a string created from the specified sequence of code points.
 * ECMAScript 2015
*/
if (!String.fromCodePoint) {
    String.fromCodePoint = function fromCodePoint() {
        var chars = [], point, offset, i
        var length = arguments.length
        for (i = 0; i < length; ++i) {
            point = arguments[i]
            if (point < 0x10000) {
                chars.push(point)
            } else {
                offset = point - 0x10000
                chars.push(0xD800 + (offset >> 10))
                chars.push(0xDC00 + (offset & 0x3FF))
            }
        }
        return String.fromCharCode.apply(null, chars)
    }
    Object.defineProperty(String, "fromCodePoint", { enumerable: false })
}

if (!Array.from) {
    Array.from = function (r, u, e) {
        let l = []
        for (let n of r) null != u ? null != e ? l.push(u.call(e, n)) : l.push(u(n)) : l.push(n)
        return l
    }
    Object.defineProperty(Array, "from", { enumerable: false })
}

if (!Array.prototype.fill) {
    Array.prototype.fill = function (value, start = 0, end = this.length) {
        const length = this.length
        let startIndex = Math.max(0, start < 0 ? length + start : start)
        let endIndex = Math.min(length, end < 0 ? length + end : end)
        for (let i = startIndex; i < endIndex; i++) {
            this[i] = value
        }
        return this
    }
}
