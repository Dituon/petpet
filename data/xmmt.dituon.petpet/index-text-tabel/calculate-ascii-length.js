const sizes = require("./ntqq-ascii-size.json")

let baseWidth = sizes[" "]
const arr = new Array(95)
for (const char in sizes) {
    let codePoint = char.codePointAt(0)
    let width = sizes[char] / baseWidth
    arr[codePoint - 32] = Math.round(width * 1000) / 1000
}
console.log(arr)