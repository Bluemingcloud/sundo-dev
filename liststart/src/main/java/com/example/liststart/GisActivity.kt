package com.example.liststart

import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
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
    private lateinit var centerMarkerPreview: ImageView // 중앙에 고정된 미리보기 마커
    private var isMarkerPreviewVisible = false // 미리보기 마커의 상태를 추적

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gis)

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

        // 권한 요청 처리
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
            )
        } else {
            apiClient.connect()
        }

        // 중앙에 고정된 미리보기 마커 설정
        centerMarkerPreview = findViewById(R.id.centerMarkerPreview)
        centerMarkerPreview.visibility = View.GONE // 초기에는 숨겨진 상태로 시작

        // selctloti 레이아웃 클릭 이벤트 처리
        val selctlotiLayout = findViewById<LinearLayout>(R.id.selctloti)
        selctlotiLayout.setOnClickListener {
            // 미리보기 마커의 가시성을 토글
            isMarkerPreviewVisible = !isMarkerPreviewVisible
            centerMarkerPreview.visibility = if (isMarkerPreviewVisible) View.VISIBLE else View.GONE
        }

        // 마커 추가 버튼 설정
        val addMarkerButton = findViewById<ImageButton>(R.id.rightButton)
        addMarkerButton.setOnClickListener {
            currentCenter?.let { center ->
                addMarkerAtLocation(center.latitude, center.longitude, "선택한 위치")
                Toast.makeText(this, "마커가 추가되었습니다: ${center.latitude}, ${center.longitude}", Toast.LENGTH_SHORT).show()
            }
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

    private fun addMarkerAtLocation(latitude: Double, longitude: Double, title: String) {
        val latLng = LatLng(latitude, longitude)
        val markerOption = MarkerOptions()
            .position(latLng)
            .title(title)
        googleMap?.addMarker(markerOption)
    }

    override fun onConnected(p0: Bundle?) {
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
            apiClient.disconnect()
        }
    }

    override fun onConnectionSuspended(p0: Int) {
        // 연결이 중단될 때 호출되는 콜백
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        // 연결 실패 시 호출되는 콜백
    }

    override fun onMapReady(map: GoogleMap?) {
        googleMap = map

        // 지도 중심의 위치를 업데이트하는 리스너
        googleMap?.setOnCameraIdleListener {
            currentCenter = googleMap?.cameraPosition?.target
        }
    }
}
