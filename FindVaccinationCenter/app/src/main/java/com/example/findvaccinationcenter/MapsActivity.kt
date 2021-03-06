package com.example.findvaccinationcenter

import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.findvaccinationcenter.model.CenterDTO
import com.example.findvaccinationcenter.network.CenterAPI
import com.example.findvaccinationcenter.network.RetrofitSender
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_maps.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

internal class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    var longitude : Double? = null
    var latlng : LatLng? = null
    var latitude : Double? = null
    var lm : LocationManager? = null
    var myLocationMarker : MarkerOptions? = null
    var myMarker : Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)


        //???????????????
        getLocationButton?.setOnClickListener {
            getCurrentLocatioon(lm!!)
        }

        //lm?.removeUpdates(gpsListener)




        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    fun getCenterAPI(){

        val apiKey = "QxZITTH8cRe+Vebo7nyrUCbYz/HYCHHXJqoP6tIQqEuxoPMpUGCzPTgJ0DqGpFpWjujBUZUBzqbg39aFuKi4xQ=="

        val api = RetrofitSender.getInstance().create(CenterAPI::class.java)
        val callCenterAPI = api.getCenterData(1, 999, apiKey)

        callCenterAPI.enqueue(object : Callback<CenterDTO> {
            override fun onResponse(call: Call<CenterDTO>, response: Response<CenterDTO>) {
                Log.d("=>", "?????? : ${response.raw()}")

                var jsonArr = response.body()?.data
                Log.d("=>", jsonArr!![0].address)

                for (i in 0..jsonArr.size - 1) {
                    var lat = jsonArr[i].lat.toDouble()
                    var lng = jsonArr[i].lng.toDouble()
                    var latlng = LatLng(lat, lng)
                    mMap.addMarker(
                        MarkerOptions()
                            .position(latlng!!)
                            .title(jsonArr[i].centerName)
                            .snippet(
                                "?????? : " + jsonArr[i].address +
                                        "\n????????? : " + jsonArr[i].phoneNumber
                            )
                    )

                }


            }

            override fun onFailure(call: Call<CenterDTO>, t: Throwable) {
                Log.d(ContentValues.TAG, "?????? : $t")
            }
        })
    }

    fun setCustomSnippet(){
        mMap.setInfoWindowAdapter(object : InfoWindowAdapter {
            override fun getInfoWindow(marker: Marker): View? {

                return null
            }

            override fun getInfoContents(marker: Marker): View {

                val info = LinearLayout(applicationContext)
                info.setOrientation(LinearLayout.VERTICAL)
                val title = TextView(applicationContext)
                title.setTextColor(Color.BLACK)
                title.setGravity(Gravity.CENTER)
                title.setTypeface(null, Typeface.BOLD)
                title.setText(marker.title)
                val snippet = TextView(applicationContext)
                snippet.setTextColor(Color.GRAY)
                snippet.setGravity(Gravity.LEFT)
                snippet.setText(marker.snippet)
                info.addView(title)
                info.addView(snippet)
                return info
            }
        })
    }

    fun getCurrentLocatioon(lm: LocationManager){

        val isGPSEnabled : Boolean = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        //?????????????????? ??????????????? ????????? ?????? ?????? ???????????????
        if(Build.VERSION.SDK_INT >= 23 &&
            ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(
                this@MapsActivity,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                0
            )
        }else{
            when{ //??????????????? ????????? ????????? ?????? ??????
                isGPSEnabled -> {
                    val location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER) // GPS ???????????? ????????? ??????
                    if(location!=null){
                        longitude = location?.longitude!!
                        latitude = location?.latitude!!
                        Log.d("=>", "GPS??????????????? ???????????????. longitude : ${longitude}, latitude : ${latitude}")

                        latlng = LatLng(longitude!!, latitude!!)

                        showCurrentLocation(latitude!!, longitude!!)
                    }else{
                        Toast.makeText(applicationContext,"???????????????",Toast.LENGTH_LONG).show()
                    }

                }
                else -> {
                    Log.d("=>", "gps ?????? ??????")
                }
            }
            //?????? ????????? ???????????? ?????????????????? ???????????? ?????? ??????????????? ????????? ???????????? ?????? ?????? ?????? ??????
            lm.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000,//??????
                1F,//?????????
                gpsListener
            )
            /*????????????
            Im.removeUpdates(gpsListener)*/
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
       // val myLocationMarker = MarkerOptions().position(latlng!!).title("basic location")

        try{
            lm = getSystemService(Context.LOCATION_SERVICE)as LocationManager

        }finally{
            getCurrentLocatioon(lm!!)
        }

        latlng.let{
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng))
            myLocationMarker = MarkerOptions().position(latlng!!)
            mMap.addMarker(myLocationMarker)
        }

        //?????????????????? ???????????????
        getCenterAPI()

        setCustomSnippet()
    }

    fun showCurrentLocation(latitude: Double, longitude: Double){
        var curPoint = LatLng(latitude, longitude)
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(curPoint, 15F))
        showMyLocationManager(curPoint)
    }

    fun showMyLocationManager(curPoint: LatLng){
        if(myLocationMarker == null){
            myLocationMarker = MarkerOptions()// ?????? ?????? ??????
            myLocationMarker?.position(curPoint)
            myLocationMarker?.title("???????????? \n")
            myLocationMarker?.snippet("*GPS??? ????????? ????????????")
            myLocationMarker?.icon(BitmapDescriptorFactory.fromResource((R.drawable.mylocation)))
            myMarker = mMap.addMarker(myLocationMarker)
        } else {
            var bitmapDrawable : BitmapDrawable = resources.getDrawable(R.drawable.mylocation) as BitmapDrawable
            var smallMarker = Bitmap.createScaledBitmap(bitmapDrawable.bitmap,200,200,false)

            myMarker?.remove(); // ????????????
            myLocationMarker?.position(curPoint)
            myLocationMarker?.title("???????????? \n")
            myLocationMarker?.icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
            myMarker = mMap.addMarker(myLocationMarker)

        }
    }


    /**
     * Model level 1 : ?????? ????????? ( ?????? )
     * ViewModel Level 2 : ?????? ???????????? ?????? ?????????????????? ????????? ?????????
     * View ??? Viewmodel ??? ????????????.
     * */
    val gpsListener = object : LocationListener{
        override fun onLocationChanged(location: Location?) {
            val provider : String = location!!.provider
            val longitude : Double = location!!.longitude
            val latitude : Double = location!!.latitude
            val altitude : Double = location!!.altitude

            showCurrentLocation(latitude, longitude)
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

        override fun onProviderEnabled(provider: String?) {}

        override fun onProviderDisabled(provider: String?) {}
    }
}
