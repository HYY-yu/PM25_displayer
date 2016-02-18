package com.app.feng.PM25_displayer.Activity;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;


public class CityLocationActivity extends Activity implements AMapLocationListener {

    LocationManagerProxy mLocationManagerProxy;
    Intent intent = new Intent();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toast.makeText(this, "正在定位...", Toast.LENGTH_SHORT).show();
        mLocationManagerProxy = LocationManagerProxy.getInstance(this);

        mLocationManagerProxy.requestLocationData(LocationProviderProxy.AMapNetwork, -1, 15, this);
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    public void onDestory() {
        super.onDestroy();
        mLocationManagerProxy.destory();
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (amapLocation != null) {

            if (amapLocation.getAMapException().getErrorCode() == 0) {
                intent.putExtra("city", amapLocation.getCity());
                Log.i("aaa", amapLocation.toString());

            } else {
                intent.putExtra("error", amapLocation.getAMapException().getErrorMessage());
                Log.e("AmapErr", "Location ERR:" + amapLocation.getAMapException().getErrorMessage());
            }

        }
        setResult(100, intent);
        this.finish();
    }
}
