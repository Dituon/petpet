# Petpet Service

# Updater

从远程仓库自动更新模板与字体

## 配置项

* **enabled**: `true`

  > 是否启用模板更新器功能，默认为 `true`
  >
  > 设置为 `false` 时将不会进行任何模板或字体的自动更新操作

* **repositoryUrls**: `"https://raw.githubusercontent.com/Dituon/petpet-templates/main/"`

  > 模板和字体资源的远程仓库地址列表
  >
  > 支持多个仓库地址，按顺序尝试拉取资源，建议保留多个镜像以提高可用性
  > 
  > 会尝试读取目录下的 `petpet-index.json`, 后根据索引规则进行解析

* **excludeTemplate**: `[]`

  > 排除不参与自动更新的模板名称集合，默认为空集合
  >
  > 名称应与模板文件名一致（不含扩展名）

* **downloadThreadCount**: `8`

  > 并发下载线程数，默认为 `8`
  >
  > 可根据网络状况和系统资源进行调整，建议不低于 `2`

* **updateTemplateSavePath**: `null`

  > 模板下载保存路径，默认为 `null`（使用默认路径）
  >
  > 如需自定义路径，请设置为有效的文件夹路径字符串

* **updateFontSavePath**: `null`

  > 字体下载保存路径，默认为 `null`（使用默认路径）
  >
  > 如需自定义路径，请设置为有效的文件夹路径字符串

* **updateMode**: `FULL_CHECK`

  > 模板更新模式:
  >
  > * `FULL_UPDATE`: 检查所有资源并自动更新
  > * `FULL_CHECK`: 检查所有资源，仅自动添加新模板，已有模板如有更新将仅记录日志
  > * `NEW_ONLY_UPDATE`: 仅下载新增模板，不检查已有资源是否更新
  >
  > 默认值为 `FULL_CHECK`