package com.example.liststart

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.liststart.view.MainActivity

class SplashActivity : AppCompatActivity() {

    private val CAMERA_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val splashImage = findViewById<ImageView>(R.id.splash_image)

        // Glide를 사용해서 GIF 로드하기
        Glide.with(this)
            .asGif()
            .load(R.drawable.splash_background) // GIF 파일의 리소스 ID
            .into(splashImage)

        // 권한 확인 및 요청
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            // 권한이 이미 허용되었을 경우 2초 후에 다음 화면으로 이동
            proceedToNextScreenWithDelay()
        } else {
            // 권한 요청
            requestCameraPermission()
        }
    }

    // 카메라 권한 요청
    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
    }

    // 권한 요청 결과 처리
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한이 허용된 경우 2초 후 다음 화면으로 이동
                proceedToNextScreenWithDelay()
            } else {
                // 권한이 거부된 경우 앱 종료
                Toast.makeText(this, "카메라 권한이 필요합니다. 앱을 종료합니다.", Toast.LENGTH_LONG).show()
                finish() // 앱 종료
            }
        }
    }

    // 2초 후에 다음 화면으로 이동
    private fun proceedToNextScreenWithDelay() {
        Handler(Looper.getMainLooper()).postDelayed({
            proceedToNextScreen()
        }, 2000) // 2초 대기
    }

    // 다음 화면으로 이동하는 함수
    private fun proceedToNextScreen() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("unity", "some_value")  // 여기에 문자열 값을 명시적으로 전달
        startActivity(intent)
        finish() // SplashActivity 종료
    }
}