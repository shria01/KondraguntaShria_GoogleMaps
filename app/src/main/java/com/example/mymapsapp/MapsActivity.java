package com.example.mymapsapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int MY_REQUEST_INT = 177;
    private GoogleMap mMap;
    private LocationManager locationManager;
    private Location myLocation;
    private boolean gotMyLocationOneTime;
    private boolean isGPSEnabled = false;
    private boolean isNetworkEnabled = false;
    private boolean canGetLocation = false;
     private double latitude, longtitude;
    private double previousLatitude, previosLongtitude;
    private boolean notTrackingMyLocation;
    private int trackMarkerDropCounter =0;

    private static final long MIN_TIME_BW_UPDATES = 1000 * 5;
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 0.0f;
    private static final int MY_LOC_ZOOM_FACTOR = 17;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng Florida = new LatLng(27.3364, -82.5307);
        mMap.addMarker(new MarkerOptions().position(Florida).title("Marker in Florida"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(Florida));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, MY_REQUEST_INT);
            }
            return;
        }else{
            mMap.setMyLocationEnabled(true);
        }
    }
    public void changeView(View view){
        if(mMap.getMapType()==1){
            mMap.setMapType(2);
        }
        else if(mMap.getMapType()==2){
            mMap.setMapType(1);
        }
    }
    public void getLocation(){
      try {
          locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

          isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
          if (isGPSEnabled) Log.d("MyMaps", "getLocation: GPS is enabled");

          isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
          if (isNetworkEnabled) Log.d("MyMaps", "getLocation: Network is enabled");

          if (!isGPSEnabled && !isNetworkEnabled) {
              Log.d("MyMaps", "getLocation: No provider is enabled");
          } else {
              if (isNetworkEnabled) {
                  Log.d("MyMaps", "getLocation: Network Enabled - requesting Location Updates");
                  if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                          && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                      return;
                  }

                  locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                          MIN_TIME_BW_UPDATES,
                          MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                  Log.d("MyMaps","getLocation: NetworkLoc update request successful");
              }
              if(isGPSEnabled){
                  locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                          MIN_TIME_BW_UPDATES,
                          MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerGps);
              }
          }

      }catch (Exception e){
          Log.d("MyMaps", "Caught exception in getLocation()");
          e.printStackTrace();
      }
    }
    LocationListener locationListenerNetwork = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d("MyMaps","locationListenNetwork - onLocationChanged dropping the marker");
            dropAmarker(LocationManager.NETWORK_PROVIDER);

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    private void dropAmarker(String networkProvider) {
    }

    LocationListener locationListenerGps = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

}
