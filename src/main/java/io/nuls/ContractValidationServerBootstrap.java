/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls;

import io.nuls.contract.constant.ContractConstant;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.parse.ConfigLoader;
import io.nuls.core.rockdb.service.RocksDBService;
import io.nuls.model.jsonrpc.RpcResult;
import io.nuls.server.RpcServerManager;
import io.nuls.server.ServerContext;
import io.nuls.server.utils.JsonRpcUtil;
import io.nuls.server.utils.ListUtil;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Properties;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author: PierreLuo
 * @date: 2019-07-10
 */
public class ContractValidationServerBootstrap {

    static String serverIP;
    static int serverPort;

    public static void main(String[] args) throws Exception {
        systemConfig();
        initConfig();
        SpringLiteContext.init("io.nuls");
        initDB();
        RpcServerManager.getInstance().startServer(serverIP, serverPort);
    }

    static void initConfig() throws IOException {
        Properties properties = ConfigLoader.loadProperties("cfg.properties");
        serverIP = properties.getProperty("contract.server.ip");
        serverPort = Integer.parseInt(properties.getProperty("contract.server.port"));
        ServerContext.dataPath = properties.getProperty("db.path");
        String sdkProviderIp = properties.getProperty("sdk.provider.ip");
        int sdkProviderPort = Integer.parseInt(properties.getProperty("sdk.provider.port"));
        ServerContext.wallet_url = String.format("http://%s:%s/", sdkProviderIp, sdkProviderPort);
        RpcResult info = JsonRpcUtil.request("info", ListUtil.of());
        Map result = (Map) info.getResult();
        Integer chainId = (Integer) result.get("chainId");
        ServerContext.chain_id = chainId != null ? chainId : ServerContext.chain_id;
    }

    static void initDB() throws Exception {
        RocksDBService.init(ServerContext.dataPath);
        // 合约地址表
        RocksDBService.createTable(ContractConstant.DB_NAME_CONTRACT_ADDRESS + "_" + ServerContext.chain_id);
    }

    /**
     * 初始化系统编码
     * Initialization System Coding
     */
    static void systemConfig() throws Exception {
        System.setProperty("protostuff.runtime.allow_null_array_element", "true");
        System.setProperty(ContractConstant.SYS_FILE_ENCODING, UTF_8.name());
        Field charset = Charset.class.getDeclaredField("defaultCharset");
        charset.setAccessible(true);
        charset.set(null, UTF_8);
    }
}
