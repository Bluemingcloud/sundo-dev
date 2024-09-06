package com.example.liststart

import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.ImageButton
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
    private var currentCenter: LatLng? = null // 현재 지도 중앙의 위치를 저장

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

        (supportFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment).getMapAsync(this)

        providerClient = LocationServices.getFusedLocationProviderClient(this)
        apiClient = GoogleApiClient.Builder(this)
            .addApi(LocationServices.API)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .build()

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
            )
        } else {
            apiClient.connect()
        }

        // 마커 추가 버튼 설정
        val addMarkerButton = findViewById<ImageButton>(R.id.rightButton) // 마커 추가 버튼에 적절한 ID 사용
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

        // 지도 중앙에 고정된 마커를 위한 이미지 설정
        googleMap?.setOnCameraIdleListener {
            // 지도 중심의 위치 업데이트
            currentCenter = googleMap?.cameraPosition?.target
        }

        // 지도에서 이동 시 현재 위치를 따라가는 리스너
        googleMap?.setOnCameraMoveListener {
            // 필요한 경우, 현재 지도의 중심 좌표를 표시할 수 있습니다.
        }
    }
}
