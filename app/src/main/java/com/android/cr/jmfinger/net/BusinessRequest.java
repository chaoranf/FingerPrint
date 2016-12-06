package com.android.cr.jmfinger.net;

import android.text.TextUtils;

import com.android.cr.jmfinger.Util;

import org.json.JSONObject;

import java.util.List;
import java.util.Map;

import okhttp3.Cookie;
import okhttp3.ResponseBody;

/**
 * Created by chaoranf on 16/11/17.
 */

public class BusinessRequest {
    private String url;
    private MethodType method;
    private BusData busData = new BusData();
    private Map<String, String> param;
    private boolean isUseCache = false;
    private ApiListener listener;
    private List<Cookie> cookie;

    private BusinessRequest(Builder builder) {
        url = builder.url;
        method = builder.method;
        if (null != builder.busData) {
            busData = builder.busData;
        }
        param = builder.param;
        isUseCache = builder.isUseCache;
        listener = builder.listener;
        cookie = builder.cookie;
    }

    public void execute() {
        new OkRequest.Builder()
                .url(url)
                .method(method.typeName)
                .dataParse(busData)
                .param(param)
                .isUseCache(isUseCache)
                .listener(new OkRequest.OkListener<BusData>() {
                    @Override
                    public void onResponse(BusData data) {
                        if (data != null && data.code == 0) {
                            showTip(data.message);
                            if (listener == null) {
                                return;
                            }
                            listener.onSuccess(data);
                        } else {
                            showTip("请求失败");
                            if (listener == null) {
                                return;
                            }
                            listener.onFail(data);
                        }
                    }

                    @Override
                    public void onError(OkRequest.OkError error) {
                        showTip("接口报错:" + error.message);
                        if (listener == null) {
                            return;
                        }
                        listener.onError(error);
                    }
                })
                .build().execute();
    }

    private void showTip(String msg) {
//        Toast.makeText(JmFingerApplication.appContext, msg, Toast.LENGTH_SHORT).show();
        Util.showTip(msg);
    }

    public static class BusData implements OkRequest.OkDataParse {
        public int code = -1;
        public String action;
        public String message;

        @Override
        public void onParse(ResponseBody response) {
            try {
                String str = new String(response.bytes(), "UTF-8");
                JSONObject jsonObject = new JSONObject(str);
                code = jsonObject.optInt("code");
                action = jsonObject.optString("action");
                message = jsonObject.optString("message");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public interface ApiListener<T extends BusData> {

        void onSuccess(T data);

        void onFail(T data);

        void onError(OkRequest.OkError error);
    }

    public enum MethodType {
        POST("POST"), GET("GET");
        public String typeName;

        MethodType(String name) {
            typeName = name;
        }
    }

    public static final class Builder {
        private String url;
        private MethodType method = MethodType.GET;
        private BusData busData;
        private Map<String, String> param;
        private boolean isUseCache;
        private ApiListener listener;
        private List<Cookie> cookie;

        public Builder url(String val) {
            url = val;
            return this;
        }

        public Builder method(MethodType val) {
            method = val;
            return this;
        }

        public Builder busData(BusData val) {
            busData = val;
            return this;
        }

        public Builder param(Map<String, String> val) {
            param = val;
            return this;
        }

        public Builder isUseCache(boolean val) {
            isUseCache = val;
            return this;
        }

        public Builder listener(ApiListener val) {
            listener = val;
            return this;
        }

        public Builder listener(List<Cookie> val) {
            cookie = val;
            return this;
        }

        public BusinessRequest build() {
            if (TextUtils.isEmpty(url)) {
                throw new RuntimeException("no url??? go to play egg!");
            } else {

            }
            return new BusinessRequest(this);
        }
    }
}
