#include <jni.h>
#include <string>

extern "C"
jstring
Java_com_android_cr_jmfinger_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
jstring
Java_com_android_cr_jmfinger_MainActivity_phoneParamters(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "phone atom";
    return env->NewStringUTF(hello.c_str());
}
