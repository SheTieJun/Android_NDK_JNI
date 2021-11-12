package me.shetj.sdk.ffmepg.demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
            binding.sampleText.text = stringFromJNI()
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