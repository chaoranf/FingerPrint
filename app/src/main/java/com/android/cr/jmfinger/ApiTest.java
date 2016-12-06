package com.android.cr.jmfinger;

import com.android.cr.jmfinger.net.BusinessRequest;
import com.android.cr.jmfinger.net.OkCookieStorage;

import java.util.Map;

/**
 * Created by chaoranf on 16/12/6.
 */

public class ApiTest {
    public static void commonRequest() {
        new BusinessRequest.Builder()
                .url("http://172.19.80.99:8080/request/")
                .method(BusinessRequest.MethodType.POST)
                .build()
                .execute();
    }

    public static void loginRequest(String uid) {
        OkCookieStorage.getInstance().updateCookie(Constant.UID, uid);
        new BusinessRequest.Builder()
                .url("http://172.19.80.99:8080/login/")
                .method(BusinessRequest.MethodType.POST)
                .build()
                .execute();
    }

    public static void synCookie(Map<String, String> cookies) {
        if (cookies == null || cookies.isEmpty()) return;
        for (Map.Entry<String, String> entry : cookies.entrySet()) {
            OkCookieStorage.getInstance().updateCookie(entry.getKey(), entry.getValue());
        }
    }
}
