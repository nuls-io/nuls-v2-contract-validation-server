package io.nuls.server;

import java.math.BigInteger;

public class ServerContext {

    public static String DEFAULT_ENCODING = "UTF-8";
    /**
     * 本链id
     */
    public static int main_chain_id = 1;
    /**
     * 本链主资产id
     */
    public static int main_asset_id = 1;
    /**
     * NULS主网链id
     */
    public static int nuls_chain_id = 1;
    /**
     * NULS主资产id
     */
    public static int nuls_asset_id = 1;
    /**
     * 访问钱包的http接口url地址
     */
    public static String wallet_url = "http://127.0.0.1:9898/";
    /**
     * 本地存储路径
     */
    public static String dataPath = "./data";

}
