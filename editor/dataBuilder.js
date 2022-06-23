function buildData() {
    let avatarBuiltList = []
    for (const avatar of avatarList) {
        if (avatar.isDelete) continue
        avatarBuiltList.push(avatar.build())
    }

    let out = `{
    "type": "${imageType}",
    "avatar": [${avatarBuiltList}],
    "text": []\n}`

    $('textarea').slideDown().text(out.toString())
}