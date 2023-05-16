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
package io.nuls.v2.util;

import io.nuls.core.basic.Result;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.model.StringUtils;
import io.nuls.model.jsonrpc.RpcResult;
import io.nuls.model.jsonrpc.RpcResultError;
import io.nuls.server.ServerContext;
import io.nuls.server.utils.JsonRpcUtil;

/**
 * @author: PierreLuo
 * @date: 2019-07-21
 */
public class NulsSDKTool {

    public static Result getContractInfo(String contractAddress) {
        RpcResult rpcResult = JsonRpcUtil.request("getContract", new Object[]{ServerContext.chain_id, contractAddress});
        return makeResult(rpcResult);
    }

    public static Result getTx(String createTxHash) {
        RpcResult rpcResult = JsonRpcUtil.request("getTx", new Object[]{ServerContext.chain_id, createTxHash});
        return makeResult(rpcResult);
    }

    public static Result getContractCodeHash(String contractAddress) {
        RpcResult rpcResult = JsonRpcUtil.request("codeHash", new Object[]{ServerContext.chain_id, contractAddress});
        return makeResult(rpcResult);
    }

    public static Result getContractCode(String contractAddress) {
        RpcResult rpcResult = JsonRpcUtil.request("contractCode", new Object[]{ServerContext.chain_id, contractAddress});
        return makeResult(rpcResult);
    }

    static Result makeResult(RpcResult rpcResult) {
        RpcResultError error = rpcResult.getError();
        if(error != null) {
            String errorMsg = "";
            String message = error.getMessage();
            if(StringUtils.isNotBlank(message)) {
                errorMsg += message;
            }
            Object errorData = error.getData();
            if(errorData != null) {
                errorMsg += ";" + errorData.toString();
            }
            return Result.getFailed(ErrorCode.init(error.getCode())).setMsg(errorMsg);
        }
        return Result.getSuccess(rpcResult.getResult());
    }
}
