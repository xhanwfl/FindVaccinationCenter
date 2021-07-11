package com.example.findvaccinationcenter

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.findvaccinationcenter.model.CenterDTO
import com.example.findvaccinationcenter.network.CenterAPI
import com.example.findvaccinationcenter.network.RetrofitSender
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_maps.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

internal class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    var longitude : Double? = null
    var latlng : LatLng? = null
    var latitude : Double? = null

    var myLocationMarker : MarkerOptions? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val lm = getSystemService(Context.LOCATION_SERVICE)as LocationManager

        //버튼이벤트
        getLocationButton?.setOnClickListener {
            getCurrentLocatioon(lm)
        }

        lm.removeUpdates(gpsLocationListener)




        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }
    fun getCenterAPI(){

        val apiKey = "QxZITTH8cRe+Vebo7nyrUCbYz/HYCHHXJqoP6tIQqEuxoPMpUGCzPTgJ0DqGpFpWjujBUZUBzqbg39aFuKi4xQ=="

        val api = RetrofitSender.getInstance().create(CenterAPI::class.java)
        val callCenterAPI = api.getCenterData(1,999,apiKey)

        callCenterAPI.enqueue(object : Callback<CenterDTO> {
            override fun onResponse(call: Call<CenterDTO>, response: Response<CenterDTO>) {
                Log.d("=>", "성공 : ${response.raw()}")

                var jsonArr = response.body()?.data
                Log.d("=>",jsonArr!![0].address)

                for(i in 0.. jsonArr.size-1){
                    var lat = jsonArr[i].lat.toDouble()
                    var lng = jsonArr[i].lng.toDouble()
                    var latlng = LatLng(lat,lng)
                    mMap.addMarker(
                        MarkerOptions()
                            .position(latlng!!)
                            .title(jsonArr[i].centerName)
                    )
                }


            }

            override fun onFailure(call: Call<CenterDTO>, t: Throwable) {
                Log.d(ContentValues.TAG, "실패 : $t")
            }
        })
    }


    fun getCurrentLocatioon(lm : LocationManager){

        val isGPSEnabled : Boolean = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        //매니페스트에 권한추가후 여기서 다시 한번 확인해야함
        if(Build.VERSION.SDK_INT >= 23 &&
            ContextCompat.checkSelfPermission(applicationContext,android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this@MapsActivity, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),0)
        }else{
            when{ //프로바이더 제공자 활성화 여부 체크
                isGPSEnabled -> {
                    val location =
                        lm.getLastKnownLocation(LocationManager.GPS_PROVIDER) // GPS 기반으로 위치를 찾음
                    longitude = location?.longitude!!
                    latitude = location?.latitude!!
                    Log.d("=>","GPS현재위치를 가져옵니다. longitude : ${longitude}, latitude : ${latitude}")

                    latlng = LatLng(latitude!!, longitude!!)

//                    mMap.addMarker(
//                        MarkerOptions()
//                            .position(latlng!!)
//                            .title("Current location")
//                    )

                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng))
                }
                else -> {
                    Log.d("=>","ㅅㅂ")
                }
            }
            //몇초 간격과 몇미터를 이동했을시에 호출되는 부분 주기적으로 위치를 업데이트 하고 싶을 경우 사용
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                1000,//몇초
                1F,//몇미터
                gpsLocationListener)
            /*해제부분
            Im.removeUpdates(gpsLocationListener)*/
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera

            latlng = LatLng(37.0, 127.0)

        if(latitude!=null&&longitude!=null){
            latlng = LatLng(latitude!!, longitude!!)
        }
        mMap.addMarker(
            MarkerOptions()
                .position(latlng!!)
                .title("basic location")
        )
       // val myLocationMarker = MarkerOptions().position(latlng!!).title("basic location")


        mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng))

        getCenterAPI()
        myLocationMarker = MarkerOptions().position(latlng!!)
        mMap.addMarker(myLocationMarker)
    }

    /**
     * Model level 1 : 순수 데이터 ( 좌표 )
     * ViewModel Level 2 : 순수 데이터를 뷰에 적용하기위해 가공한 데이터
     * View 는 Viewmodel 을 바라본다.
     * */
    val gpsLocationListener = object : LocationListener{
        override fun onLocationChanged(location: Location?) {
            val provider : String = location!!.provider
            val longitude : Double = location!!.longitude
            val latitude : Double = location!!.latitude
            val altitude : Double = location!!.altitude

        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

        override fun onProviderEnabled(provider: String?) {}

        override fun onProviderDisabled(provider: String?) {}
    }
}
