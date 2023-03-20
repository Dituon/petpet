const fs = require('fs')

const PATH = './'

const fileList = fs.readdirSync(PATH)
const length = fileList.length
const data = JSON.parse(fs.readFileSync('./data.json', 'utf8'))

for (let i = 1; i < length; i+= 2){
    try {
        data.avatar[0].pos[i] = undefined
        fs.rmSync(PATH + i + '.png')
    } catch (e) {}
}

let ni = 0
for (let i = 0; i < length; i+= 2){
    try {
    fs.renameSync(PATH + i + '.png', PATH + ni++ + '.png')
    } catch (e) {}
}

console.log(JSON.stringify(data.avatar[0].pos.filter(p => p !== undefined)))