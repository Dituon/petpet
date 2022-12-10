/**
 * @typedef { {name: string, avatar: string | URL} } TargetDTO
 */

/**
 * @typedef { object } PetRequestDTO
 * @property { string } key
 * @property { TargetDTO } [from]
 * @property { TargetDTO } [to]
 * @property { TargetDTO } [bot]
 * @property { TargetDTO } [group]
 * @property { string[] | URL[] } randomAvatarList
 * @property { string[] } textList
 */

/**
 * fetch Image by GET
 * @param { PetRequestDTO } param
 * @return { Promise<Blob> } Image Blob
 */
const getImage = async param => {
    if (!param.key) throw new Error('Param Key undefined')
    let {key, randomAvatarList, textList, ...targets} = param

    const urlParam = new URLSearchParams()
    urlParam.append('key', key)
    !randomAvatarList || urlParam.append('randomAvatarList', randomAvatarList)
    !textList || urlParam.append('textList', textList)
    
    Object.entries(targets).forEach(target => {
        let [targetKey, targetObj] = target
        let {name, avatar} = targetObj
        urlParam.append(`${targetKey}Name`, name)
        urlParam.append(`${targetKey}Avatar`, avatar)
    })

    let url = new URL('http://127.0.0.1:2333/petpet')
    url.search = urlParam

    return fetch(url).then(p => p.blob())
}

// 👇示例脚本

/** @type { PetRequestDTO } */
const requestParam = {
    key: 'petpet',
    to: {
        name: 'Dituon',
        avatar: 'https://q1.qlogo.cn/g?b=qq&nk=2544193782&s=640'
    },
    // randomAvatarList: [],
    // textList: []
}

getImage(requestParam).then(imageBlob => {
    // do something
    console.log('Hello, Petpet!')

    /*
    // DOM JS
    const image = new Image()
    image.src = URL.createObjectURL(imageBlob)
    document.body.appendChild(image)
    */

    /*
    // Node JS
    // ES Style
    // import fs from 'fs'

    // AMD Style
    const fs = require('fs')

    fs.writeFileSync('./petpet.gif', imageBlob)
    */
})