package io.nuls.server;

import java.math.BigInteger;

public class ServerContext {

    public static String DEFAULT_ENCODING = "UTF-8";
    /**
     * 本链id
     */
    public static int chain_id = 1;
    /**
     * 访问钱包的http接口url地址
     */
    public static String wallet_url = "http://127.0.0.1:18004/";
    /**
     * 本地存储路径
     */
    public static String dataPath = "./data";

}
