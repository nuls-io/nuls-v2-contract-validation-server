package io.nuls.server.utils;

import io.nuls.core.constant.CommonCodeConstanst;
import io.nuls.core.log.Log;
import io.nuls.core.parse.JSONUtils;
import io.nuls.model.restful.RestFulResult;

import java.io.IOException;
import java.util.Map;

import static io.nuls.server.ServerContext.wallet_url;

/**
 * 基于HttpClient第三方工具的http工具类
 *
 * @author Administrator
 */
public class RestFulUtil {

    /**
     * 发送get请求不带参数
     *
     * @param url
     * @return
     */
    public static RestFulResult<Map<String, Object>> get(String url) {
        return get(url, null);
    }

    /**
     * 发送get请求
     *
     * @param url
     * @param params
     * @return
     */
    public static RestFulResult<Map<String, Object>> get(String url, Map<String, Object> params) {
        try {
            url = wallet_url + url;
            String resultStr = HttpClientUtil.get(url, params);
            RestFulResult<Map<String, Object>> result = toResult(resultStr);
            return result;
        } catch (Exception e) {
            Log.error(e);
            return RestFulResult.failed(CommonCodeConstanst.DATA_ERROR.getCode(), e.getMessage(), null);
        }
    }

    /**
     * 发送post请求
     *
     * @param url
     * @param params
     * @return
     */
    public static RestFulResult<Map<String, Object>> post(String url, Map<String, Object> params) {
        try {
            url = wallet_url + url;
            String resultStr = HttpClientUtil.post(url, params);
            RestFulResult<Map<String, Object>> result = toResult(resultStr);
            return result;
        } catch (Exception e) {
            Log.error(e);
            return RestFulResult.failed(CommonCodeConstanst.DATA_ERROR.getCode(), e.getMessage(), null);
        }
    }

    /**
     * 发送put请求
     *
     * @param url
     * @param params
     * @return
     */
    public static RestFulResult<Map<String, Object>> put(String url, Map<String, Object> params) {
        try {
            url = wallet_url + url;
            String resultStr = HttpClientUtil.put(url, params);
            RestFulResult<Map<String, Object>> result = toResult(resultStr);
            return result;
        } catch (Exception e) {
            Log.error(e);
            return RestFulResult.failed(CommonCodeConstanst.DATA_ERROR.getCode(), e.getMessage(), null);
        }
    }

    private static RestFulResult toResult(String str) throws IOException {
        Map<String, Object> resultMap = JSONUtils.json2map(str);
        RestFulResult<Map<String, Object>> result = null;
        Boolean b = (Boolean) resultMap.get("success");
        if (b) {
            Map<String, Object> data = (Map<String, Object>) resultMap.get("data");
            result = RestFulResult.success(data);
        } else {
            Map<String, Object> data = (Map<String, Object>) resultMap.get("data");
            if (data != null) {
                result = RestFulResult.failed(data.get("code").toString(), data.get("msg").toString());
            }


        }
        return result;
    }
}
