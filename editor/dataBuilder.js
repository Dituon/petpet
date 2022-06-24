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

    let out = `{
    "type": "${imageType}",
    "avatar": [${avatarBuiltList}],
    "text": [${textBuiltList}]\n}`

    $('textarea').slideDown().text(out.toString())
}