package com.android.cr.jmfinger.net;

import okhttp3.Call;

/**
 * Created by chaoranf on 16/11/17.
 */

public class OkManager {

    private static class OkManagerINSTANCE {
        private static OkManager instance = new OkManager();
    }

    public static OkManager getInstance() {
        OkManager ret = OkManagerINSTANCE.instance;
        return ret;
    }

    private OkConfig mConfig = new OkConfig.Builder().build();

    public OkConfig getConfig() {
        return mConfig;
    }

    public void init(OkConfig okConfig) {
        mConfig = okConfig;
    }

    public void cancel(OkRequest.OkTag tag) {

        //1，这种方式比较傻，需要遍历
        for (Call call : OkHttpClientHolder.getInstance().getClientHttp().dispatcher().queuedCalls()) {
            if (tag == call.request().tag()) {
                call.cancel();
            }
        }
        for (Call call : OkHttpClientHolder.getInstance().getClientHttp().dispatcher().runningCalls()) {
            if (tag == call.request().tag()) {
                call.cancel();
            }
        }
        //2，添加请求的是后维护一个map，直接取对应tag的call，cancel;比较麻烦的是，需要动态的维护这个map
        //map的维护工作包括：1，当call完成时，map中remove掉；2，当call被取消的时候，map中remove掉；
        //3，当添加call时，需要在map中add
        //Call call = mMap.get(tag);
        //call.cancel();
    }

}
