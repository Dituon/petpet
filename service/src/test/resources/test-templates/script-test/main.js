register(info => {
    return {
        hidden: true,
        inRandomList: false,
        isDefaultTemplate: true
    }
})

on("load", e => {
    log.info(`已加载 ${e.templates.length} 模板`)
})
