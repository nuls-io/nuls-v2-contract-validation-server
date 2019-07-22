package io.nuls.api.controller.contract;

import io.nuls.ContractValidationServerBootstrap;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.log.Log;
import io.nuls.core.model.StringUtils;
import io.nuls.model.jsonrpc.RpcResult;
import io.nuls.server.ServerContext;
import io.nuls.server.jsonrpc.controller.ContractController;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ContractControllerTest extends Base{

    @BeforeClass
    public static void startUp() throws Exception {
        ContractValidationServerBootstrap.main(null);
    }

    @Test
    public void validateContractCode() {
        FileInputStream in=  null;
        try {
            TimeUnit.SECONDS.sleep(5);
            List<Object> params = new ArrayList<>();
            String address = "tNULSeBaMyoghhJR8wA46u9B5vAiefYRhVct1Z";
            File file = new File(BASE + "/contract/code/nrc20_token.zip");
            in = new FileInputStream(file);
            params.add(ServerContext.main_chain_id);
            params.add(address);
            params.add("mockHeader," + Base64.getEncoder().encodeToString(IOUtils.toByteArray(in)));

            ContractController controller = SpringLiteContext.getBean(ContractController.class);
            RpcResult rpcResult = controller.validateContractCode(params);
            System.out.println(rpcResult);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(in);
        }

    }

    @Test
    public void testFileBinaryBase64Encoder() {
        FileInputStream in = null;
        OutputStream out = null;
        try {
            File file = new File(BASE + "/contract/code/ddd.zip");
            in = new FileInputStream(file);

            String fileDataURL = "aaaa,";
            fileDataURL += Base64.getEncoder().encodeToString(IOUtils.toByteArray(in));
            String[] arr = fileDataURL.split(",");
            if (arr.length != 2) {
                Assert.assertTrue(false);
            }
            String headerInfo = arr[0];
            String body = arr[1];

            //String contentType = (headerInfo.split(":")[1]).split(";")[0];
            byte[] fileContent = Base64.getDecoder().decode(body);
            out = new FileOutputStream(new File(BASE + "/contract/code/ddd_copy.zip"));
            IOUtils.write(fileContent, out);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(in);
        }
    }

    @Test
    public void getCodeTest() {
        File file = new File(BASE + "/contract/code/tNULSeBaMyoghhJR8wA46u9B5vAiefYRhVct1Z/src/io/nuls/contract/token/SimpleToken.java");
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            List<String> strings = IOUtils.readLines(in);
            StringBuilder sb = new StringBuilder();
            strings.forEach(a -> {
                sb.append(a).append("\r\n");
            });
            System.out.println(sb.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(in);
        }
    }


}