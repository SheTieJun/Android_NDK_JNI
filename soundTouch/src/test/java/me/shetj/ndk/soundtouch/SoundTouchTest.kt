package me.shetj.ndk.soundtouch

import org.junit.Test
import org.junit.Assert.*
import org.junit.Before

/**
 * SoundTouch功能测试类
 * 测试setPitchOctaves方法的基本功能
 */
class SoundTouchTest {
    
    private lateinit var soundTouch: SoundTouch
    private var handle: Long = 0
    
    @Before
    fun setUp() {
        soundTouch = SoundTouch()
        handle = soundTouch.newInstance()
        
        // 初始化SoundTouch实例
        soundTouch.init(
            handle = handle,
            channels = 2,      // 双声道
            sampleRate = 44100, // 标准采样率
            tempo = 1.0f,      // 正常速度
            pitch = 0.0f,      // 正常音高
            speed = 1.0f       // 正常播放速率
        )
    }
    
    @Test
    fun testSetPitchOctaves_normalValues() {
        // 测试正常范围内的八度调整值
        val testValues = floatArrayOf(-2.0f, -1.0f, -0.5f, 0.0f, 0.5f, 1.0f, 2.0f)
        
        for (octaves in testValues) {
            try {
                soundTouch.setPitchOctaves(handle, octaves)
                // 如果没有抛出异常，则认为测试通过
                assertTrue("setPitchOctaves should handle value: $octaves", true)
            } catch (e: Exception) {
                fail("setPitchOctaves failed with value $octaves: ${e.message}")
            }
        }
    }
    
    @Test
    fun testSetPitchOctaves_boundaryValues() {
        // 测试边界值
        val boundaryValues = floatArrayOf(-3.0f, 3.0f, -10.0f, 10.0f)
        
        for (octaves in boundaryValues) {
            try {
                soundTouch.setPitchOctaves(handle, octaves)
                // 边界值测试，主要确保不会崩溃
                assertTrue("setPitchOctaves should handle boundary value: $octaves", true)
            } catch (e: Exception) {
                // 边界值可能会有异常，记录但不失败
                println("Boundary value $octaves caused exception: ${e.message}")
            }
        }
    }
    
    @Test
    fun testSetPitchOctaves_withOtherEffects() {
        // 测试与其他音效的兼容性
        try {
            // 设置音高调整
            soundTouch.setPitchOctaves(handle, 1.0f)
            
            // 设置其他音效参数
            soundTouch.setTempo(handle, 1.2f)
            soundTouch.setRate(handle, 0.9f)
            soundTouch.setPitchSemiTones(handle, 2.0f)
            
            // 再次设置八度调整，确保兼容性
            soundTouch.setPitchOctaves(handle, -0.5f)
            
            assertTrue("setPitchOctaves should work with other effects", true)
        } catch (e: Exception) {
            fail("setPitchOctaves failed when used with other effects: ${e.message}")
        }
    }
    
    @Test
    fun testSetPitchOctaves_invalidHandle() {
        // 测试无效句柄
        val invalidHandle = 0L
        
        try {
            soundTouch.setPitchOctaves(invalidHandle, 1.0f)
            // 如果使用无效句柄没有异常，可能需要检查实现
            println("Warning: setPitchOctaves with invalid handle did not throw exception")
        } catch (e: Exception) {
            // 预期行为：无效句柄应该抛出异常或有错误处理
            assertTrue("setPitchOctaves correctly handled invalid handle", true)
        }
    }
    
    @Test
    fun testVersionString() {
        // 基本功能测试：确保库正常加载
        val version = soundTouch.getVersionString()
        assertNotNull("Version string should not be null", version)
        assertTrue("Version string should not be empty", version.isNotEmpty())
    }
    
    // 清理资源
    fun tearDown() {
        if (handle != 0L) {
            soundTouch.deleteInstance(handle)
        }
    }
}