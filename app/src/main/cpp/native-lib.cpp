#include <jni.h>
#include <string>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>

using namespace cv;

extern "C"
JNIEXPORT void JNICALL
Java_mjh_v01_aproject_MyVideoView_ConvertRGBtoGray(JNIEnv *env, jobject instance,
                                                   jlong matAddrInput, jlong matAddrResult) {

    Mat &matInput = *(Mat *)matAddrInput;
    Mat &matResult = *(Mat *)matAddrResult;

    cvtColor(matInput, matResult, CV_RGBA2GRAY);
}