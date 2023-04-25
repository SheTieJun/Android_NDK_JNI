/*
 *  Copyright (c) 2019 The WebRTC project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

#ifndef MODULES_AUDIO_PROCESSING_NS_NS_COMMON_H_
#define MODULES_AUDIO_PROCESSING_NS_NS_COMMON_H_

#include <cstddef>

namespace webrtc {

    constexpr size_t kFftSize = 256;//采用256点FFT变换
    constexpr size_t kFftSizeBy2Plus1 = kFftSize / 2 + 1;
    constexpr size_t kNsFrameSize = 160;  //每帧时域信号包含160个采样点
    constexpr size_t kOverlapSize = kFftSize - kNsFrameSize;//两帧数据间的overlap为96个采样点

    constexpr int kShortStartupPhaseBlocks = 50;//信号处理开始的前50帧认为只有噪声，没有语音，此时，噪声估计通过每帧的信号与搭建噪声模型共同更新
    constexpr int kLongStartupPhaseBlocks = 200; //信号处理开始的前200帧认为只有噪声，此时利用分位数的方法进行噪声估计和更新
    constexpr int kFeatureUpdateWindowSize = 500; //用于建立信号模型时，直方图数据的更新。直方图每500帧进行统计后，重新更新。

    constexpr float kLtrFeatureThr = 0.5f; //LRT特征的初始门限值为0.5
    constexpr float kBinSizeLrt = 0.1f;//计算LRT时，每个bin的大小（步长）为0.1
    constexpr float kBinSizeSpecFlat = 0.05f;//计算频谱平坦度时，每个bin的大小（步长）为0.05
    constexpr float kBinSizeSpecDiff = 0.1f; //计算频谱差异时，每个bin的大小（步长）为0.1

}  // namespace webrtc

#endif  // MODULES_AUDIO_PROCESSING_NS_NS_COMMON_H_
