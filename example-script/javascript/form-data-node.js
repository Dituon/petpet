//package.json
/*
{
  "type": "module",
  "dependencies": {
    "form-data": "^4.0.0",
    "node-fetch": "^3.3.0"
  }
}
*/

// 👇CJS
// const fs = require('fs');
// const fetch = (...args) => import('node-fetch').then(({default: fetch}) => fetch(...args));
// const FormData = require('form-data');

// 👇ESM
import fs from 'fs'
import fetch from 'node-fetch'
import FormData from 'form-data'

(async () => {
    const imgBlob = fs.readFileSync('../../editor/avatar.jpg')

    const formData = new FormData()
    formData.append('key', 'petpet')
    formData.append('toAvatar', imgBlob, 'image')
    const data = await fetch('http://localhost:2333/petpet', {
        body: formData,
        method: "post"
    })

    fs.writeFileSync('./result.gif', await data.buffer())
})()
