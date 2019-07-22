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

import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.model.jsonrpc.RpcResult;
import io.nuls.server.ServerContext;
import io.nuls.server.jsonrpc.controller.ContractController;
import io.nuls.server.utils.JsonRpcUtil;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author: PierreLuo
 * @date: 2019-07-22
 */
public class ContractRequestTest extends Base{

    @Before
    public void before() {
        ServerContext.main_chain_id = 2;
        ServerContext.wallet_url = "http://localhost:15151/";
    }

    @Test
    public void validateContractCode() {
        FileInputStream in=  null;
        try {
            List<Object> params = new ArrayList<>();
            String address = "tNULSeBaMyoghhJR8wA46u9B5vAiefYRhVct1Z";
            File file = new File(BASE + "/contract/code/nrc20_token.zip");
            in = new FileInputStream(file);
            params.add(ServerContext.main_chain_id);
            params.add(address);
            params.add("mockHeader," + Base64.getEncoder().encodeToString(IOUtils.toByteArray(in)));

            RpcResult rpcResult = JsonRpcUtil.request("validateContractCode", params);
            System.out.println(rpcResult);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(in);
        }

    }

}
