const fs = require('fs')
const path = require('path')

const config = {
  path: "./templates",
  outputBase: "./",
  targetVersion: 6.2,
  buildIndexMap: true,
}

const fontsPathName = 'fonts'
const rootDir = process.cwd()

function listDirectoriesSync(dir) {
  const dirents = fs.readdirSync(dir, { withFileTypes: true })
  return dirents
    .filter((dirent) => dirent.isDirectory() && dirent.name !== fontsPathName)
    .map((dirent) => dirent.name)
}

function listFilesSync(dir) {
  const dirents = fs.readdirSync(dir, { withFileTypes: true })
  return dirents
    .filter((dirent) => dirent.isFile() && dirent.name.endsWith('.png'))
    .map((dirent) => dirent.name)
}

function readJsonFileSync(filePath) {
  try {
    const content = fs.readFileSync(filePath, 'utf8')
    return JSON.parse(content)
  } catch (err) {
    return {}
  }
}

function buildDataIndexSync(dataPath) {
  const dataSubDirs = listDirectoriesSync(path.join(rootDir, dataPath))
  const dataTemplateNames = dataSubDirs

  let fontsNames = []
  const fontsDirPath = path.join(rootDir, dataPath, fontsPathName)
  try {
    fs.accessSync(fontsDirPath)
    fontsNames = fs.readdirSync(fontsDirPath)
  } catch (e) { }

  const jsonData = {
    version: config.targetVersion,
    dataPath,
    dataList: dataTemplateNames,
    fontList: fontsNames,
  }

  fs.writeFileSync(path.join(rootDir, config.outputBase, 'index.json'), JSON.stringify(jsonData, null, 4))

  if (!config.buildIndexMap) return

  const lengthIndex = {}
  const aliasIndex = {}
  const typeIndex = {}

  for (const dir of dataSubDirs) {
    if (dir !== fontsPathName) {
      const dirPath = path.join(rootDir, dataPath, dir)
      const files = listFilesSync(dirPath)
      lengthIndex[dir] = files.length

      const dataJsonFile = path.join(rootDir, dataPath, dir, 'data.json')
      const dataJson = readJsonFileSync(dataJsonFile)
      aliasIndex[dir] = dataJson.alias || []
      typeIndex[dir] = dataJson.type || 'Unknown'
    }
  }

  const indexMapJsonData = JSON.stringify({
    length: lengthIndex,
    alias: aliasIndex,
    type: typeIndex,
  })

  fs.writeFileSync(path.join(rootDir, config.outputBase, 'index.map.json'), indexMapJsonData)
}

try {
  buildDataIndexSync(config.path)
} catch (err) {
  console.error('An error occurred:', err)
}