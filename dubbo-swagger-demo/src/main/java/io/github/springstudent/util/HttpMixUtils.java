package org.shangyu.szjsc.third.utils;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.hikvision.artemis.sdk.ArtemisHttpUtil;
import com.hikvision.artemis.sdk.config.ArtemisConfig;
import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.shangyu.szjsc.common.bean.Constants;
import org.shangyu.szjsc.common.exceptions.ResultException;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author zhouning
 * @date 2024/05/15 9:51
 */
public class HttpMixUtils {

    /**
     * 刷新密钥缓存
     */
    private static final Cache<String, String> refreshSecretCache = CacheBuilder.newBuilder().expireAfterWrite(2820L, TimeUnit.MINUTES).build();
    /**
     * 请求秘钥缓存
     */
    private static final Cache<String, String> requestSecretCache = CacheBuilder.newBuilder().expireAfterWrite(12L, TimeUnit.MINUTES).build();

    static {
        initHkArtemisConfig();
    }

    private static void initHkArtemisConfig() {
        ArtemisConfig.host = Constants.CAMERA_PREVIEW_APP_HOST;
        ArtemisConfig.appKey = Constants.CAMERA_PREVIEW_APP_KEY;
        ArtemisConfig.appSecret = Constants.CAMERA_PREVIEW_APP_SECRET;
        com.hikvision.artemis.sdk.constant.Constants.DEFAULT_TIMEOUT = Constants.HTTP_TIMEOUT;

    }


    /**
     * 获取永不过期的视频流播放地址
     *
     * @param
     * @throws ResultException
     * @author ZhouNing
     * @date 2024/5/16 9:12
     **/
    public static JSONObject cameraPreviewUrlNoExpire() throws ResultException {
        final String ARTEMIS_PATH = "/artemis";
        final String previewURLsApi = ARTEMIS_PATH + "/api/vnsc/mls/v1/preview/openApi/getPreviewParam";
        Map<String, String> path = new HashMap<String, String>(2) {{
            put("https://", previewURLsApi);
        }};
        String contentType = "application/json";
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("indexCode", "2503998ee0764013abfb65ee7e17c76c");
        jsonBody.put("transmode", 1);
        jsonBody.put("streamType", 0);
        jsonBody.put("protocol", "ws");
        jsonBody.put("expireTime", -1);
        appendExpand(jsonBody);
        String body = jsonBody.toJSONString();
        String result = ArtemisHttpUtil.doPostStringArtemis(path, body, null, null, contentType, null);
        return parseJSONResult(result, previewURLsApi, jsonBody, HttpResultParse.HK_Artemis);
    }

    /**
     * 获取相机预览地址
     *
     * @param
     * @throws ResultException
     * @author ZhouNing
     * @date 2024/5/15 14:27
     **/
    public static JSONObject cameraPreviewURL() throws ResultException {
        final String ARTEMIS_PATH = "/artemis";
        final String previewURLsApi = ARTEMIS_PATH + "/api/video/v1/cameras/previewURLs";
        Map<String, String> path = new HashMap<String, String>(2) {{
            put("https://", previewURLsApi);
        }};
        String contentType = "application/json";
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("cameraIndexCode", "2503998ee0764013abfb65ee7e17c76c");
        jsonBody.put("streamType", 0);
        jsonBody.put("protocol", "hls");
        jsonBody.put("transmode", 1);
        appendExpand(jsonBody);
        String body = jsonBody.toJSONString();
        String result = ArtemisHttpUtil.doPostStringArtemis(path, body, null, null, contentType, null);
        return parseJSONResult(result, previewURLsApi, jsonBody, HttpResultParse.HK_Artemis);
    }

    private static void appendExpand(JSONObject jsonBody) {
        if (jsonBody.getString("protocol").equals("rtsp")) {
            jsonBody.put("expand", "streamform=rtp");
        } else if (jsonBody.getString("protocol").equals("hls")) {
            //兼容h265视频流通过hls播放
            jsonBody.put("expand", "transcode=1&videotype=h264");
        } else if (jsonBody.getString("protocol").equals("ws")) {
        }
    }

    /**
     * ͨ获取所有相机
     *
     * @param
     * @throws Exception
     * @author ZhouNing
     * @date 2024/5/15 14:27
     **/
    public static JSONObject cameras() throws ResultException {
        final String ARTEMIS_PATH = "/artemis";
        final String previewURLsApi = ARTEMIS_PATH + "/api/resource/v1/cameras";
        Map<String, String> path = new HashMap<String, String>(2) {{
            put("https://", previewURLsApi);
        }};
        String contentType = "application/json";
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("pageNo", 1);
        jsonBody.put("pageSize", 10);
        String body = jsonBody.toJSONString();
        String result = ArtemisHttpUtil.doPostStringArtemis(path, body, null, null, contentType, null);
        return parseJSONResult(result, previewURLsApi, jsonBody, HttpResultParse.HK_Artemis);
    }


    public static JSONObject parseJSONResult(String result, String reqUri, Map<String, Object> paramMap, HttpResultParse httpResultParse) throws ResultException {
        JSONObject jsonObject = JSON.parseObject(result);
        String msg = httpResultParse.msgFunc.apply(jsonObject);
        if (httpResultParse.codePredicate.test(jsonObject)) {
            return jsonObject;
        } else {
            throw new ResultException("请求uri" + reqUri + "，参数:" + paramMap + ",错误:" + msg);
        }
    }

    /**
     * 获取环境数据
     *
     * @param
     * @throws Exception
     * @author ZhouNing
     * @date 2024/5/15 14:30
     **/
    public static String envData() throws Exception {
        //每次请求接口前都要获取请求秘钥
        String requestSecret = envRequestSecret();
        Map<String, Object> params = new HashMap<>();
        long time = System.currentTimeMillis();
        String sign = SecureUtil.md5(Constants.ENV_APP_KEY + requestSecret + time);
        params.put("appKey", Constants.ENV_APP_KEY);
        params.put("sign", sign);
        params.put("requestTime", time + "");
        /** 业务需要的字段信息 **/
        params.put("gcbm", "SYJ220144");
        String result = post(Constants.ENV_HOST + "/data-assets/gateway/service/sync_133_1", params);
        System.out.println("#########envData="+result);
        return result;
    }

    private static String envRequestSecret() throws Exception {
        String requestSecret = requestSecretCache.getIfPresent(Constants.ENV_APP_KEY);
        if (requestSecret == null) {
            String json = "";
            String refreshSecret = refreshSecretCache.getIfPresent(Constants.ENV_APP_KEY);
            if (refreshSecret != null) {
                json = envRequestTokenBySec(refreshSecret);
                if (json == null || !json.startsWith("{") || !"00".equals(JSONObject.parseObject(json).getString("code"))) {
                    json = envRequestTokenByKey();
                }
            } else {
                json = envRequestTokenByKey();
            }

            if (json == null || !json.startsWith("{") || !"00".equals(JSONObject.parseObject(json).getString("code"))) {
                throw new RuntimeException("请求requestSecret出现错误：" + json);
            }
            JSONObject datas = JSONObject.parseObject(json).getJSONObject("datas");
            refreshSecretCache.put(Constants.ENV_APP_KEY, datas.getString("refreshSecret"));
            requestSecretCache.put(Constants.ENV_APP_KEY, datas.getString("requestSecret"));
            requestSecret = datas.getString("requestSecret");
        }
        return requestSecret;
    }


    /**
     * 通过 appkey 和 app 密钥获取刷新密钥和请求密钥请求
     *
     * @param
     * @throws Exception
     * @author ZhouNing
     * @date 2024/5/20 14:49
     **/
    public static String envRequestTokenByKey() throws Exception {
        Map<String, Object> params = new HashMap<>();
        long time = System.currentTimeMillis();
        String sign = SecureUtil.md5(Constants.ENV_APP_KEY + Constants.ENV_APP_SECRET + time);
        params.put("appKey", Constants.ENV_APP_KEY);
        params.put("sign", sign);
        params.put("requestTime", time + "");
        String result = post(Constants.ENV_HOST + "/data-assets/gateway/refreshTokenByKey", params);
        System.out.println("#########envRequestTokenByKey="+result);
        return result;
    }

    /**
     * 通过 appkey 和刷新获取刷新密钥和请求密钥请求
     *
     * @param requestSecret
     * @throws Exception
     * @author ZhouNing
     * @date 2024/5/20 14:49
     **/
    public static String envRequestTokenBySec(String requestSecret) throws Exception {
        Map<String, Object> params = new HashMap<>();
        long time = System.currentTimeMillis();
        String sign = SecureUtil.md5(Constants.ENV_APP_KEY + requestSecret + time);
        params.put("appKey", Constants.ENV_APP_KEY);
        params.put("sign", sign);
        params.put("requestTime", time + "");
        String result = post(Constants.ENV_HOST + "/data-assets/gateway/refreshTokenBySec", params);
        System.out.println("#########envRequestTokenBySec="+result);
        return result;
    }


    private static String post(String url, Map<String, Object> paramMap) throws Exception {
        String response = "";
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse execute = null;
        try {
            HttpPost post = new HttpPost(url);
            if (paramMap != null) {
                List<NameValuePair> formParams = new ArrayList<>();
                for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
                    formParams.add(new BasicNameValuePair(entry.getKey(), String.valueOf(entry.getValue())));
                }
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formParams, "UTF-8");
                post.setEntity(entity);
            }
            SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, (x509Certificates, s) -> true).build();
            //设置请求超时时间
            RequestConfig defaultRequestConfig = RequestConfig.custom()
                    .setSocketTimeout(Constants.HTTP_TIMEOUT)
                    .setConnectTimeout(Constants.HTTP_TIMEOUT)
                    .setConnectionRequestTimeout(Constants.HTTP_TIMEOUT)
                    .setStaleConnectionCheckEnabled(true)
                    .build();
            httpClient = HttpClients.custom().setDefaultRequestConfig(defaultRequestConfig).setSSLContext(sslContext).setSSLHostnameVerifier(new NoopHostnameVerifier()).build();
            execute = httpClient.execute(post);
            response = IOUtils.toString(execute.getEntity().getContent(), "UTF-8");
        } catch (Exception e) {
            throw e;
        } finally {
            if (httpClient != null) {
                httpClient.close();
            }
            if (execute != null) {
                execute.close();
            }
        }
        return response;
    }

}
