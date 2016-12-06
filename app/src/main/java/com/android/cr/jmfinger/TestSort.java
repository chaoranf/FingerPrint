package com.android.cr.jmfinger;

import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Created by chaoranf on 16/11/30.
 */

public class TestSort {

    public static void main(String[] args) {
        test();
    }

    private static void test() {
        Map<String, String> map = new IdentityHashMap<>();
        map.put(new String("1"), "1");
        map.put(new String("a"), "1");
        map.put(new String("1"), "2");
        map.put(new String("b"), "2");
        map.put(new String("Abbb"), "3");
        map.put(new String("AB"), "4");
        map.put(new String("B"), "4");
        map.put(new String("123123"), "4");
        map.put(new String("321"), "4");
        map.put(new String("312"), "4");
        map.put(new String("asdfadsfads"), "4");
        map.put(new String("bA"), "4");
        map.put(new String("b2"), "4");
        map.put(new String("C"), "4");

        logn("before==================");
        long cur = System.currentTimeMillis();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            logn(entry.getKey() + "=" + entry.getValue());
        }
        String[] keys = map.keySet().toArray(new String[0]);
        Arrays.sort(keys);
        logn("after==================");
        for (String str : keys) {
            logn(str + "=" + map.get(str));
        }
        logn(System.currentTimeMillis() - cur + "");

    }

    private static void log(String str) {
        System.out.print(str);
    }

    private static void logn(String str) {
        System.out.println(str);
    }
}
