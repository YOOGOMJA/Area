#include <jni.h>
#include <string>

extern "C"
JNIEXPORT jstring JNICALL
Java_mjh_v01_aproject_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
