function buildData() {
    let avatarBuiltList = []
    for (const avatar of avatarList) {
        if (avatar.isDelete) continue
        avatarBuiltList.push(avatar.build())
    }

    let textBuiltList = []
    for (const text of textList) {
        if (text.isDelete) continue
        textBuiltList.push(text.build())
    }

    let alias = [];
    $('#alias').val().trim().split(' ').forEach(alia => {
        if (alia) alias.push('"' + alia + '"')
    })
    const extra = !alias.length ? '' : `,\n    "alias": [${alias}]`

    let out = `{
    "type": "${imageType}",
    "avatar": [${avatarBuiltList}],
    "text": [${textBuiltList}]${extra}\n}`

    $('textarea').slideDown().text(out)
    $('#downloadConfig').slideDown()
}

$('#downloadConfig').click(() => {
    let blob = new Blob([$('textarea').text()], {
        type: 'text/plain;charset=utf-8'
    });
    saveAs(blob, 'data.json')
})