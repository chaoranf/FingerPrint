package com.android.cr.jmfinger.net;

import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.CookieJar;
import okhttp3.OkHttpClient;

/**
 * 持久化OKHttpClient,避免重复创建线程池以及其他资源
 */
public class OkHttpClientHolder {
    private static class JMOKHttp3INSTANCE {
        private static OkHttpClientHolder instance = new OkHttpClientHolder();
    }

    public static OkHttpClientHolder getInstance() {
        return JMOKHttp3INSTANCE.instance;
    }

    private OkHttpClient mClientHttp = null;
    private OkHttpClient mClientHttps = null;

    private final static Object clientHttpLock = new Object();

    private final static Object clientHttpsLock = new Object();

    private CookieJar mCookieJar = new OkCookieJar();

    public OkHttpClient getClientHttp() {
        if (mClientHttp == null) {
            synchronized (clientHttpLock) {
                if (mClientHttp == null) {
                    mClientHttp = getBuilder(false).build();
                }
            }
        }
        return mClientHttp;
    }

    public OkHttpClient getClientHttps() {
        if (mClientHttps == null) {
            synchronized (clientHttpsLock) {
                if (mClientHttps == null) {
                    mClientHttps = getBuilder(true).build();
                }
            }
        }
        return mClientHttps;
    }

    public OkHttpClient getClient(boolean isHttps) {
        if (isHttps) {
            return getClientHttps();
        } else {
            return getClientHttp();
        }
    }

    private OkHttpClient.Builder getBuilder(boolean isHttps) {
        OkConfig config = OkManager.getInstance().getConfig();
        int readTimeOut = config.getTIMEOUT();
        int writeTimeOut = config.getTIMEOUT();
        int connTimeOut = config.getTIMEOUT();

        OkHttpClient.Builder builder = new OkHttpClient().newBuilder().readTimeout(readTimeOut,
                TimeUnit.SECONDS).writeTimeout(writeTimeOut, TimeUnit.SECONDS).connectTimeout
                (connTimeOut, TimeUnit.SECONDS).cookieJar(mCookieJar);
        if (isHttps) {
            SSLSocketFactory sslSocketFactory = getSSLSocketFactory();
            if (null != sslSocketFactory) {
                builder.sslSocketFactory(sslSocketFactory, new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{};
                    }
                });
            }
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
        }
        return builder;
    }

    private SSLSocketFactory client;

    public SSLSocketFactory getSSLSocketFactory() {

        if (client == null) {
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };
            try {
                // Install the all-trusting trust manager
                final SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
                // Create an ssl socket factory with our all-trusting manager
                client = sslContext.getSocketFactory();
            } catch (Exception e) {
            }
            return client;
        }

        return client;
    }
}
