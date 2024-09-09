package com.example.liststart

import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class GisActivity : AppCompatActivity(), OnMapReadyCallback {

    private var googleMap: GoogleMap? = null
    private var polygonList: MutableList<Polygon> = mutableListOf()
    private var isRestrictedAreaVisible = false // 규제구역 표시 여부
    private var lat: Double = 0.0
    private var long: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gis)

        // Intent로 전달된 사업 좌표를 받습니다.
        val bundle = intent.extras
        if (bundle != null) {
            lat = bundle.getDouble("lat", 0.0) // 위도값 받기, 기본값 0.0
            long = bundle.getDouble("long", 0.0) // 경도값 받기, 기본값 0.0
        }

        // 지도 프래그먼트 초기화
        (supportFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment).getMapAsync(this)

        // 규제구역 버튼 처리
        val controlLineButton = findViewById<ImageButton>(R.id.controllLine)
        controlLineButton.setOnClickListener {
            if (isRestrictedAreaVisible) {
                hideRestrictedAreas() // 규제구역 숨기기
            } else {
                loadDevelopmentRestrictedAreas() // 규제구역 표시
            }
        }
    }

    override fun onMapReady(map: GoogleMap?) {
        googleMap = map

        // 지도 이동: 전달된 사업 좌표로 이동
        if (lat != 0.0 && long != 0.0) {
            val location = LatLng(lat, long)
            val zoomLevel = 15f // 원하는 줌 레벨 설정
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(location, zoomLevel))

            // 마커 추가
            googleMap?.addMarker(com.google.android.gms.maps.model.MarkerOptions().position(location).title("사업 위치"))
        }
    }

    // 규제구역 API 호출 및 데이터 표시
    private fun loadDevelopmentRestrictedAreas() {
        val apiKey = "05C26CB0-9905-39AC-8E59-423EE652CA06" // API 키 입력
        val url = "https://api.vworld.kr/req/data?service=data&request=GetFeature&data=LT_C_UD801&key=$apiKey&geomFilter=BOX($long,$lat,${long + 1},${lat + 1})&format=json&size=100"

        // 비동기 네트워크 요청
        FetchDevelopmentRestrictedAreaTask().execute(url)
    }

    // API에서 개발제한구역 데이터를 가져오는 AsyncTask
    inner class FetchDevelopmentRestrictedAreaTask : AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg params: String?): String? {
            val urlString = params[0]
            var result: String? = null
            try {
                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val sb = StringBuilder()
                var line: String?

                while (reader.readLine().also { line = it } != null) {
                    sb.append(line)
                }
                reader.close()

                result = sb.toString()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return result
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            // 응답 로그 출력
            Log.d("API Response", result ?: "No response")

            result?.let {
                // 받은 GeoJSON 데이터를 처리
                parseAndDisplayRestrictedAreas(it)
            } ?: run {
                Toast.makeText(this@GisActivity, "개발제한구역 데이터를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // GeoJSON 데이터를 파싱하여 지도에 표시
    private fun parseAndDisplayRestrictedAreas(jsonData: String) {
        try {
            val jsonObject = JSONObject(jsonData)
            val response = jsonObject.getJSONObject("response")

            // status 확인
            val status = response.getString("status")
            if (status != "OK") {
                Toast.makeText(this, "API 요청에 실패했습니다: $status", Toast.LENGTH_SHORT).show()
                return
            }

            // result 필드가 존재하는지 확인
            if (!response.has("result")) {
                Toast.makeText(this, "결과 데이터가 없습니다.", Toast.LENGTH_SHORT).show()
                return
            }

            // featureCollection 안의 features 배열을 가져옴
            val featureCollection = response.getJSONObject("result").getJSONObject("featureCollection")
            val features = featureCollection.getJSONArray("features")

            // 개발제한구역 데이터를 파싱하여 지도에 Polygon으로 표시
            for (i in 0 until features.length()) {
                val feature = features.getJSONObject(i)
                val geometry = feature.getJSONObject("geometry")
                val coordinates = geometry.getJSONArray("coordinates")

                // POLYGON 좌표 배열 파싱
                val polygonOptions = PolygonOptions()
                val outerBoundary = coordinates.getJSONArray(0).getJSONArray(0)

                for (j in 0 until outerBoundary.length()) {
                    val point = outerBoundary.getJSONArray(j)
                    val lon = point.getDouble(0) // 0번째 인덱스는 경도
                    val lat = point.getDouble(1) // 1번째 인덱스는 위도
                    polygonOptions.add(LatLng(lat, lon)) // LatLng에 경도와 위도를 맞게 넣어줌
                }

                // 개발제한구역을 색상으로 표시
                polygonOptions.fillColor(0x55FF0000)  // 반투명 빨간색
                polygonOptions.strokeColor(0xFFFF0000.toInt())  // 빨간색 테두리
                polygonOptions.strokeWidth(3f)  // 선 굵기

                val polygon = googleMap?.addPolygon(polygonOptions)
                polygon?.let {
                    polygonList.add(it) // 생성한 폴리곤을 리스트에 추가
                }
            }

            isRestrictedAreaVisible = true // 규제구역 표시 상태로 설정

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "개발제한구역 데이터를 처리하는데 실패했습니다.", Toast.LENGTH_SHORT).show()
        }
    }
    // 규제구역을 숨기는 함수
    private fun hideRestrictedAreas() {
        polygonList.forEach { it.remove() } // 지도에서 폴리곤 제거
        polygonList.clear() // 리스트 초기화
        isRestrictedAreaVisible = false // 규제구역 숨김 상태로 설정
    }
}
