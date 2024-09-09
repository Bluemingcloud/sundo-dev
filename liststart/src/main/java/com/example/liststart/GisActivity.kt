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
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnSuccessListener

class GisActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback {

    lateinit var providerClient: FusedLocationProviderClient
    lateinit var apiClient: GoogleApiClient
    private var googleMap: GoogleMap? = null
    private var currentCenter: LatLng? = null
    private lateinit var centerMarkerPreview: ImageView // 화면 가운데 미리보기 마커
    private lateinit var selectLocationTextView: TextView // 위치 선택 텍스트뷰
    private var isMarkerPreviewVisible = false // 미리보기 마커 상태 추적
    private lateinit var centerEditText: EditText // 제목 수정용 EditText
    private lateinit var rightButton: ImageButton // 수정 적용 버튼
    private lateinit var leftButton: ImageButton // GPS 위치로 이동 버튼

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

        // 마커 추가 버튼 설정
        val addMarkerButton = findViewById<TextView>(R.id.selectLocationTextView)
        addMarkerButton.setOnClickListener {
            currentCenter?.let { center ->
                addMarkerAtLocation(center.latitude, center.longitude, "선택한 위치")
                Toast.makeText(this, "마커가 추가되었습니다: ${center.latitude}, ${center.longitude}", Toast.LENGTH_SHORT).show()
            }
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

    private fun moveMap(latitude: Double, longitude: Double) {
        val latLng = LatLng(latitude, longitude)
        val position: CameraPosition = CameraPosition.Builder()
            .target(latLng)
            .zoom(16f)
            .build()
        googleMap!!.moveCamera(CameraUpdateFactory.newCameraPosition(position))
    }

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

    override fun onMapReady(map: GoogleMap?) {
        googleMap = map

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
    }
}
