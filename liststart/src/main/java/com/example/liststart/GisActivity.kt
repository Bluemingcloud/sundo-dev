package com.example.liststart


import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

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

import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition

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

    private var currentCenter: LatLng? = null
    private lateinit var centerMarkerPreview: ImageView // 화면 가운데 미리보기 마커
    private lateinit var selectLocationTextView: TextView // 위치 선택 텍스트뷰
    private var isMarkerPreviewVisible = false // 미리보기 마커 상태 추적
    private lateinit var centerEditText: EditText // 제목 수정용 EditText
    private lateinit var rightButton: ImageButton // 수정 적용 버튼
    private lateinit var leftButton: ImageButton // GPS 위치로 이동 버튼

    private var polygonList: MutableList<Polygon> = mutableListOf()
    private var isRestrictedAreaVisible = false // 규제구역 표시 여부
    private var lat: Double = 0.0
    private var long: Double = 0.0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gis)


        // 인텐트로 전달된 제목 데이터 받기
        val title = intent.getStringExtra("title") ?: "이름 없음"

        // UI 요소 초기화
        centerEditText = findViewById(R.id.centerEditText)
        centerEditText.setText(title)

        // 권한 요청 설정
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            if (it.all { permission -> permission.value == true }) {
                apiClient.connect()
            } else {
                Toast.makeText(this, "권한 거부", Toast.LENGTH_SHORT).show()
            }

        // Intent로 전달된 사업 좌표를 받습니다.
        val bundle = intent.extras
        if (bundle != null) {
            lat = bundle.getDouble("lat", 0.0) // 위도값 받기, 기본값 0.0
            long = bundle.getDouble("long", 0.0) // 경도값 받기, 기본값 0.0

        }

        // 지도 프래그먼트 초기화
        (supportFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment).getMapAsync(this)

        providerClient = LocationServices.getFusedLocationProviderClient(this)
        apiClient = GoogleApiClient.Builder(this)
            .addApi(LocationServices.API)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .build()

        // 위치 권한 요청
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
            )
        } else {
            apiClient.connect()

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


        // 중앙 미리보기 마커와 위치 선택 텍스트뷰 설정
        centerMarkerPreview = findViewById(R.id.centerMarkerPreview)
        selectLocationTextView = findViewById(R.id.selectLocationTextView)
        centerMarkerPreview.visibility = View.GONE // 초기에는 숨김
        selectLocationTextView.visibility = View.GONE // 초기에는 숨김

        // 좌표 선택 버튼 클릭 이벤트 설정
        val selctlotiLayout = findViewById<LinearLayout>(R.id.selctloti)
        selctlotiLayout.setOnClickListener {
            isMarkerPreviewVisible = !isMarkerPreviewVisible
            centerMarkerPreview.visibility = if (isMarkerPreviewVisible) View.VISIBLE else View.GONE
            selectLocationTextView.visibility = if (isMarkerPreviewVisible) View.VISIBLE else View.GONE
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

        // 수정 버튼 클릭 이벤트 설정
        rightButton = findViewById(R.id.rightButton)
        rightButton.setOnClickListener {
            val newTitle = centerEditText.text.toString()
            Toast.makeText(this, "수정된 제목: $newTitle", Toast.LENGTH_SHORT).show()
        }

        // GPS 위치로 이동 버튼 설정
        leftButton = findViewById(R.id.leftButton)
        leftButton.setOnClickListener {
            moveToCurrentLocation()
        }
    }

    // 규제구역 API 호출 및 데이터 표시
    private fun loadDevelopmentRestrictedAreas() {
        val apiKey = "05C26CB0-9905-39AC-8E59-423EE652CA06" // API 키 입력
        val url = "https://api.vworld.kr/req/data?service=data&request=GetFeature&data=LT_C_UD801&key=$apiKey&geomFilter=BOX($long,$lat,${long + 1},${lat + 1})&format=json&size=100"


    private fun addMarkerAtLocation(latitude: Double, longitude: Double, title: String, markerColor: Float = BitmapDescriptorFactory.HUE_RED) {
        val latLng = LatLng(latitude, longitude)
        val markerOption = MarkerOptions()
            .position(latLng)
            .title(title)
            .icon(BitmapDescriptorFactory.defaultMarker(markerColor)) // 마커 색상 설정
        googleMap?.addMarker(markerOption)
    }

    private fun moveToCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            providerClient.lastLocation
                .addOnSuccessListener(this, OnSuccessListener<Location> { location ->
                    if (location != null) {
                        val latitude = location.latitude
                        val longitude = location.longitude
                        moveMap(latitude, longitude)
                        addMarkerAtLocation(latitude, longitude, "현재 위치", BitmapDescriptorFactory.HUE_BLUE) // 현재 위치에 파란색 마커 추가
                    } else {
                        Toast.makeText(this, "현재 위치를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                })
        } else {
            Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onConnected(p0: Bundle?) {
        val lat = intent.getDoubleExtra("lat", 0.0)
        val long = intent.getDoubleExtra("long", 0.0)

        if (lat == 0.0 && long == 0.0) {
            // 현재 위치 사용
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
                providerClient.lastLocation
                    .addOnSuccessListener(
                        this@GisActivity,
                        OnSuccessListener<Location> { location ->
                            location?.let {
                                val latitude = it.latitude
                                val longitude = it.longitude
                                moveMap(latitude, longitude)
                            }
                        }
                    )
            }
        } else {
            // 인텐트로 전달받은 좌표로 이동 및 파란색 마커 추가
            moveMap(lat, long)
            addMarkerAtLocation(lat, long, "초기 위치", BitmapDescriptorFactory.HUE_AZURE) // 파란색 마커 추가
        }

        apiClient.disconnect()
    }

    override fun onConnectionSuspended(p0: Int) {
        // 연결이 중단될 때 호출
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        // 연결 실패 시 호출
    }

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


        // 지도 중심 위치 업데이트 리스너
        googleMap?.setOnCameraIdleListener {
            currentCenter = googleMap?.cameraPosition?.target
        }
    }

    // 화면의 다른 부분을 클릭했을 때 EditText 포커스 해제
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val view = currentFocus
            if (view is EditText) {
                val outRect = android.graphics.Rect()
                view.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    view.clearFocus()
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)

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
