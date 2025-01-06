const fs = require('fs')
const path = require('path')

const config = {
  path: "./templates",
  outputBase: "./",
  fontsPath: './templates/fonts',
  targetVersion: 6.2
}

const rootDir = process.cwd()

const dataPath = path.join(rootDir, config.path)
const fontsPath = path.join(rootDir, config.fontsPath)
const outputPath = path.join(rootDir, config.outputBase)

const lengthMap = {}
const aliasMap = {}
const typeMap = {}

const oldTemplatesList = fs.readdirSync(dataPath)
  .filter(template => {
    const templatePath = path.join(dataPath, template)
    const filePath = path.join(templatePath, 'data.json')
    if (!fs.existsSync(filePath)) {
      return false
    }
    lengthMap[template] = fs.readdirSync(templatePath)
      .filter(file => file.match(/\d\.png/))
      .length
    const templateData = require(filePath)
    aliasMap[template] = templateData.alias ?? []
    typeMap[template] = templateData.type ?? "Unknown"
    return true
  })

const fontList = fs.readdirSync(fontsPath)
  .filter(font => font.match(/.+\.(woff|eot|woff2|ttf|svg)/))

fs.writeFileSync(path.join(outputPath, 'index.json'), JSON.stringify({
  version: config.targetVersion,
  dataPath: config.path,  
  dataList: oldTemplatesList,
  fontList: fontList
}, null, 2))

fs.writeFileSync(path.join(outputPath, 'index.map.json'), JSON.stringify({
  length: lengthMap,
  alias: aliasMap,
  type: typeMap
}))
