# NULS智能合约源代码认证服务

## 依赖NULS2.0 API模块

在cfg.properties中有以下配置，可根据情况修改以下配置

```properties
# 配置 NULS-API 模块的ip和port
sdk.provider.ip=127.0.0.1
sdk.provider.port=18004
```

## 认证接口

### 认证合约源代码 - validateContractCode

## 查询接口

### 查询合约认证状态 - getContractAddressInfo

### 获取合约代码文件树 - getContractCodeTree

### 获取指定合约代码文件内容 - getContractCode

## 接口调试

提供Postman接口调式工具的导入文件(JSON-RPC)，导入后，即可调试接口

[JSON-PRC 接口调试-POSTMAN导入文件](https://github.com/nuls-io/nuls-v2-contract-validation-server/blob/master/documents/contract-validation-server_Postman_JSONPRC.json)

## Startup

### Linux 环境(仅支持)

> 前提：Linux服务器内需要安装`docker`

- 打包

      $ mvn clean -DskipTests=true package
    
- 解压

      $ cd target
      $ tar -xzf contract-validation-server.tar.gz
    
- 构建镜像 

      $ cd contract-validation-server
      $ ./make_image.sh
    
- 启动应用

      $ ./start_container.sh
    
    启动应用后，根据`cfg.properties`文件中的`contract.server.port`配置，将开放此配置端口的合约源代码验证服务，默认配置的服务端口是`15151`
    
- 关闭应用

      $ ./stop_container.sh