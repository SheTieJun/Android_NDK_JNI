package me.shetj.sdk.ffmepg.demo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import me.shetj.recorder.core.SimRecordListener
import me.shetj.sdk.ffmepg.demo.databinding.ActivityWebRtcNsBinding

class WebRtcNSActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWebRtcNsBinding
    private lateinit var recordUtils: RecordUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebRtcNsBinding.inflate(layoutInflater)
        recordUtils = RecordUtils(this,object : SimRecordListener(){

            override fun onStart() {
                super.onStart()
                binding.msg.text = "正在录音"
            }

            override fun onSuccess(isAutoComplete: Boolean, file: String, time: Long) {
                super.onSuccess(isAutoComplete, file, time)
                binding.msg.text = "录音完成:$file"
            }
        })
        setContentView(binding.root)

        WebRtcNsKit.create()

        binding.start.setOnClickListener {
            recordUtils.startOrPause()
        }

        binding.stop.setOnClickListener {
            recordUtils.complete()
        }

        binding.switchNs.setOnCheckedChangeListener { compoundButton, b ->
            recordUtils.setWebRtcNS(b)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        recordUtils.complete()
        WebRtcNsKit.free()
    }

    companion object {
        fun start(mainActivity: MainActivity) {
            mainActivity.startActivity(Intent(mainActivity, WebRtcNSActivity::class.java))
        }
    }
}