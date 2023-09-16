package com.example.immersivestatusbartest

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette

/**
 * 参考郭霖博客:
 *      https://guolin.blog.csdn.net/article/details/125234545?spm=1001.2014.3001.5502
 */
class MainActivity : AppCompatActivity() {

    private lateinit var bgImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bgImageView = findViewById(R.id.bg_image)
        val darkImageBtn = findViewById<Button>(R.id.dark_image_btn)
        val lightImageBtn = findViewById<Button>(R.id.light_image_btn)
        val splitImageBtn = findViewById<Button>(R.id.split_image_btn)

        darkImageBtn.setOnClickListener {
            setBgImageByResource(R.drawable.dark_image)
        }
        lightImageBtn.setOnClickListener {
            setBgImageByResource(R.drawable.light_image)
        }
        splitImageBtn.setOnClickListener {
            setBgImageByResource(R.drawable.split_image)
        }

        setBgImageByResource(R.drawable.dark_image)
    }

    private fun setBgImageByResource(imageResource: Int) {
        val bitmap = BitmapFactory.decodeResource(resources, imageResource)
        bgImageView.setImageBitmap(bitmap)
        detectBitmapColor(bitmap)
    }

    private fun detectBitmapColor(bitmap: Bitmap) {
        val colorCount = 5
        val left = 0
        val top = 0
        val right = getScreenWidth()
        val bottom = getStatusBarHeight()

        Palette
            .from(bitmap)
            .maximumColorCount(colorCount) // 几个特征颜色值
            .setRegion(left, top, right, bottom) // 分析的区域
            .generate {
                it?.let { palette ->
                    var mostPopularSwatch: Palette.Swatch? = null // 权重最大的那个颜色特征值
                    for (swatch in palette.swatches) {
                        if (mostPopularSwatch == null
                            || swatch.population > mostPopularSwatch.population
                        ) {
                            mostPopularSwatch = swatch
                        }
                    }
                    mostPopularSwatch?.let { swatch ->
                        val luminance = ColorUtils.calculateLuminance(swatch.rgb)
                        Log.i("TAG", "luminance: $luminance")
                        // If the luminance value is lower than 0.5, we consider it as dark.
                        if (luminance < 0.5) {
                            setDarkStatusBar() // 系统就两种状态栏效果可选,深色状态栏,对应的就是状态栏的图标是浅色的
                        } else {
                            setLightStatusBar()
                        }
                    }
                }
            }
    }

    private fun setLightStatusBar() {
        val flags = window.decorView.systemUiVisibility
        // 不管原来是什么模式，位运算或操作都是该标识位置1，对应的就是成为浅色状态栏，状态栏图标为深色
        window.decorView.systemUiVisibility = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }

    private fun setDarkStatusBar() { // 采用位操作真实精妙！
        // 1. or操作: 补充浅色状态栏
        val flags = window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        // 2. xor操作: 剔除浅色状态栏(也就是成为另一种,深色状态栏),异或操作相同为0,不同为1
        window.decorView.systemUiVisibility = flags xor View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }

    private fun getScreenWidth(): Int {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.widthPixels
    }

    private fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }
}