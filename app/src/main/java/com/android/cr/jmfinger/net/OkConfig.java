package com.android.cr.jmfinger.net;

/**
 * Created by chaoranf on 16/11/17.
 */

public class OkConfig {
    private static final int DEFAULT_TIMEOUT = 10;
    private static final int DEFAULT_CACHE_SIZE = Integer.MAX_VALUE;
    private final int TIMEOUT;
    private final int CACHE_SIZE;

    private OkConfig(int time_out, int max_cache_size) {
        TIMEOUT = time_out;
        CACHE_SIZE = max_cache_size;
    }

    public int getTIMEOUT() {
        return TIMEOUT;
    }

    public int getCACHE_SIZE() {
        return CACHE_SIZE;
    }

    private OkConfig(Builder builder) {
        TIMEOUT = builder.TIMEOUT;
        CACHE_SIZE = builder.CACHE_SIZE;
    }

    public static final class Builder {
        private int TIMEOUT = DEFAULT_TIMEOUT;
        private int CACHE_SIZE = DEFAULT_CACHE_SIZE;

        public Builder() {
        }

        public Builder timeOut(int val) {
            TIMEOUT = val;
            return this;
        }

        public Builder cacheSize(int val) {
            CACHE_SIZE = val;
            return this;
        }

        public OkConfig build() {
            return new OkConfig(this);
        }
    }
}
