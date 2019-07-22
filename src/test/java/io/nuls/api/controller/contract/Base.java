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

import io.nuls.ContractValidationServerBootstrap;
import io.nuls.core.log.Log;
import io.nuls.core.model.StringUtils;
import org.junit.Before;
import org.junit.BeforeClass;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

/**
 * @author: PierreLuo
 * @date: 2019-07-22
 */
public class Base {

    protected static String BASE;

    protected static void getBase() {
        String serverHome = System.getProperty("contract.server.home");
        if(StringUtils.isBlank(serverHome)) {
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
    }

    @Before
    public void startBaseUp() throws Exception {
        getBase();
        System.out.println("base is " + BASE);
    }

}
