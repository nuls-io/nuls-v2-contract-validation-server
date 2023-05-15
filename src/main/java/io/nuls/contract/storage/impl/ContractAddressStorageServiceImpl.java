/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2019 nuls.io
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
package io.nuls.contract.storage.impl;


import io.nuls.base.basic.AddressTool;
import io.nuls.contract.storage.ContractAddressStorageService;
import io.nuls.contract.utils.ContractDBUtil;
import io.nuls.contract.utils.ContractUtil;
import io.nuls.core.basic.Result;
import io.nuls.core.constant.CommonCodeConstanst;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.model.StringUtils;
import io.nuls.core.rockdb.service.RocksDBService;
import io.nuls.model.contract.ContractAddressInfoPo;
import io.nuls.model.contract.ContractVerifyPo;

import static io.nuls.contract.constant.ContractConstant.*;

/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/5/24
 */
@Component
public class ContractAddressStorageServiceImpl implements ContractAddressStorageService {

    private final String baseArea = DB_NAME_CONTRACT_ADDRESS + "_";
    private final String baseAreaCodeHash = DB_NAME_CONTRACT_ADDRESS_CODE_HASH + "_";

    @Override
    public Result saveContractAddress(int chainId, byte[] contractAddressBytes, ContractAddressInfoPo info) throws Exception {
        if (contractAddressBytes == null || info == null) {
            return Result.getFailed(CommonCodeConstanst.NULL_PARAMETER);
        }
        if (info.getStatus().intValue() == 2 && StringUtils.isNotBlank(info.getCodeHash())) {
            this.saveCodeHashVerified(chainId, info.getCodeHash(), info.getContractAddress());
        }
        boolean result = ContractDBUtil.putModel(baseArea + chainId, contractAddressBytes, info);
        if (result) {
            return ContractUtil.getSuccess();
        } else {
            return ContractUtil.getFailed();
        }
    }

    @Override
    public void saveCodeHashVerified(int chainId, String codeHash, String contract) throws Exception {
        byte[] hashBytes = HexUtil.decode(codeHash);
        byte[] bytes = RocksDBService.get(baseAreaCodeHash + chainId, hashBytes);
        if (bytes != null) {
            return;
        }
        ContractDBUtil.putModel(baseAreaCodeHash + chainId, hashBytes, new ContractVerifyPo(contract, System.currentTimeMillis()));
    }

    @Override
    public ContractVerifyPo getCodeHashVerified(int chainId, String codeHash) throws Exception {
        ContractVerifyPo po = ContractDBUtil.getModel(baseAreaCodeHash + chainId, HexUtil.decode(codeHash), ContractVerifyPo.class);
        return po;
    }

    @Override
    public Result<ContractAddressInfoPo> getContractAddressInfo(int chainId, byte[] contractAddressBytes) {
        if (contractAddressBytes == null) {
            return Result.getFailed(CommonCodeConstanst.NULL_PARAMETER);
        }
        ContractAddressInfoPo infoPo = ContractDBUtil.getModel(baseArea + chainId, contractAddressBytes, ContractAddressInfoPo.class);
        return ContractUtil.getSuccess().setData(infoPo);
    }

    @Override
    public Result deleteContractAddress(int chainId, byte[] contractAddressBytes) throws Exception {
        if (contractAddressBytes == null) {
            return Result.getFailed(CommonCodeConstanst.NULL_PARAMETER);
        }
        boolean result = RocksDBService.delete(baseArea + chainId, contractAddressBytes);
        if (result) {
            return ContractUtil.getSuccess();
        } else {
            return ContractUtil.getFailed();
        }
    }

    @Override
    public Result updateContractInfo(int chainId, byte[] contractAddressBytes, ContractAddressInfoPo info) throws Exception {
        this.deleteContractAddress(chainId, contractAddressBytes);
        return this.saveContractAddress(chainId, contractAddressBytes, info);
    }

    @Override
    public boolean isExistContractAddress(int chainId, byte[] contractAddressBytes) {
        if (contractAddressBytes == null) {
            return false;
        }
        byte[] contract = RocksDBService.get(baseArea + chainId, contractAddressBytes);
        if (contract == null) {
            return false;
        }
        return true;
    }


}
