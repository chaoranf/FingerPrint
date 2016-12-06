package com.android.cr.jmfinger.net;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import okhttp3.Cookie;

/**
 * cookie的连接层持久化存储器<br/>
 * 需要把每个连接中的cookie放到一个地方，进行集中管理<br/>
 * 只做内存处理，持久化业务端正自己搞
 */
public class OkCookieStorage {

    private static class GuCookieStorageINSTANCE {
        private static OkCookieStorage instance = new OkCookieStorage();
    }

    public static OkCookieStorage getInstance() {
        OkCookieStorage ret = GuCookieStorageINSTANCE.instance;
        return ret;
    }

    private OkCookieStorage() {
    }

    private Map<String, Cookie> mCookies = new HashMap<>();

    /**
     * 批量更新,用于cookiejar
     *
     * @param list
     */
    public void updateCookiesForCookieJar(List<Cookie> list) {
        if (list != null) {
            for (Cookie cookie : list) {
                updateCookie(cookie.name(), cookie.value());
            }
        }
    }

    /**
     * 单个更新
     *
     * @param key
     * @param value
     */
    public void updateCookie(@NonNull String key, @NonNull String value) {
        if (TextUtils.isEmpty(key) || TextUtils.isEmpty(value)) {
            //连接层要求coookie必须有key,跟普通的cookie标准不一样
            return;
        }
        mCookies.put(key, buildCookie(key, value));
    }

    private Cookie buildCookie(String key, String value) {
        Cookie.Builder builder = new Cookie.Builder().name(key.trim()).value(value.trim()).domain
                ("gu.com");
        return builder.build();
    }

    @NonNull
    public List<Cookie> getCookiesForCookieJar() {
        List<Cookie> cookies = new ArrayList<>();
        if (mCookies != null) {
            Iterator iter = mCookies.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                Cookie cookie = (Cookie) entry.getValue();
                cookies.add(cookie);
            }
        }
        return cookies;
    }
}
