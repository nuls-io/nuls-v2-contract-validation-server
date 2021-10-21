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
package io.nuls.loadHexCode;

import io.nuls.base.basic.AddressTool;
import io.nuls.core.constant.BaseConstant;
import io.nuls.core.parse.JSONUtils;
import io.nuls.model.jsonrpc.RpcResult;
import io.nuls.server.ServerContext;
import io.nuls.server.utils.JsonRpcUtil;
import io.nuls.server.utils.ListUtil;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author: PierreLuo
 * @date: 2019-02-28
 */
public class LoadHexCode2JarTest {

    @Before
    public void before() {
        ServerContext.chain_id = 2;
        ServerContext.wallet_url = "https://api.nuls.io/";
    }

    @Test
    public void test() throws IOException {
        //String hexFile = "old_package_nrc20.txt";
        //String hexFile = "bbai.txt";
        //String hexName = "nrc20_bug";
        String hexName = "lptoken";
        String hexFile = hexName+ ".txt";
        String s = IOUtils.resourceToString(hexFile, StandardCharsets.UTF_8, this.getClass().getClassLoader());
        byte[] bytes = Hex.decode(s);
        OutputStream out = new FileOutputStream(new File("/Users/pierreluo/IdeaProjects/contract-validation-server/src/test/resources/"+hexName+".jar"));
        IOUtils.write(bytes, out);
        out.close();
    }


    /**
     * 获取节点中是多签地址的
     */
    @Test
    public void test1() throws IOException {
        String hexFile = "cnode.txt";
        String s = IOUtils.resourceToString(hexFile, StandardCharsets.UTF_8, this.getClass().getClassLoader());
        Map<String, Object> map = JSONUtils.json2map(s);
        List<Map> list = (List<Map>) ((Map)map.get("result")).get("list");
        List<String> addressList = new ArrayList<>();
        list.stream().forEach(m -> {
            //System.out.println(m.get("agentAddress"));
            addressList.add(m.get("agentAddress").toString());
        });
        addressList.stream().forEach(address -> {
            if(isMultiSignAddress(AddressTool.getAddress(address))) {
                System.out.println("多签地址: " + address);
            }
        });
    }

    /**
     * 获取红牌中包含多签地址的
     */
    @Test
    public void test2() throws IOException {
        String hexFile = "redCard.txt";
        String s = IOUtils.resourceToString(hexFile, StandardCharsets.UTF_8, this.getClass().getClassLoader());
        Map<String, Object> map = JSONUtils.json2map(s);
        List<Map> list = (List<Map>) ((Map)map.get("result")).get("list");
        List<String> hashList = new ArrayList<>();
        list.stream().forEach(m -> {
            //System.out.println(m.get("agentAddress"));
            hashList.add(m.get("hash").toString());
        });
        hashList.stream().forEach(hash -> {
            System.out.println(String.format("hash: %s", hash));
            if(isRedCardTxContainMultiSignAddress(hash)) {
                System.out.println("多签地址的红牌交易: " + hash);
            }
        });
    }

    private boolean isRedCardTxContainMultiSignAddress(String hash) {
        RpcResult getTx = JsonRpcUtil.request("getTx", ListUtil.of(1, hash));
        Map map = (Map) getTx.getResult();
        List<Map> fromList = (List<Map>) map.get("from");
        boolean hasRed = false;
        for(Map from : fromList) {
            System.out.println(String.format("from address: %s", from.get("address").toString()));
            if(isMultiSignAddress(AddressTool.getAddress(from.get("address").toString()))) {
                System.out.println("包含多签地址的红牌交易的多签地址: " + from.get("address").toString());
                hasRed = true;
            }
        }
        return hasRed;
    }

    public static boolean isMultiSignAddress(byte[] addr) {
        if (addr != null && addr.length > 3) {
            return addr[2] == BaseConstant.P2SH_ADDRESS_TYPE;
        }
        return false;
    }

}
