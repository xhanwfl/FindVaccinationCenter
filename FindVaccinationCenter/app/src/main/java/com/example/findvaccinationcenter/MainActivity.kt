package com.example.findvaccinationcenter

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.LatLng

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var handler = Handler()
        handler.postDelayed(Runnable {
            checkPermission()
        },2000)
    }

    fun checkPermission(){
        var lm = getSystemService(Context.LOCATION_SERVICE)as LocationManager
        val isGPSEnabled : Boolean = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        //매니페스트에 권한추가후 여기서 다시 한번 확인해야함
        if(Build.VERSION.SDK_INT >= 23 &&
            ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                0
            )
        }else{
            when{
                isGPSEnabled ->{
                    var intent = Intent(this,MapsActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

}
