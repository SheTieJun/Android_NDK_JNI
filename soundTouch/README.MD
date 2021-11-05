## SoundTouch

- soundtouch是一个音频处理库
- soundtouch 的主要功能功能：变音。  通过：音速、音调

- 官网(http://www.surina.net/soundtouch)
- 下载地址(https://codeberg.org/soundtouch/soundtouch)


## SoundTouch公开函数与参数的说明
- version:2.3.1
[SoundTouch.cpp](src/main/cpp/soundtouch/SoundTouch.cpp)
### 采样：
- [X] setChannels(int) 设置声道，1 = mono单声道, 2 = stereo立体声
- [X] setSampleRate(uint) 设置采样率
### 速率：
- [X] setRate(double) 指定播放速率，设置新的rate，源rate=1.0，小于1变慢；大于1变快
- [X] setTempo(double) 指定节拍，设置新的节拍tempo，源tempo=1.0，小于1则变慢；大于1变快
- [X] setRateChange(double)、setTempoChange(double) 在原速1.0基础上，按百分比做增量，取值(-50 .. +100 %)
### 音调：
setPitch(double) 指定音调值, 源pitch = 1.0，小于1音调变低；大于1音调变高
setPitchOctaves(double) 在源pitch的基础上，使用八度音(Octave)设置新的pitch [-1.00, 1.00]。
- [X] setPitchSemiTones(double) 在源pitch的基础上，使用半音(Semitones)设置新的pitch [-12.0,12.0]
以上调音函数根据乐理进行单位换算，最后进入相同的处理流程calcEffectiveRateAndTempo()。三个函数对参数没有上下界限限制，只是参数过大失真越大。SemiTone指半音，通常说的“降1个key”就是降低1个半音。所以我认为使用SemiTone为单位即可满足需求，并且容易理解。

### 处理：
- [X] putSamples(const SAMPLETYPE *samples, uint nSamples) 输入采样数据
- [X] receiveSamples(SAMPLETYPE *output, uint maxSamples) 输出处理后的数据，需要循环执行
- [X] flush() 冲出处理管道中的最后一组“残留”的数据，应在最后执行

### 速度和pitch参数的设置
#### 变调不变速
setPitch(double newPitch)
setPitchOctaves(double newPitch)
- [X] setPitchSemiTones(double or int newPitch)
#### 变速不变调
setRate(double newRate)
- [X] setRateChange(double newRate)
- [X] setTempo(double newTempo)
- [X] setTempoChange(double newTempo) 在源tempo的基础上，以百分比设置新的tempo[-50,100]
### SoundTouch实时处理音频流
- [X] ST对音频的处理是输入函数putSamples()与输出函数receiveSamples()。实时处理音频流的思路就是，循环读取音频数据段，放入ST进行输出，输出处理后的数据段用于播放。