//
// Created by stj on 2021/11/4.
//

#ifndef ANDROID_FFMPEG_REGISTER_H
#define ANDROID_FFMPEG_REGISTER_H

typedef struct jni_rect_t {
	int left;
	int top;
	int right;
	int bottom;
} jni_rect;

typedef struct jni_point_t {
	float x;
	float y;
} jni_point;

typedef struct jni_data_bean_t {
	jni_rect rect; // Rect
	jni_point points[4]; // PointF[]
	const char *message; // String
	int id; // int
	float score; // float
	signed char data[4]; // byte[]
	int double_dimen_array[2][2]; // int[][]
} jni_data_bean;



// 对应 android.graphics.Rect 类
typedef struct rect_block_t {
	jclass clazz;
	jfieldID left;
	jfieldID top;
	jfieldID right;
	jfieldID bottom;
	jmethodID constructor;
} rect_block;

// 对应 android.graphics.PointF 类
typedef struct point_block_t {
	jclass clazz;
	jfieldID x;
	jfieldID y;
	jmethodID constructor;
} point_block;

// 对应 com.afei.jnidemo.DataBean$Inner 类
typedef struct inner_block_t {
	jclass clazz;
	jfieldID message;
	jmethodID constructor;
} inner_block;

// 对应 com.afei.jnidemo.DataBean 类
typedef struct data_bean_block_t {
	jclass clazz;
	jfieldID rect;
	jfieldID points;
	jfieldID inner;

	jfieldID id;
	jfieldID score;
	jfieldID data;
	jfieldID double_dimen_array;

	jmethodID constructor;
} data_bean_block;


// 注册
void register_classes(JNIEnv *env);

// C结构体转Java类
jobject data_bean_c_to_java(JNIEnv *env, jni_data_bean *data_bean);

// Java类转C结构体
void data_bean_java_to_c(JNIEnv *env, jobject data_bean_in, jni_data_bean *data_bean_out);

rect_block m_rect_block;
point_block m_point_block;
inner_block m_inner_block;
data_bean_block m_data_bean_block;

#endif //ANDROID_FFMPEG_REGISTER_H