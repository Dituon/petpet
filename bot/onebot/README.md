# Petpet Onebot

## 工作流程

由于 Onebot v11 协议不支持通过 HTTP form-data 上传图像, 仅能使用以下方式传递图像: 

- **网络地址**
- **本地文件**
- **Base64 编码的字符串**

为了兼顾性能和兼容性, Petpet Onebot 采用了本地 HTTP 图像服务器的方案。

客户端生成图像后, 会将图像的 UUID 和地址返回给 Onebot 协议端, 例如: 

- `http://localhost:2233/15f13dd2-87e1-4570-8717-e29891d4b40e`

如果客户端和协议端运行在不同的虚拟机或容器中, 则需要调整容器的网络配置或 Petpet 客户端的配置。

#### 示例配置

在 Docker 容器中访问主机上的 Petpet 服务时, 可以选择以下方法之一: 

- **修改容器配置**: 使用 `--net=host` 参数启动容器。
- **修改客户端配置**: 将 Petpet 配置中的 `httpServerUrl` 设置为以下地址之一: 
    - `http://host.docker.internal:2233/`
    - `http://172.17.0.1:2233` (Docker 默认网桥地址)

## 配置项

- **httpServerPort**: `2233`

  > Petpet Onebot HTTP 图像服务器端口

- **httpServerUrl**: `http://localhost:2233/`

  > 传递给 Onebot 协议端的图像服务器地址