## Lame
- Lame官网（lame.sourceforge.io/index.php）

- LAME是一个高质量的MPEG音频层III (MP3)编码器，在LGPL下授权


### [Mp3Recorder](https://github.com/SheTieJun/Mp3Recorder)

### 方法说明
- 初始化：`lame_global_flags *lame  =lame_init();`
- 设置输入采样率：`lame_set_in_samplerate(lame,inSamplerate);`
- 设置输出采样率：`lame_set_out_samplerate(lame,outSamplerate);`
- 设置声道：`lame_set_num_channels(lame,inChannel);`
- 设置比特率：`lame_set_brate(lame,outBitrate);`
- 设置质量：`lame_set_quality(lame,quality);`
- 设置处理PCM:`lame_encode_buffer` 和`lame_encode_buffer_interleaved`
	- `lame_encode_buffer_interleaved`主要用来处理双声道
```C
 int CDECL lame_encode_buffer_interleaved(
     lame_global_flags*  gfp,           /* global context handlei    */
     short int           pcm[],         /* PCM data for left and right
                                              channel, interleaved     */
     int                 num_samples,   /* number of samples per channel,
                                           _not_ number of samples in
                                           pcm[]         */
     unsigned char*      mp3buf,        /* pointer to encoded MP3 stream */
     int                 mp3buf_size ); /* number of valid octets in this
                                              stream      */
```
- 清空缓存中的数据：`lame_encode_flush`
- 关闭:`lame_close(lame);`


[Lame-Detail-Args](https://doorxp.github.io/apple/2012/07/10/Lame-Detail-Args.html)
[Lame说明文档](https://www.jianshu.com/p/5208d6dcd7eb)


### 其他
#### 1. 设置滤波器
```C
    lame_set_lowpassfreq(lame,lowpassfreq);
    lame_set_highpassfreq(lame,highpassfreq);
```

#### 2. VBR模式
```C
    lame_set_VBR(lame,vbr_mtrh); //设置成vbr
    lame_set_VBR_q(lame,quality);
```