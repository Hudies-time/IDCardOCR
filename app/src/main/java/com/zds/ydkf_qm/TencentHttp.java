package com.zds.ydkf_qm;
import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class TencentHttp {
    private static final String SECRET_ID = "ID";
    private static final String SECRET_KEY = "KEY";

    private static final String HOST = "ocr.tencentcloudapi.com";
    private static final String ENDPOINT = "https://" + HOST + "/";
    private static final String SERVICE = "ocr";
    private static final String ACTION = "IDCardOCR";
    private static final String VERSION = "2018-11-19";
    private static final String REGION = "ap-guangzhou";

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final OkHttpClient client = new OkHttpClient();

    public interface Callback {
        void onSuccess(String json);
        void onFailure(String error);
    }

    public static void idCardOcr(String imageBase64, String cardSide, Callback cb) {
        try {
            long ts = System.currentTimeMillis() / 1000;

            // 1) 组装请求体（注意：要和签名用的 payload 完全一致）
            JSONObject body = new JSONObject();
            body.put("ImageBase64", imageBase64);
            body.put("CardSide", cardSide);

            JSONObject cfg = new JSONObject();
            cfg.put("CopyWarn", true);
            cfg.put("ReshootWarn", true);
            cfg.put("DetectPsWarn", true);
            cfg.put("TempIdWarn", true);
            cfg.put("InvalidDateWarn", true);
            cfg.put("Quality", true);
            body.put("Config", cfg.toString());

            String payload = body.toString();

            String authorization = SignUtil.buildAuthorization(
                    SECRET_ID, SECRET_KEY, SERVICE, HOST, ACTION, ts, payload
            );

            Request request = new Request.Builder()
                    .url(ENDPOINT)
                    .post(RequestBody.create(JSON, payload))
                    .addHeader("Authorization", authorization)
                    .addHeader("Content-Type", "application/json; charset=utf-8")
                    .addHeader("Host", HOST)
                    .addHeader("X-TC-Action", ACTION)
                    .addHeader("X-TC-Version", VERSION)
                    .addHeader("X-TC-Timestamp", String.valueOf(ts))
                    .addHeader("X-TC-Region", REGION)
                    .build();

            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(okhttp3.Call call, java.io.IOException e) {
                    if (cb != null) cb.onFailure("network failed: " + e);
                }

                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) {
                    try {
                        String resp = response.body() != null ? response.body().string() : "";
                        if (!response.isSuccessful()) {
                            if (cb != null) cb.onFailure("HTTP " + response.code() + " => " + resp);
                            return;
                        }
                        if (cb != null) cb.onSuccess(resp);
                    } catch (Exception ex) {
                        if (cb != null) cb.onFailure("read response failed: " + ex);
                    } finally {
                        response.close();
                    }
                }
            });

        } catch (Exception e) {
            if (cb != null) cb.onFailure("build request failed: " + e);
        }
    }
}
