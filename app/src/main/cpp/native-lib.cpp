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
			LOGV("ignore . and ..");
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

void data_bean_java_to_c(JNIEnv *env, jobject data_bean_in, jni_data_bean *data_bean_out) {
	if (data_bean_in == nullptr) {
		LOGW("input data is null!");
		return;
	}
	LOGD("start data_bean_java_to_c");

	// 1. assign rect
	jobject rect = env->GetObjectField(data_bean_in, m_data_bean_block.rect);
	data_bean_out->rect.left = env->GetIntField(rect, m_rect_block.left);
	data_bean_out->rect.top = env->GetIntField(rect, m_rect_block.top);
	data_bean_out->rect.right = env->GetIntField(rect, m_rect_block.right);
	data_bean_out->rect.bottom = env->GetIntField(rect, m_rect_block.bottom);

	// 2. point array
	jobjectArray point_array = (jobjectArray) env->GetObjectField(data_bean_in, m_data_bean_block.points);
	jsize len = env->GetArrayLength(point_array);
	// len = NELEM(data_bean_out->points);
	LOGD("point array len: %d", len); // 注意这个 len 必须等于 NELEM(data_bean_out->points)
	for (int i = 0; i < len; i++) {
		jobject point = env->GetObjectArrayElement(point_array, i);
		data_bean_out->points[i].x = env->GetFloatField(point, m_point_block.x);
		data_bean_out->points[i].y = env->GetFloatField(point, m_point_block.y);
	}

	// 3. inner class
	jobject inner = env->GetObjectField(data_bean_in, m_data_bean_block.inner);
	jstring message = (jstring) env->GetObjectField(inner, m_inner_block.message);
	data_bean_out->message = env->GetStringUTFChars(message, 0);

	// 4. other
	data_bean_out->id = env->GetIntField(data_bean_in, m_data_bean_block.id);
	data_bean_out->score = env->GetFloatField(data_bean_in, m_data_bean_block.score);
	// byte array
	jbyteArray byte_array = (jbyteArray) env->GetObjectField(data_bean_in, m_data_bean_block.data);
	jbyte *data = env->GetByteArrayElements(byte_array, 0);
	len = env->GetArrayLength(byte_array);
	LOGD("byte array len: %d", len);
	memcpy(data_bean_out->data, data, len * sizeof(jbyte));
	env->ReleaseByteArrayElements(byte_array, data, 0);
	// double dimen int array
	jobjectArray array = (jobjectArray) env->GetObjectField(data_bean_in, m_data_bean_block.double_dimen_array);
	len = env->GetArrayLength(array); // 获取行数
	LOGD("double dimen int array len: %d", len);
	for (int i = 0; i < len; i++) {
		jintArray sub_array = (jintArray) env->GetObjectArrayElement(array, i); // 这步得到的就是一维数组了
		jint *int_array = env->GetIntArrayElements(sub_array, 0);
		jsize sub_len = env->GetArrayLength(sub_array); // 获取列数
		LOGD("sub_len: %d", sub_len);
		memcpy(data_bean_out->double_dimen_array[i], int_array, sub_len * sizeof(jint));
		env->ReleaseIntArrayElements(sub_array, int_array, 0);
	}
	LOGD("end data_bean_java_to_c");
}


jobject data_bean_c_to_java(JNIEnv *env, jni_data_bean *c_data_bean) {
	if (c_data_bean == nullptr) {
		LOGW("input data is null!");
		return nullptr;
	}
	LOGD("start data_bean_c_to_java");

	// 1. create rect
	jobject rect = env->NewObject(m_rect_block.clazz, m_rect_block.constructor);
	env->SetIntField(rect, m_rect_block.left, c_data_bean->rect.left);
	env->SetIntField(rect, m_rect_block.top, c_data_bean->rect.top);
	env->SetIntField(rect, m_rect_block.right, c_data_bean->rect.right);
	env->SetIntField(rect, m_rect_block.bottom, c_data_bean->rect.bottom);

	// 2. point array
	jsize len = NELEM(c_data_bean->points);
	LOGD("point array len: %d", len);
	jobjectArray point_array = env->NewObjectArray(len, m_point_block.clazz, NULL);
	for (int i = 0; i < len; i++) {
		jobject point = env->NewObject(m_point_block.clazz, m_point_block.constructor);
		env->SetFloatField(point, m_point_block.x, c_data_bean->points[i].x);
		env->SetFloatField(point, m_point_block.y, c_data_bean->points[i].y);
		env->SetObjectArrayElement(point_array, i, point);
	}

	// 3. inner class
	jobject inner = env->NewObject(m_inner_block.clazz, m_inner_block.constructor);
	jstring message = env->NewStringUTF(c_data_bean->message);
	env->SetObjectField(inner, m_inner_block.message, message);

	// 4. DataBean class
	jobject java_data_bean = env->NewObject(m_data_bean_block.clazz, m_data_bean_block.constructor);
	env->SetObjectField(java_data_bean, m_data_bean_block.rect, rect);
	env->SetObjectField(java_data_bean, m_data_bean_block.points, point_array);
	env->SetObjectField(java_data_bean, m_data_bean_block.inner, inner);
	env->SetIntField(java_data_bean, m_data_bean_block.id, c_data_bean->id);
	env->SetFloatField(java_data_bean, m_data_bean_block.score, c_data_bean->score);
	// byte array
	len = NELEM(c_data_bean->data);
	LOGD("data array len: %d", len);
	jbyteArray data = env->NewByteArray(len);
	env->SetByteArrayRegion(data, 0, len, c_data_bean->data);
	env->SetObjectField(java_data_bean, m_data_bean_block.data, data);
	// double dimen int array
	len = NELEM(c_data_bean->double_dimen_array);
	LOGD("double dimen int array len: %d", len);
	jclass clazz = env->FindClass("[I"); // 一维数组的类
	jobjectArray double_dimen_array = env->NewObjectArray(len, clazz, NULL);
	for (int i = 0; i < len; i++) {
		jsize sub_len = NELEM(c_data_bean->double_dimen_array[i]);
		LOGD("sub_len: %d", sub_len);
		jintArray int_array = env->NewIntArray(sub_len);
		env->SetIntArrayRegion(int_array, 0, sub_len, c_data_bean->double_dimen_array[i]);
		env->SetObjectArrayElement(double_dimen_array, i, int_array);
	}
	env->SetObjectField(java_data_bean, m_data_bean_block.double_dimen_array, double_dimen_array);

	return java_data_bean;
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
	}
}

static void *test(void *data) {
	LOGI("test");
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

		isAttached = true;
	}

	printLog(env, "new Thread");

	if(isAttached) {
		//将当前线程从java虚拟机上分离
		gJavaVM->DetachCurrentThread();
	}
	return nullptr;
}





}

