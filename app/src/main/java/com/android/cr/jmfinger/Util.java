package com.android.cr.jmfinger;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StatFs;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * Created by chaoranf on 16/10/28.
 */

public class Util {

    private static class SingleLoader {
        private static final Util INSTANCE = new Util();
    }

    private Util() {
    }

    public static final Util getInstance() {
        return SingleLoader.INSTANCE;
    }

    private Map<String, String> mFinger = new HashMap<>();

    public Map<String, String> getFingerMap() {
        return mFinger;
    }

    private Context mContext;
    private List<Pair<String, String>> listItem;
    private String _BRAND, _HARDWARE, _MODEL, _DISPLAY, _CPU_ABI, _CPU_INFO,
            _TOTALMEM, _AVAILMEM, _TOTALROM, _AVAILROM,
            _TOTALSDCARD = "Can't Read SDCard",
            _AVAILSDCARD = "Can't Read SDCard", _BATCAPACITY, _BATLEVEL,
            _BACK_CAMERA = "Not  Available", _FRONT_CAMERA = "Not Available",
            _NETWORKTYPE = "No Sim Card", _IMEI, _IMSI, _RELEASE,
            _OSNAME = "Unknowed", _API, _SCREENRES, _DPI,
            _OPERATOR = "No Sim Card";

    public String fetchInfo(Context context) {
        mContext = context;

        // Get device & android system info
        androidSystem();
        // get Processor RAM info
        CPU_RAM();
        // get storage info
        storage();
        // get display info
        display();
        // get camera info
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                backCamera();
                frontCamera();
            } catch (Exception ex) {
                showTip("没有获取摄像头权限");
                ex.printStackTrace();
            }
        }
        // get battery info
        battery();
        // get telephony manager info
        telephonyManager();

        String[] listLeft = {"_BRAND", "_HARDWARE", "_MODEL", "_DISPLAY", "_RELEASE",
                "_OSNAME", "_API", "_CPU_ABI", "_CPU_INFO", "_TOTALMEM", "_AVAILMEM",
                "_TOTALROM", "_AVAILROM", "_TOTALSDCARD", "_AVAILSDCARD",
                "_SCREENRES", "_DPI", "_BACK_CAMERA", "_FRONT_CAMERA", " _BATCAPACITY",
                "_BATLEVEL", "_NETWORKTYPE", "_IMEI", "_IMSI", "_OPERATOR"};
        // Add Item to ListView
        String[] listRight = {_BRAND, _HARDWARE, _MODEL, _DISPLAY, _RELEASE,
                _OSNAME, _API, _CPU_ABI, _CPU_INFO, _TOTALMEM, _AVAILMEM,
                _TOTALROM, _AVAILROM, _TOTALSDCARD, _AVAILSDCARD,
                _SCREENRES, _DPI, _BACK_CAMERA, _FRONT_CAMERA, _BATCAPACITY,
                _BATLEVEL, _NETWORKTYPE, _IMEI, _IMSI, _OPERATOR};
        listItem = new ArrayList();
        Pair<String, String> inside = null;
        for (int i = 0; i < listLeft.length; i++) {
            inside = new Pair(listLeft[i], listRight[i]);
            listItem.add(inside);
            Log.e("testff", listLeft[i] + ":" + listRight[i]);
        }
        Log.e("testff", "共有" + listLeft.length + "条数据");

        staticParamMd5 = setStaticParamMd5("");
        dynamicParamMd5 = setDynamicParamMd5("");
        mFinger.put(Constant.FINGER, staticParamMd5);
        mFinger.put(Constant.FINGER_DYNAMIC, dynamicParamMd5);
        mFinger.put(Constant.DEVICEINFO, "all need device_info: for example brand " + _BRAND);
        return listItem.toString();

    }

    /**
     * 每个用户基本不会变的一些参数的算法md5值
     * 这个值作为基本的finger
     */
    private String staticParamMd5;
    /**
     * 用户基本时刻都在变化的参数的算法md5值
     */
    private String dynamicParamMd5;

    /**
     * 参数选取原则，选择信息熵较大并且较为重要的参数
     * <p>
     * 针对不同厂商，第一维度
     * _BRAND,一般都能读到，信息熵足够，碎片化的厂商
     * _TOTALROM,信息熵巨大，碎片化的厂商
     * _CPU_ABI,信息熵较大，区别不同cpu
     * _FRONT_CAMERA,信息熵较大，区别同一厂商不同手机
     * _BACK_CAMERA,信息熵较大，区别同一厂商不同手机
     * _TOTALMEM,信息熵较大，区别同一厂商不同手机
     * <p>
     * 针对不同版本，第二维度
     * _RELEASE，sdk版本
     * <p>
     * 针对不同用户，第三维度（还可细分，按照存储、显示、用户信息来分为三部分）
     * _TOTALSDCARD,信息熵较大，碎片化的用户
     * _IMEI,_IMEI,信息熵巨大，
     * imei本来是工信部为每部手机贴的入网签，可惜，在识别的时候往往有很多坑比手机读不到
     * imsi本来是识别每个sim卡的，可惜，同样也有很多读不到的（可以读到哪个运营商）
     * <p>
     * 针对
     * <p>
     * <p>
     * 算法选取原则，自己设定的一套不为他知的协议，通过想过的协议做CS两端的有效校验
     * 和服务器约定好算法
     * <p>
     * md5和相关数据的存储策略，未注册用户，可用md5值为数据库表的primary key，添加数据；
     * 这些数据是一个用户的基本不变数据，可以作为校验用户唯一性的数据
     * 该md5，可以设定在T时间内，可变换N次，来保证安全性
     *
     * @param apiParam 服务器加的盐，控制参数变化，以防算法被破解
     *                 通过盐来控制，登陆有效期
     */
    private String setStaticParamMd5(String apiParam) {
        StringBuilder sb = new StringBuilder();
        sb.append(_BRAND);
        sb.append(_TOTALROM);
        sb.append(_CPU_ABI);
        sb.append(_FRONT_CAMERA);
        sb.append(_BACK_CAMERA);
        sb.append(_TOTALMEM);

        sb.append(_RELEASE);

        sb.append(_TOTALSDCARD);
        sb.append(_IMEI);
        sb.append(_IMSI);

        sb.append(apiParam);

        return MD5.toMD5(sb.toString());
    }
    //其中一种假想，对于未登录用户，，不会发生购买行为，所以不是有效的商业用户，
    // 可以对每个md5做记录，每个不同的md5，都可以记做一个不同的设备
    // 关于md5的信息量，完全可以放心，128位的摘要，可以用来表示3.40e+38这么多个用户
    // 应该够用了吧？如果害怕扩展不够用可以用sha，来个512位的，但是相应的会带来性能的消耗，完全没必要

    //针对登陆用户，以uid为key存储，其中保持一个不加盐的md5，加盐的md5用来在内存中更快判断
    //服务器做判断，缓存或内存中会有用户不加盐的md5，先对比，不一致做不合格处理；一致，在做有限期加盐认证

    /**
     * 一些动态变化的参数，不要区分维度，叠加就好，用来动态验证用户合法性
     * _AVAILROM，信息熵巨大，碎片化的用户
     * _AVAILSDCARD,信息熵较大，碎片化的用户
     * _BATLEVEL,信息熵较大，短期内变化不会特别大(需要评估现在的快充时间)
     * _AVAILMEM,信息熵巨大，基本不会一样
     * 和服务器约定好算法
     * 如果这个md5，两次请求一模一样，，基本上可以判断用户为非法用户（模拟攻击之类）
     * 当然也有倒霉的用户刚好一模一样，，这个概率小到中500W
     *
     * @param apiParam 服务器加的盐，控制参数变化，以防算法被破解
     *                 通过盐来控制，登陆有效期
     */
    private String setDynamicParamMd5(String apiParam) {
        StringBuilder sb = new StringBuilder();
        sb.append(_AVAILROM);
        sb.append(_AVAILSDCARD);
        sb.append(_BATLEVEL);
        sb.append(_AVAILMEM);
        return MD5.toMD5(sb.toString());
    }

    public String getStaticParamMd5() {
        return staticParamMd5;
    }

    public String getDynamicParamMd5() {
        return dynamicParamMd5;
    }

    // Android System
    private void androidSystem() {
        _RELEASE = android.os.Build.VERSION.RELEASE;
        _MODEL = android.os.Build.MODEL;
        _BRAND = android.os.Build.BRAND;
        _DISPLAY = android.os.Build.DISPLAY;
        _CPU_ABI = android.os.Build.CPU_ABI;
        _HARDWARE = android.os.Build.HARDWARE;
        _API = String.valueOf(android.os.Build.VERSION.SDK_INT);
        StringBuilder _OSName = new StringBuilder();
        Field[] fields = Build.VERSION_CODES.class.getFields();
        for (Field field : fields) {
            String fieldName = field.getName();
            int fieldValue = -1;
            try {
                fieldValue = field.getInt(new Object());
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            if (fieldValue == Build.VERSION.SDK_INT) {
                _OSNAME = String.valueOf(_OSName.append(fieldName));
            }
        }
    }

    // CPU & RAM
    private void CPU_RAM() {
        // CPU
        String _CPUVersion;
        if (Build.VERSION.SDK_INT >= 17) {
            _CPU_INFO = String.valueOf(Runtime.getRuntime()
                    .availableProcessors());
        } else { // old version
            _CPUVersion = getCPUVersion();
            _CPU_INFO = getCPUInfo() + " " + _CPUVersion;
        }
        // RAM
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        long availableMemory = memoryInfo.availMem / 1048576L;
        long totalMemory;
        if (Build.VERSION.SDK_INT >= 16) {
            totalMemory = (memoryInfo.totalMem + availableMemory) / 1048576L;
        } else {
            totalMemory = Long.parseLong(getTotalRAM());
            totalMemory = (totalMemory + availableMemory) / 1048576L;
        }
        _AVAILMEM = String.valueOf(availableMemory) + " MB";
        _TOTALMEM = String.valueOf(totalMemory) + " MB";
    }

    // CPU Old Version
    public String getCPUInfo() {
        RandomAccessFile reader = null;
        String load = null;
        try {
            reader = new RandomAccessFile("/proc/cpuinfo", "r");
            load = reader.readLine();
            reader.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return load;
    }

    public String getCPUVersion() {
        RandomAccessFile reader = null;
        String load = null;
        try {
            reader = new RandomAccessFile("/proc/version", "r");
            load = reader.readLine();
            reader.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return load;
    }

    // RAM Old Version
    public String getTotalRAM() {
        RandomAccessFile reader = null;
        String load = null;
        try {
            reader = new RandomAccessFile("/proc/meminfo", "r");
            load = reader.readLine();
            reader.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return load;
    }

    private void storage() {
        // Internal Storage (ROM)
        long TotalROM = new File(mContext.getFilesDir().getAbsoluteFile().toString())
                .getTotalSpace();
        long AvailROM = new File(mContext.getFilesDir().getAbsoluteFile().toString())
                .getFreeSpace();
        _TOTALROM = String.valueOf(TotalROM / 1048576L) + " MB";
        _AVAILROM = String.valueOf(AvailROM / 1048576L) + " MB";

        // External Storage
        String state = Environment.getExternalStorageState();
        if (android.os.Environment.MEDIA_MOUNTED.equals(state)) {
            if (Build.VERSION.SDK_INT >= 19) {
                File externalStorageDir = Environment.getExternalStorageDirectory();
                long AvailSDCard = externalStorageDir.getFreeSpace() / 1024 / 1024;
                long TotalSDCard = externalStorageDir.getTotalSpace() / 1024 / 1024;
                _TOTALSDCARD = String.valueOf(TotalSDCard + " MB");
                _AVAILSDCARD = String.valueOf(AvailSDCard + " MB");
            } else {
                getTotalExternalMemorySize();
                getAvailableExternalMemorySize();
            }
        }
    }

    // External Storage Old Version
    public boolean externalMemoryAvailable() {
        return android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);
    }

    @SuppressWarnings("deprecation")
    public void getAvailableExternalMemorySize() {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long availableBlocks = stat.getAvailableBlocks();
            _AVAILSDCARD = String.valueOf(formatSize(availableBlocks * blockSize));
        }
    }

    @SuppressWarnings("deprecation")
    public void getTotalExternalMemorySize() {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long totalBlocks = stat.getBlockCount();
            _TOTALSDCARD = String.valueOf(formatSize(totalBlocks * blockSize));
        }
    }

    public static String formatSize(long size) {
        String suffix = null;

        if (size >= 1024) {
            suffix = "KB";
            size /= 1024;
            if (size >= 1024) {
                suffix = "MB";
                size /= 1024;
            }
        }

        StringBuilder resultBuffer = new StringBuilder(Long.toString(size));

        int commaOffset = resultBuffer.length() - 3;
        while (commaOffset > 0) {
            resultBuffer.insert(commaOffset, ',');
            commaOffset -= 3;
        }

        if (suffix != null) resultBuffer.append(suffix);
        return resultBuffer.toString();
    }

    // Display
    private void display() {
        WindowManager wm = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE));
        Display display = wm.getDefaultDisplay();
        Point myPoint = getDisplaySize(display);
        _SCREENRES = String.valueOf(myPoint.y) + "x"
                + String.valueOf(myPoint.x) + " pixel";
        DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
        int densityDpi = dm.densityDpi;
        _DPI = String.valueOf(densityDpi) + " pixel/inch";

    }

    @SuppressWarnings("deprecation")
    private static Point getDisplaySize(final Display display) {
        Point point = new Point();
        if (Build.VERSION.SDK_INT >= 13) {
            display.getSize(point);
        } else {
            point.x = display.getWidth();
            point.y = display.getHeight();
        }
        return point;
    }

    // Camera
    // Back Camera
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void backCamera() {
        PackageManager packageManager = mContext.getPackageManager();
        int checkCamera = Camera.getNumberOfCameras();
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)
                && checkCamera > 0) {
            Camera camera = Camera.open(0); // For Back Camera
            android.hardware.Camera.Parameters params = camera.getParameters();
            List<Camera.Size> sizes = params.getSupportedPictureSizes();
            Camera.Size result = null;

            ArrayList<Integer> arrayListForWidth = new ArrayList<Integer>();
            ArrayList<Integer> arrayListForHeight = new ArrayList<Integer>();

            for (int i = 0; i < sizes.size(); i++) {
                result = sizes.get(i);
                arrayListForWidth.add(result.width);
                arrayListForHeight.add(result.height);
            }
            if (arrayListForWidth.size() != 0 && arrayListForHeight.size() != 0) {
                float back = (((Collections.max(arrayListForWidth)) * (Collections
                        .max(arrayListForHeight))) / (1024 * 1024.0f));
                DecimalFormat df = new DecimalFormat("#.#");
                _BACK_CAMERA = String.valueOf(df.format(back)) + " MP";
            }
            camera.release();
            arrayListForWidth.clear();
            arrayListForHeight.clear();

        }
    }

    // Front Camera
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void frontCamera() {
        PackageManager packageManager = mContext.getPackageManager();
        int checkCamera = Camera.getNumberOfCameras();
        if (packageManager
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)
                && checkCamera > 1) {
            Camera camera = Camera.open(1); // For Front Camera
            android.hardware.Camera.Parameters params1 = camera.getParameters();
            List<Camera.Size> sizes1 = params1.getSupportedPictureSizes();
            Camera.Size result1 = null;

            ArrayList<Integer> arrayListForWidth = new ArrayList<Integer>();
            ArrayList<Integer> arrayListForHeight = new ArrayList<Integer>();

            for (int i = 0; i < sizes1.size(); i++) {
                result1 = sizes1.get(i);
                arrayListForWidth.add(result1.width);
                arrayListForHeight.add(result1.height);
            }
            if (arrayListForWidth.size() != 0 && arrayListForHeight.size() != 0) {
                float front = (((Collections.max(arrayListForWidth)) * (Collections
                        .max(arrayListForHeight))) / (1024 * 1024.0f));
                DecimalFormat df = new DecimalFormat("#.#");
                _FRONT_CAMERA = String.valueOf(df.format(front)) + " MP";
            }
            camera.release();
        }
    }

    // Battery
    private void battery() {
        _BATCAPACITY = getBatteryCapacity() + " mAh";
        Intent batteryIntent = mContext.registerReceiver(null,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int rawlevel = batteryIntent.getIntExtra("level", -1);
        double scale = batteryIntent.getIntExtra("scale", -1);

        _BATLEVEL = "Unknowed";
        if (rawlevel >= 0 && scale > 0) {
            _BATLEVEL = String.valueOf(rawlevel / scale * 100) + " %";
        }
    }

    // Battery Capacity
    public String getBatteryCapacity() {
        Object mPowerProfile_ = null;
        String capacity = "";
        final String POWER_PROFILE_CLASS = "com.android.internal.os.PowerProfile";
        try {
            mPowerProfile_ = Class.forName(POWER_PROFILE_CLASS)
                    .getConstructor(Context.class).newInstance(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            double batteryCapacity = (Double) Class
                    .forName(POWER_PROFILE_CLASS)
                    .getMethod("getAveragePower", java.lang.String.class)
                    .invoke(mPowerProfile_, "battery.capacity");
            capacity = String.valueOf(batteryCapacity);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return capacity;
    }

    // Telephony Manager
    // IMEI, IMSI, Network Operator Name, Network Type
    private void telephonyManager() {
        String serviceName = Context.TELEPHONY_SERVICE;
        TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(serviceName);
        if (telephonyManager.getDeviceId() != null) {
            _IMEI = telephonyManager.getDeviceId();
        } else {
            _IMEI = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
        }
        _IMSI = telephonyManager.getSubscriberId();
        if (!telephonyManager.getNetworkOperatorName().equals(null)) {
            _OPERATOR = telephonyManager.getNetworkOperatorName();
            _NETWORKTYPE = getNetworkType();
        }
    }

    // Network Type
    public String getNetworkType() {
        TelephonyManager mTelephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        int networkType = mTelephonyManager.getNetworkType();
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "2G";
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "3G";
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "4G";
            default:
                return "Unknown";
        }
    }

    private static final Handler mHandler = new Handler(Looper.getMainLooper());

    public static void showTip(final String msg) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(JmFingerApplication.appContext, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
