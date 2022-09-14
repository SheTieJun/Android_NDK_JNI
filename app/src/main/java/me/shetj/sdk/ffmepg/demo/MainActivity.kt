package me.shetj.sdk.ffmepg.demo

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.shetj.ffmpeg.kt.FFmpegKit
import me.shetj.ffmpeg.kt.RunState
import me.shetj.ffmpeg.kt.buildCommand
import me.shetj.ffmpeg.kt.convertToCommand
import me.shetj.ndk.lame.LameUtils
import me.shetj.sdk.curl.CUrlKit
import me.shetj.sdk.curl.CurlHttp
import me.shetj.sdk.ffmepg.demo.databinding.ActivityMainBinding
import me.shetj.sdk.json.JsonKit
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val cacert by lazy {
        val path = cacheDir.resolve("cacert.pem")
        assets.open("cacert.pem").copyTo(FileOutputStream(path))
        path
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CurlHttp.initCurl()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Example of a call to a native method
        try {
            STKit.getInstance().init(2, 44100, 1f, 10f, 1f)
            LameUtils.init(44100, 1, 44100, 64, 3,3000,200,false)
            binding.sampleText.text = stringFromJNI() +
                    "\nLame:" + LameUtils.version() +
                    "\nSoundTouch:${STKit.getInstance().getVersion()}" +
                    "\n${CUrlKit.getVersion()}"
        } catch (e: Exception) {
            e.printStackTrace()
        }



        binding.test.setOnClickListener {
            CurlHttp.setCertificate(cacert)
            //TODO 需要在线程
            val testGet = CurlHttp.testGet()
            Log.i("testGet",testGet)
            Toast.makeText(this,testGet,Toast.LENGTH_SHORT).show()

            //
            val testPost = CurlHttp.testPost()
            Log.i("testPost",testPost)
            Toast.makeText(this,testPost,Toast.LENGTH_SHORT).show()
        }
        binding.testJson.setOnClickListener {

            JsonKit.test()
        }


        binding.ffmpegKit.setOnClickListener {
            lifecycleScope.launch {
                val command = "ffmepg -version".convertToCommand()
                withContext(Dispatchers.IO){
                    FFmpegKit.runCommand(command).collect{
                        when(it){
                            RunState.OnCancel -> {
                                Log.e("FFmpegKit","OnCancel")
                            }
                            is RunState.OnError -> {
                                Log.e("FFmpegKit",it.message.toString())
                            }
                            RunState.OnFinish -> {
                                Log.e("FFmpegKit","OnFinish")
                            }
                            is RunState.OnProgress -> {
                                Log.e("FFmpegKit","OnProgress")
                            }
                            RunState.OnStart ->{
                                Log.e("FFmpegKit","OnStart")
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {
        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        CurlHttp.cleanUp()
        STKit.onDestroy()
    }
}