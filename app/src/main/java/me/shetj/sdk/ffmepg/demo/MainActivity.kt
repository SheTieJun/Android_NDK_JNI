package me.shetj.sdk.ffmepg.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import me.shetj.ndk.lame.LameUtils
import me.shetj.sdk.ffmepg.demo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Example of a call to a native method
        try {
            STKit.getInstance().init(1,44100,1f,10f,1f)
            LameUtils.init(44100,1,44100,44,3)
            binding.sampleText.text = stringFromJNI() +
                    "\nLame:"+LameUtils.version() +
                    "\nSoundTouch:${ STKit.getInstance().getVersion()}"
        }catch (e:Exception){
            e.printStackTrace()
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
}