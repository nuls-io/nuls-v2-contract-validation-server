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
package io.nuls.api.controller.contract;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.contract.validation.service.CompareJar;
import io.nuls.core.basic.Result;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.model.jsonrpc.RpcErrorCode;
import io.nuls.model.jsonrpc.RpcResultError;
import io.nuls.server.ServerContext;
import io.nuls.v2.txdata.CreateContractData;
import io.nuls.v2.util.NulsSDKTool;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

/**
 * @author: PierreLuo
 * @date: 2021/10/21
 */
public class CompareJarTest {

    @Before
    public void before() {
        ServerContext.chain_id = 2;
        ServerContext.wallet_url = "http://beta.api.nuls.io/";
    }

    @Test
    public void compareTest() throws Exception {
        // 获取智能合约的代码
        String createTxHash = "8fe1692c19946134cec4abe4af6725c3475daad7b4f028a677cad817c9698643";
        Result result1 = NulsSDKTool.getTx(createTxHash);
        Map txMap = (Map) result1.getData();
        String txDataHex = (String) txMap.get("txDataHex");
        CreateContractData txData = new CreateContractData();
        txData.parse(new NulsByteBuffer(HexUtil.decode(txDataHex)));
        byte[] contractCode = txData.getCode();

        InputStream in = new FileInputStream("/Users/pierreluo/IdeaProjects/pocmContract-new/target/pocmContract-new-1.0-SNAPSHOT.jar");
        byte[] validateContractCode = IOUtils.toByteArray(in);
        // 比较代码指令
        boolean isValidationPass = CompareJar.compareJarBytes(contractCode, validateContractCode);
        System.out.println(isValidationPass);
    }
}
