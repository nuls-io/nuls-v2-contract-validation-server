/*
 * MIT License
 * Copyright (c) 2017-2019 nuls.io
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.nuls.server.jsonrpc.controller;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.contract.storage.ContractAddressStorageService;
import io.nuls.contract.validation.service.CompareJar;
import io.nuls.core.basic.Result;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Controller;
import io.nuls.core.core.annotation.RpcMethod;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.log.Log;
import io.nuls.core.model.StringUtils;
import io.nuls.model.contract.ContractAddressInfoPo;
import io.nuls.model.contract.ContractCode;
import io.nuls.model.contract.ContractCodeNode;
import io.nuls.model.jsonrpc.RpcErrorCode;
import io.nuls.model.jsonrpc.RpcResult;
import io.nuls.model.jsonrpc.RpcResultError;
import io.nuls.server.jsonrpc.JsonRpcException;
import io.nuls.server.utils.RunShellUtil;
import io.nuls.server.utils.VerifyUtils;
import io.nuls.v2.txdata.CreateContractData;
import io.nuls.v2.util.NulsSDKTool;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author: PierreLuo
 * @date: 2019-07-21
 */
@Controller
public class ContractController {

    @Autowired
    private ContractAddressStorageService contractService;

    private static String BASE;

    private static String VALIDATE_HOME;

    static {
        String serverHome = System.getProperty("contract.server.home");
        if (StringUtils.isBlank(serverHome)) {
            URL resource = ClassLoader.getSystemClassLoader().getResource("");
            String classPath = resource.getPath();
            File file = null;
            try {
                file = new File(URLDecoder.decode(classPath, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                Log.error(e);
                file = new File(classPath);
            }
            BASE = file.getPath();
        } else {
            BASE = serverHome;
        }
        VALIDATE_HOME = BASE + File.separator + "contract" + File.separator + "code" + File.separator;
    }

    @RpcMethod("getContractAddressInfo")
    public RpcResult getContractAddressInfo(List<Object> params) {
        RpcResult result = new RpcResult();
        try {
            VerifyUtils.verifyParams(params, 2);
            int chainId = (Integer) params.get(0);
            String contractAddress = (String) params.get(1);
            if (!AddressTool.validAddress(chainId, contractAddress)) {
                result.setError(new RpcResultError(RpcErrorCode.PARAMS_ERROR, "[contractAddress] is inValid"));
                return result;
            }
            byte[] contractAddressBytes = AddressTool.getAddress(contractAddress);
            ContractAddressInfoPo contractInfo = contractService.getContractAddressInfo(chainId, contractAddressBytes).getData();
            if (contractInfo == null) {
                Result contractInfoResult = NulsSDKTool.getContractInfo(contractAddress);
                if(contractInfoResult.isFailed()) {
                    result.setError(new RpcResultError(RpcErrorCode.DATA_NOT_EXISTS));
                    return result;
                }
                Map contractInfoMap = (Map) contractInfoResult.getData();
                String txHash = (String) contractInfoMap.get("createTxHash");
                String status = (String) contractInfoMap.get("status");
                if(!"normal".equals(status)) {
                    result.setError(new RpcResultError(RpcErrorCode.CONTRACT_STATUS_ERROR));
                    return result;
                }
                contractInfo = new ContractAddressInfoPo();
                contractInfo.setContractAddress(contractAddress);
                contractInfo.setCreateTxHash(txHash);
                contractInfo.setStatus(0);
                contractService.saveContractAddress(chainId, contractAddressBytes, contractInfo);
            }
            result.setResult(contractInfo);
        } catch (Exception e) {
            Log.error(e);
            result.setError(new RpcResultError(RpcErrorCode.PARAMS_ERROR, e.getMessage()));
        }
        return result;
    }

    @RpcMethod("validateContractCode")
    public RpcResult validateContractCode(List<Object> params) {

        RpcResult result = new RpcResult();
        OutputStream out = null;
        InputStream jarIn = null;
        String contractAddress = null;
        File zipFile = null;
        try {
            VerifyUtils.verifyParams(params, 3);
            int chainId = (Integer) params.get(0);
            contractAddress = (String) params.get(1);
            if (!AddressTool.validAddress(chainId, contractAddress)) {
                result.setError(new RpcResultError(RpcErrorCode.PARAMS_ERROR, "contractAddress is inValid."));
                return result;
            }
            // 检查认证状态，未认证的合约继续下一步
            byte[] contractAddressBytes = AddressTool.getAddress(contractAddress);
            ContractAddressInfoPo contractInfo = contractService.getContractAddressInfo(chainId, contractAddressBytes).getData();
            if (contractInfo == null) {
                Result contractInfoResult = NulsSDKTool.getContractInfo(contractAddress);
                if(contractInfoResult.isFailed()) {
                    result.setError(new RpcResultError(RpcErrorCode.DATA_NOT_EXISTS));
                    return result;
                }
                Map contractInfoMap = (Map) contractInfoResult.getData();
                String txHash = (String) contractInfoMap.get("createTxHash");
                String status = (String) contractInfoMap.get("status");
                if(!"normal".equals(status)) {
                    result.setError(new RpcResultError(RpcErrorCode.CONTRACT_STATUS_ERROR));
                    return result;
                }
                contractInfo = new ContractAddressInfoPo();
                contractInfo.setContractAddress(contractAddress);
                contractInfo.setCreateTxHash(txHash);
                contractInfo.setStatus(0);
                contractService.saveContractAddress(chainId, contractAddressBytes, contractInfo);
            }

            Integer status = contractInfo.getStatus();
            // 已进入以下状态 -> 正在审核 or 通过验证 or 已删除
            if (status > 0) {
                result.setError(new RpcResultError(RpcErrorCode.CONTRACT_VALIDATION_ERROR));
                return result;
            }

            // 生成文件
            String fileDataURL = (String) params.get(2);
            String[] arr = fileDataURL.split(",");
            if (arr.length != 2) {
                result.setError(new RpcResultError(RpcErrorCode.PARAMS_ERROR, "File Data error."));
                return result;
            }

            Log.debug("contract validate home: {}", VALIDATE_HOME);
            Log.debug("base home: {}", BASE);

            String headerInfo = arr[0];
            String body = arr[1];
            byte[] fileContent = Base64.getDecoder().decode(body);
            zipFile = new File(VALIDATE_HOME + contractAddress + ".zip");
            out = new FileOutputStream(zipFile);
            IOUtils.write(fileContent, out);

            boolean isValidationPass = false;
            do {
                // 编译代码
                List<String> resultList = RunShellUtil.run(BASE + File.separator + "bin" + File.separator + "compile.sh", contractAddress);
                if (!resultList.isEmpty()) {
                    String error = resultList.stream().collect(Collectors.joining());
                    Log.error(error);
                    result.setError(new RpcResultError(RpcErrorCode.TX_SHELL_ERROR));
                    break;
                }

                // 检查智能合约源代码zip包验证，不能包含非java文件
                String illegalFile = checkSourceFile(VALIDATE_HOME + contractAddress + File.separator + "src");
                if (StringUtils.isNotBlank(illegalFile)) {
                    result.setError(new RpcResultError(RpcErrorCode.PARAMS_ERROR).setMessage("An illegal file was detected. The file name is " + illegalFile));
                    break;
                }

                File jarFile = new File(VALIDATE_HOME + contractAddress + File.separator + contractAddress + ".jar");
                jarIn = new FileInputStream(jarFile);
                byte[] validateContractCode = IOUtils.toByteArray(jarIn);

                // 获取智能合约的代码
                String createTxHash = contractInfo.getCreateTxHash();
                Result result1 = NulsSDKTool.getTx(createTxHash);
                if (result1.isFailed()) {
                    result.setError(new RpcResultError(RpcErrorCode.DATA_NOT_EXISTS));
                    break;
                }
                Map txMap = (Map) result1.getData();
                String txDataHex = (String) txMap.get("txDataHex");
                CreateContractData txData = new CreateContractData();
                txData.parse(new NulsByteBuffer(HexUtil.decode(txDataHex)));
                byte[] contractCode = txData.getCode();

                // 比较代码指令
                isValidationPass = CompareJar.compareJarBytes(contractCode, validateContractCode);

                if (!isValidationPass) {
                    result.setError(new RpcResultError(RpcErrorCode.CONTRACT_VALIDATION_FAILED));
                    break;
                }

                // 合约认证通过后，更新合约认证状态
                result.setResult(isValidationPass);
                contractInfo.setStatus(2);
                contractInfo.setCertificationTime(System.currentTimeMillis());
                contractService.updateContractInfo(chainId, contractAddressBytes, contractInfo);
            } while (false);

            if (!Log.isDebugEnabled()) {
                if (!isValidationPass) {
                    // 删除上传的文件
                    if (zipFile.exists()) {
                        zipFile.delete();
                    }
                    delFolder(VALIDATE_HOME + contractAddress);
                    return result;
                }
            }


        } catch (Exception e) {
            Log.error(e);
            if (!Log.isDebugEnabled()) {
                // 删除上传的文件
                if (zipFile != null && zipFile.exists()) {
                    zipFile.delete();
                }
                if (StringUtils.isNotBlank(contractAddress)) {
                    delFolder(VALIDATE_HOME + contractAddress);
                }
            }
            throw new JsonRpcException(new RpcResultError(RpcErrorCode.PARAMS_ERROR, e.getMessage()));
        } finally {
            IOUtils.closeQuietly(jarIn);
            IOUtils.closeQuietly(out);
        }
        return result;
    }

    public String checkSourceFile(String path) {
        File src = new File(path);
        return recursiveCheck(src.listFiles());
    }

    private String recursiveCheck(File[] files) {
        for (File file : files) {
            if (file.isDirectory()) {
                String result = recursiveCheck(file.listFiles());
                if (StringUtils.isNotBlank(result)) {
                    return result;
                }
            } else {
                if (!permissibleFile(file.getName())) {
                    return file.getName();
                }
            }
        }
        return null;
    }

    private boolean permissibleFile(String fileName) {
        if (fileName != null && fileName.endsWith(".java")) {
            return true;
        }
        return false;
    }

    private void delFolder(String folderPath) {
        try {
            //删除完里面所有内容
            delAllFile(folderPath);
            File myFilePath = new File(folderPath);
            //删除空文件夹
            myFilePath.delete();
        } catch (Exception e) {
        }
    }

    private boolean delAllFile(String path) {
        boolean flag = false;
        File file = new File(path);
        if (!file.exists()) {
            return flag;
        }
        if (!file.isDirectory()) {
            return flag;
        }
        String[] tempList = file.list();
        File temp = null;
        for (int i = 0; i < tempList.length; i++) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + tempList[i]);
            } else {
                temp = new File(path + File.separator + tempList[i]);
            }
            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
                delFolder(path + File.separator + tempList[i]);
                flag = true;
            }
        }
        return flag;
    }

    @RpcMethod("getContractCodeTree")
    public RpcResult getContractCodeTree(List<Object> params) {
        RpcResult result = new RpcResult();


        try {
            VerifyUtils.verifyParams(params, 2);
            int chainId = (Integer) params.get(0);
            String contractAddress = (String) params.get(1);
            if (!AddressTool.validAddress(chainId, contractAddress)) {
                result.setError(new RpcResultError(RpcErrorCode.PARAMS_ERROR, "[contractAddress] is inValid"));
                return result;
            }
            ContractAddressInfoPo contractInfo = contractService.getContractAddressInfo(chainId, AddressTool.getAddress(contractAddress)).getData();
            if (contractInfo == null) {
                result.setError(new RpcResultError(RpcErrorCode.DATA_NOT_EXISTS));
                return result;
            }
            Integer status = contractInfo.getStatus();
            // 检查认证状态，通过认证的合约继续下一步
            if (status != 2) {
                result.setError(new RpcResultError(RpcErrorCode.CONTRACT_NOT_VALIDATION_ERROR));
                return result;
            }

            // 提取文件目录树
            ContractCode root = contractCodeTreeCaches.get(contractAddress);
            if (root == null) {
                result.setError(new RpcResultError(RpcErrorCode.PARAMS_ERROR, "root path is inValid"));
                return result;
            }
            result.setResult(root);
        } catch (Exception e) {
            Log.error(e);
            result.setError(new RpcResultError(RpcErrorCode.PARAMS_ERROR, e.getMessage()));
        }
        return result;
    }

    private LoadingCache<String, ContractCode> contractCodeTreeCaches = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build(new CacheLoader<String, ContractCode>() {
                @Override
                public ContractCode load(String contractAddress) {
                    return generateContractCodeTree(contractAddress);
                }
            });

    private ContractCode generateContractCodeTree(String contractAddress) {
        File src = new File(VALIDATE_HOME + contractAddress + File.separator + "src");
        ContractCode root = new ContractCode();
        ContractCodeNode rootNode = new ContractCodeNode();
        if (!src.isDirectory()) {
            return null;
        }
        List<ContractCodeNode> children = new ArrayList<>();
        rootNode.setName(src.getName());
        rootNode.setPath(extractFilePath(src));
        rootNode.setDir(true);
        rootNode.setChildren(children);
        root.setRoot(rootNode);
        File[] files = src.listFiles();
        recursive(src.listFiles(), children);
        return root;
    }


    private void recursive(File[] files, List<ContractCodeNode> children) {
        for (File file : files) {
            ContractCodeNode node = new ContractCodeNode();
            children.add(node);
            node.setName(extractFileName(file));
            node.setPath(extractFilePath(file));
            node.setDir(file.isDirectory());
            if (file.isDirectory()) {
                node.setChildren(new ArrayList<>());
                recursive(file.listFiles(), node.getChildren());
            }
        }
    }

    private String extractFileName(File file) {
        if (file.isDirectory()) {
            return file.getName();
        }
        String name = file.getName();
        name = name.replaceAll("\\.java", "");
        return name;
    }

    private String extractFilePath(File file) {
        String path = file.getPath();
        path = path.replaceAll(BASE, "");
        return path;
    }

    @RpcMethod("getContractCode")
    public RpcResult getContractCode(List<Object> params) {
        RpcResult result = new RpcResult();
        try {
            VerifyUtils.verifyParams(params, 3);
            int chainId = (Integer) params.get(0);
            String contractAddress = (String) params.get(1);
            if (!AddressTool.validAddress(chainId, contractAddress)) {
                result.setError(new RpcResultError(RpcErrorCode.PARAMS_ERROR, "[contractAddress] is inValid"));
                return result;
            }
            // 检查认证状态，通过认证的合约继续下一步
            ContractAddressInfoPo contractInfo = contractService.getContractAddressInfo(chainId, AddressTool.getAddress(contractAddress)).getData();
            if (contractInfo == null) {
                result.setError(new RpcResultError(RpcErrorCode.DATA_NOT_EXISTS));
                return result;
            }
            Integer status = contractInfo.getStatus();
            if (status != 2) {
                result.setError(new RpcResultError(RpcErrorCode.CONTRACT_NOT_VALIDATION_ERROR));
                return result;
            }

            // 提取文件内容
            String filePath = (String) params.get(2);
            String code = contractCodeCaches.get(filePath);
            if (StringUtils.isBlank(code)) {
                result.setError(new RpcResultError(RpcErrorCode.DATA_NOT_EXISTS, "Fail to read contract code."));
                return result;
            }
            result.setResult(code);
        } catch (Exception e) {
            Log.error(e);
            result.setError(new RpcResultError(RpcErrorCode.PARAMS_ERROR, e.getMessage()));
        }
        return result;
    }

    private LoadingCache<String, String> contractCodeCaches = CacheBuilder.newBuilder()
            .maximumSize(500)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build(new CacheLoader<String, String>() {
                @Override
                public String load(String filePath) {
                    return readContractCode(filePath);
                }
            });

    private String readContractCode(String filePath) {
        FileInputStream in = null;
        try {
            File file = new File(BASE + filePath);
            if (!file.exists()) {
                return null;
            }
            in = new FileInputStream(file);
            List<String> strings = IOUtils.readLines(in);
            StringBuilder sb = new StringBuilder();
            strings.forEach(a -> {
                sb.append(a).append("\r\n");
            });
            return sb.toString();
        } catch (Exception e) {
            Log.error(e);
            return null;
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

}
