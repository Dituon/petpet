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
 * fetch Image by POST
 * @param { PetRequestDTO } param
 * @return { Promise<Blob> } Image Blob
 */
const fetchImage = async param => {
    if (!param.key) throw new Error('Param Key undefined')
    param.randomAvatarList = param.randomAvatarList ?? []
    param.textList = param.textList ?? []

    return fetch('http://127.0.0.1:2333/petpet', {
        method: 'post',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(param)
    }).then(p => p.blob())
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

fetchImage(requestParam).then(imageBlob => {
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