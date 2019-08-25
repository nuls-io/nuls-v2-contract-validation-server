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

import io.nuls.core.log.Log;
import io.nuls.core.model.StringUtils;
import io.nuls.model.contract.ContractCode;
import io.nuls.model.contract.ContractCodeNode;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: PierreLuo
 * @date: 2019-01-29
 */
public class ContractCodeTreeTest extends Base{

    @Test
    public void genTree() {
        File src = new File(BASE + "/contract/code/tNULSeBaNAGTjvn1iTzbSBStNPRtbtxjja4bts/src");
        ContractCode root = new ContractCode();
        ContractCodeNode rootNode = new ContractCodeNode();
        if(!src.isDirectory()) {
            return;
        }
        List<ContractCodeNode> children = new ArrayList<>();
        rootNode.setName(src.getName());
        rootNode.setPath(extractFilePath(src));
        rootNode.setDir(true);
        rootNode.setChildren(children);
        root.setRoot(rootNode);
        File[] files = src.listFiles();
        recursive(src.listFiles(), children);
        System.out.println(root);
    }

    @Test
    public void checkSourceFile() {
        File src = new File(BASE + "/contract/code/tNULSeBaMyoghhJR8wA46u9B5vAiefYRhVct1Z/src");
        String illegalFile = recursiveCheck(src.listFiles());
        if(StringUtils.isNotBlank(illegalFile)) {
            System.out.println("An illegal file was detected. The file name is " + illegalFile);
        }
    }

    private String recursiveCheck(File[] files) {
        for(File file : files) {
            if(file.isDirectory()) {
                String result = recursiveCheck(file.listFiles());
                if(StringUtils.isNotBlank(result)) {
                    return result;
                }
            } else {
                System.out.println(file.getName());
                if(!permissibleFile(file.getName())) {
                    return file.getName();
                }
            }
        }
        return null;
    }

    private boolean permissibleFile(String fileName) {
        if(fileName != null && fileName.endsWith(".java")) {
            return true;
        }
        return false;
    }

    private void recursive(File[] files, List<ContractCodeNode> children) {
        for(File file : files) {
            ContractCodeNode node = new ContractCodeNode();
            children.add(node);
            node.setName(extractFileName(file));
            node.setPath(extractFilePath(file));
            node.setDir(file.isDirectory());
            if(file.isDirectory()) {
                node.setChildren(new ArrayList<>());
                recursive(file.listFiles(), node.getChildren());
            }
        }
    }

    private String extractFileName(File file) {
        if(file.isDirectory()) {
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
}
