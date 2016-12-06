package com.android.cr.jmfinger.net;

import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

/**
 * 从OKHttp3连接中，接出Cookie来自行处理
 */
public class OkCookieJar implements CookieJar {
    @Override
    public void saveFromResponse(HttpUrl httpUrl, List<Cookie> list) {
        OkCookieStorage.getInstance().updateCookiesForCookieJar(list);
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl httpUrl) {
        return OkCookieStorage.getInstance().getCookiesForCookieJar();
    }
}
