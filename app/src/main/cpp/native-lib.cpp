#include <jni.h>
#include <string>
#include "LogKit.h"

#include <iostream>
#include <dirent.h>


//图片
#include <android/bitmap.h>

//线程
#include <pthread.h>

// Java + 包名 + 类名 + 方法名
extern "C" {
using namespace std;

static JavaVM *gJavaVM;

//图片相关
void bitmapInfo(JNIEnv *env, jobject instance, jobject bitmap) {
	if (NULL == bitmap) {
		LOGE("bitmap is null!");
		return;
	}
	AndroidBitmapInfo info; // create a AndroidBitmapInfo
	int result;
	// 获取图片信息
	result = AndroidBitmap_getInfo(env, bitmap, &info);
	if (result != ANDROID_BITMAP_RESULT_SUCCESS) {
		LOGE("AndroidBitmap_getInfo failed, result: %d", result);
		return;
	}
	LOGD("bitmap width: %d, height: %d, format: %d, stride: %d", info.width, info.height,
	     info.format, info.stride);
	// 获取像素信息
	unsigned char *addrPtr;
	result = AndroidBitmap_lockPixels(env, bitmap, reinterpret_cast<void **>(&addrPtr));
	if (result != ANDROID_BITMAP_RESULT_SUCCESS) {
		LOGE("AndroidBitmap_lockPixels failed, result: %d", result);
		return;
	}
	// 执行图片操作的逻辑
	int length = info.stride * info.height;
	for (int i = 0; i < length; ++i) {
		LOGD("value: %x", addrPtr[i]);
	}
	// 像素信息不再使用后需要解除锁定
	result = AndroidBitmap_unlockPixels(env, bitmap);
	if (result != ANDROID_BITMAP_RESULT_SUCCESS) {
		LOGE("AndroidBitmap_unlockPixels failed, result: %d", result);
	}
}


//文件遍历
void showAllFiles(string dir_name) {
	// check the parameter
	if (dir_name.empty()) {
		LOGE("dir_name is null !");
		return;
	}
	DIR *dir = opendir(dir_name.c_str());
	// check is dir ?
	if (NULL == dir) {
		LOGE("Can not open dir. Check path or permission!");
		return;
	}
	struct dirent *file;
	// read all the files in dir
	while ((file = readdir(dir)) != NULL) {
		// skip "." and ".."
		if (strcmp(file->d_name, ".") == 0 || strcmp(file->d_name, "..") == 0) {
			LOGI("ignore . and ..");
			continue;
		}
		if (file->d_type == DT_DIR) {
			string filePath = dir_name + "/" + file->d_name;
			showAllFiles(filePath); // 递归执行
		} else {
			// 如果需要把路径保存到集合中去，就在这里执行 add 的操作
			LOGI("filePath: %s/%s", dir_name.c_str(), file->d_name);
		}
	}
	closedir(dir);
}


JNIEXPORT jstring JNICALL Java_me_shetj_sdk_ffmepg_demo_MainActivity_stringFromJNI(
		JNIEnv *env,
		jobject /* this */) {
	std::string hello = "Hello from C++";
	jint version = env->GetVersion();
	//输出日志
	LOGI("获取env的版本 = %d", version);
	//查找系统类，注意 FindClass 不要 L和;
	jclass mStringClass = env->FindClass("java/lang/String");
	//获取类的父类
	jclass clazz = env->GetSuperclass(mStringClass); // clazz is Ljava/lang/Object;

	//删除本地引用
	(env)->DeleteLocalRef(mStringClass);
	(env)->DeleteLocalRef(clazz);

	//抛出异常== JNI_OK
	//	env->ThrowNew(env->FindClass("java/io/IOException"), "IO异常") ;



	return env->NewStringUTF(hello.c_str());
}

/**
 * 测试线程
 * @param data
 * @return
 */

static void *test(void *data) {
	int status;
	JNIEnv *env;
	bool isAttached = false;

	status = gJavaVM->GetEnv((void **)(&env), JNI_VERSION_1_6);
	if (status == JNI_EDETACHED) {
		//将当前线程附着在java虚拟机上
		status = gJavaVM->AttachCurrentThread(&env, nullptr);
		if (status != JNI_OK) {
			LOGE("Failed to attach current thread");
			return nullptr;
		}
		LOGI("线程启动执行中");
		isAttached = true;
	}

	if(isAttached) {
		//将当前线程从java虚拟机上分离
		gJavaVM->DetachCurrentThread();
	}
	return nullptr;
}



void createThread() {
	pthread_t thread;
	/**
	 * 四个参数：
	 * 1. 指向线程标识符的指针
	 * 2. 设置线程属性
	 * 3. 线程运行函数的起始地址
	 * 4. 运行函数的参数
	 */
	int result = pthread_create(&thread, nullptr, test, nullptr);
	if (result != 0) {
		LOGE("线程启动失败");
	}else{
		LOGI("线程启动成功");
	}
}

extern "C" JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
	LOGI("JNI load---------");

	JNIEnv *env;
	if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
		return JNI_ERR;
	}
	//保存全局变量
	gJavaVM = vm;

	createThread();

	return JNI_VERSION_1_6;
}







}

