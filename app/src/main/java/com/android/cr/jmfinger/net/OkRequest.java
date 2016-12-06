package com.android.cr.jmfinger.net;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by chaoranf on 16/11/17.
 */

public class OkRequest {

    private final String TAG = "test_request";
    @NonNull
    private String url;
    @Nullable
    private String method;
    @Nullable
    private Map<String, String> param;
    @Nullable
    private String tag;
    @Nullable
    private OkListener listener;
    @Nullable
    private OkDataParse dataParse;
    @NonNull
    private boolean isHttps;
    @Nullable
    private boolean isUseCache;
    @Nullable
    private OkError okError;

    private OkRequest(Builder builder) {
        url = builder.url;
        method = builder.method;
        param = builder.param;
        tag = builder.tag;
        listener = builder.listener;
        dataParse = builder.dataParse;
        isHttps = builder.isHttps;
        isUseCache = builder.isUseCache;
    }

    public void execute() {
        OkHttpClient client = OkHttpClientHolder.getInstance().getClient(isHttps);
        Request.Builder requestBuilder = new Request.Builder();
        setBuilderParam(requestBuilder);
        Request request = requestBuilder.build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //io异常或者用户cancel
                okError = new OkError(-3, e.getMessage());
                handleError();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //只要网络连通，都会走这里，比如网络连接成功。需要注意statusCode == 403，也是网络连通的一种情况
                //这里的网络连通指的是客户端是否拥有接入网络能力
                //这里还是子线程哦
                handleResponse(response);
            }
        });
    }

    private void handleError() {
        loge(okError.toString());
        if (listener != null) {
            listener.onError(okError);
        }
    }

    private void handleResponse(Response response) {
        loge("onResponse" + response.code());
        if (response.code() == 200) {
            //200认为是成功的回调，可以认为是成功的网络请求
            if (dataParse != null) {
                try {
                    dataParse.onParse(response.body());
                } catch (Exception ex) {
                    okError = new OkError(-2, "data parse error");
                }
            }
        } else {
            //其他statusCode，认为出现了网络问题
            okError = new OkError(-1, "api error " + response.code());
        }

        if (okError != null) {
            handleError();
            return;
        }

        if (listener != null) {
            listener.onResponse(dataParse);
        }
    }

    private void setBuilderParam(Request.Builder builder) {
        builder.headers(getRequestHeaders());
        if (TextUtils.isEmpty(method) || method.equals("GET")) {
            builder.url(getGetUrl());
            builder.get();
        } else {
            builder.url(url);
            builder.post(getRequestBody());
        }
        if (TextUtils.isEmpty(tag)) {

        } else {
            builder.tag(tag);
        }
        if (isUseCache) {
            //可以走标准的http协议，也可以走自己设定的
//            builder.cacheControl(new CacheControl.Builder().maxAge(60, TimeUnit.SECONDS).build());
        } else {
            builder.cacheControl(CacheControl.FORCE_NETWORK);
        }

    }

    private String getGetUrl() {
        if (param == null || param.isEmpty()) {
            return url;
        }
        StringBuilder sb = new StringBuilder(url);
        if (url.contains("?")) {
            sb.append("&");
        } else {
            sb.append("?");
        }
        for (Map.Entry<String, String> entry : param.entrySet()) {
            String value = entry.getValue();
            if (value == null || value.equals("") || value.equals("null")) {
                continue;
            } else {
                String realValue = null;
                try {
                    realValue = URLEncoder.encode(value, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    continue;
                }
                sb.append(entry.getKey()).append("=").append(realValue).append("&");
            }
        }

        sb.deleteCharAt(sb.length() - 1);
        String result = sb.toString();
        sb = null;
        return result;
    }

    private Headers getRequestHeaders() {
        Headers.Builder builder = new Headers.Builder();
        return builder.build();
    }

    private RequestBody getRequestBody() {
        FormBody.Builder formBodyBuilder = new FormBody.Builder();

        if (param == null || param.isEmpty()) {

        } else {
            for (Map.Entry<String, String> entry : param.entrySet()) {
                formBodyBuilder.add(entry.getKey(), entry.getValue());
            }
        }

        return formBodyBuilder.build();
    }

    private void loge(String content) {
        Log.e(TAG, content);
    }

    protected interface OkListener<T extends OkDataParse> {
        void onResponse(T data);

        void onError(OkError error);
    }

    public interface OkDataParse {
        void onParse(ResponseBody responseBody);
    }

    public static class OkError {
        public int code;
        public String message;

        public OkError(int code, String message) {
            this.code = code;
            this.message = message;
        }

        @Override
        public String toString() {
            return "OKRequest hanppen OkError: " + code + " , " + message;
        }
    }

    public static class OkTag {
        String tag_group;
        String tag_value;
    }

    public static final class Builder {
        private String url;
        private String method;
        private Map<String, String> param;
        private List<Cookie> cookie;
        private String tag;
        private OkListener listener;
        private OkDataParse dataParse;
        private boolean isHttps = false;
        private boolean isUseCache = false;

        public Builder() {
        }

        public Builder url(String val) {
            url = val;
            if (url.startsWith("https")) {
                isHttps(true);
            } else {
                isHttps(false);
            }
            return this;
        }

        public Builder method(String val) {
            method = val;
            return this;
        }

        public Builder param(Map<String, String> val) {
            param = val;
            return this;
        }

        public Builder tag(String val) {
            tag = val;
            return this;
        }

        public Builder listener(OkListener val) {
            listener = val;
            return this;
        }

        public Builder dataParse(OkDataParse val) {
            dataParse = val;
            return this;
        }

        private Builder isHttps(boolean val) {
            isHttps = val;
            return this;
        }

        public Builder isUseCache(boolean val) {
            isUseCache = val;
            return this;
        }

        public OkRequest build() {
            return new OkRequest(this);
        }
    }
}
