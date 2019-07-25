# NULS智能合约源代码认证服务

## 依赖NULS2.0 SDK-Provider模块

## 认证接口

### 认证合约源代码 - validateContractCode

## 查询接口

### 获取合约代码文件树 - getContractCodeTree

### 获取指定合约代码文件内容 - getContractCode

## Startup

### Linux 环境(仅支持)

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
    
- 关闭应用

      $ ./stop_container.sh