package com.example.mymapsapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int MY_REQUEST_INT = 177;
    private GoogleMap mMap;
    private EditText locationSearch;
    private LocationManager locationManager;
    private Location myLocation;
    private boolean gotMyLocationOneTime;
    private boolean isGPSEnabled = false;
    private boolean isNetworkEnabled = false;
    private boolean canGetLocation = false;
    private double latitude, longtitude;
    private double previousLatitude, previosLongtitude;
    private boolean notTrackingMyLocation;
    private int trackMarkerDropCounter = 0;

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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, MY_REQUEST_INT);
            }
            return;
        } else {
            mMap.setMyLocationEnabled(true);
        }
        locationSearch = (EditText) findViewById(R.id.editText_addr);
        gotMyLocationOneTime = false;
        getLocation();
    }

    public void changeView(View view) {
        if (mMap.getMapType() == 1) {
            mMap.setMapType(2);
        } else if (mMap.getMapType() == 2) {
            mMap.setMapType(1);
        }
    }

    public void onSearch(View view) {
        String location = locationSearch.getText().toString();

        List<Address> addressList = null;
        List<Address> addressListZip = null;

        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = service.getBestProvider(criteria, false);

        Log.d("MyMaps", "onSearch: location= " + location);
        Log.d("MyMaps", "onSearch: provider= " + provider);

        LatLng userlocation = null;
        try {
            if (locationManager != null) {
                Log.d("MyMaps", "onSearch: locationManager is not null");
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                if ((myLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)) != null) {
                    userlocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                    Log.d("MyMaps", "onSearch: using GPS_PROVIDER userLocation is" + myLocation.getLatitude() + ", " + myLocation.getLongitude());
                    Toast.makeText(this, "UserLoc" + myLocation.getLatitude() + myLocation.getLongitude(), Toast.LENGTH_SHORT);
                } else if ((myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)) != null) {
                    userlocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                    Log.d("myMaps", "onSearch: using GPS_PROVIDER userLocation is " + myLocation.getLatitude() + ", " + myLocation.getLongitude());
                    Toast.makeText(this, "UserLoc" + myLocation.getLongitude() + myLocation.getLatitude(), Toast.LENGTH_SHORT);
                } else {
                    Log.d("MyMaps", "onSearch: myLocation is null from getLastKnownLocation with Network provider");
                }
            } else {
                Log.d("MyMaps", "onSearch: myLocation is null!!!");
            }

        } catch (SecurityException | IllegalArgumentException e) {
            Log.d("MyMaps", "Exception getLastKnownLoction");
            Toast.makeText(this, "Exception getLastKnownLocation", Toast.LENGTH_SHORT);

        }
        if (!location.matches("")) {
            Log.d("MyMaps", "onSearch: location field is populated");

            Geocoder geocoder = new Geocoder(this, Locale.US);
            Log.d("MyMaps", "onSearch: created a new Geocoder");
            try {
                addressList = geocoder.getFromLocationName(location, 10000,
                        userlocation.latitude - (5.0 / 60.0),
                        userlocation.longitude - (5.0 / 60.0),
                        userlocation.latitude + (5.0 / 60.0),
                        userlocation.longitude + (5.0 / 60.0));
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!addressList.isEmpty()) {
                Log.d("MyMaps", "Address list size= " + addressList.size());
                for (int x = 0; x < addressList.size(); x++) {
                    Address address = addressList.get(x);
                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                    mMap.addMarker(new MarkerOptions().position(latLng).title(x + ": " + address.getSubThoroughfare() + " " + address.getThoroughfare()));
                    Log.d("MyMaps", "onSearch: added Marker");
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                }
            }

        }

    }

    public void getLocation() {
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
                    Log.d("MyMaps", "getLocation: NetworkLoc update request successful");
                }
                if (isGPSEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerGps);
                }
            }

        } catch (Exception e) {
            Log.d("MyMaps", "Caught exception in getLocation()");
            e.printStackTrace();
        }
    }


    LocationListener locationListenerNetwork = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d("MyMaps", "locationListenNetwork - onLocationChanged dropping the marker");
            dropAmarker(LocationManager.NETWORK_PROVIDER);

            if (gotMyLocationOneTime == false) {
                locationManager.removeUpdates(this);
                locationManager.removeUpdates(locationListenerGps);
                gotMyLocationOneTime = true;
                previousLatitude = latitude;
                previosLongtitude = longtitude;
            } else {
                if (ActivityCompat.checkSelfPermission(MapsActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    //TODO: Consider Calling
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, (LocationListener) this);

            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                case LocationProvider.AVAILABLE:
                    Log.d("MyMaps", "locationListener - onStatusChanged AVAIL");
                    Toast.makeText(MapsActivity.this, "locationListener - onStatusChanged - AVAIL", Toast.LENGTH_SHORT).show();
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    Log.d("MyMaps", "locationListenGPS - onStatusChanged OUTOFSERVICE");
                    Toast.makeText(MapsActivity.this, "locationListenGPS - onStatusChanged - OUT", Toast.LENGTH_SHORT).show();
                    if (ActivityCompat.checkSelfPermission(MapsActivity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this,
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        //TODO: Consider Calling
                        return;
                    }
                    if (!notTrackingMyLocation) {
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                    }
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.d("MyMaps", "locationListenGPS - onStatusChanged TEMP UNAVAIL");
                    Toast.makeText(MapsActivity.this, "locationListenGPS - onStatusChanged - TEMP UNAVAIL", Toast.LENGTH_SHORT).show();

                    if (!notTrackingMyLocation) {
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                    }
                    break;
                default:
                    Log.d("MyMaps", "locationListenGPS - onStatusChanged default");
                    Toast.makeText(MapsActivity.this, "locationListenGPS - onStatusChanged - default", Toast.LENGTH_SHORT).show();
                    if (!notTrackingMyLocation) {
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                    }
                    break;


            }

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    private void dropAmarker(String networkProvider) {
        if(locationManager != null){
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            myLocation = locationManager.getLastKnownLocation(networkProvider);

            if(myLocation !=null){
                latitude = myLocation.getLatitude();
                longtitude = myLocation.getLongitude();
            }

        }
        LatLng userLocation = null;
        if(myLocation ==null){
            Toast.makeText(this, "dropAmarker: myLocation is null can't show location!!", Toast.LENGTH_SHORT).show();

        }else{
            userLocation = new LatLng(myLocation. getLatitude(),myLocation.getLongitude());

            Log.d("MyMaps","dropAmarker: Provider: "+ networkProvider + " "+ myLocation.getLatitude()+ " " + myLocation.getLongitude());
            Log.d("MyMaps","dropAmarker: myLocation accuracy: " + myLocation.getAccuracy());

            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(userLocation,MY_LOC_ZOOM_FACTOR);

            if(networkProvider == LocationManager.GPS_PROVIDER){
                Circle circle = mMap.addCircle(new CircleOptions().center(userLocation).radius(1).strokeColor(Color.RED).strokeWidth(2).fillColor(Color.RED));
                Circle circleOuterRing1 = mMap.addCircle(new CircleOptions().center(userLocation).radius(3).strokeColor(Color.RED).strokeWidth(2).fillColor(Color.TRANSPARENT));
                Circle circleOuterRing2 = mMap.addCircle(new CircleOptions().center(userLocation).radius(5).strokeColor(Color.RED).strokeWidth(2).fillColor(Color.TRANSPARENT));
            }else{
                Circle circle = mMap.addCircle(new CircleOptions().center(userLocation).radius(1).strokeColor(Color.BLUE).strokeWidth(2).fillColor(Color.BLUE));
                Circle circleOuterRing1 = mMap.addCircle(new CircleOptions().center(userLocation).radius(3).strokeColor(Color.BLUE).strokeWidth(2).fillColor(Color.TRANSPARENT));
                Circle circleOuterRing2 = mMap.addCircle(new CircleOptions().center(userLocation).radius(5).strokeColor(Color.BLUE).strokeWidth(2).fillColor(Color.TRANSPARENT));
            }
            mMap.animateCamera(update);

        }

    }
    public void trackMyLocation(View view){
        Log.d("MyMaps", "Tracking now");
        if(notTrackingMyLocation){
            getLocation();
            Toast.makeText(this, "trackMyLocation: tracking is ON", Toast.LENGTH_SHORT).show();
            Log.d("MyMaps","trackMyLocation: tracking is ON");
            notTrackingMyLocation = false;
        }else{
            locationManager.removeUpdates(locationListenerGps);
            locationManager.removeUpdates(locationListenerNetwork);
            Toast.makeText(this,"trackMyLocation: tracking is OFF", Toast.LENGTH_SHORT).show();
            Log.d("MyMaps","trackMyLocation: tracking is OFF");
            notTrackingMyLocation = true;
        }
    }
    public void clearMarkers(View view){
        trackMarkerDropCounter =0;
        mMap.clear();
    }


    LocationListener locationListenerGps = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d("MyMaps", "locationListenGPS - onLocationChanged dropping the marker");
            dropAmarker(LocationManager.GPS_PROVIDER);

            if (gotMyLocationOneTime == false) {
                locationManager.removeUpdates(this);
                locationManager.removeUpdates(locationListenerNetwork);
                gotMyLocationOneTime = true;
                previousLatitude = latitude;
                previosLongtitude = longtitude;
            } else {
                if (ActivityCompat.checkSelfPermission(MapsActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    //TODO: Consider Calling
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, (LocationListener) this);

            }

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                case LocationProvider.AVAILABLE:
                    Log.d("MyMaps", "locationListener - onStatusChanged AVAIL");
                    Toast.makeText(MapsActivity.this, "locationListener - onStatusChanged - AVAIL", Toast.LENGTH_SHORT).show();
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    Log.d("MyMaps", "locationListenGPS - onStatusChanged OUTOFSERVICE");
                    Toast.makeText(MapsActivity.this, "locationListenGPS - onStatusChanged - OUT", Toast.LENGTH_SHORT).show();
                    if (ActivityCompat.checkSelfPermission(MapsActivity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this,
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        //TODO: Consider Calling
                        return;
                    }
                    if (!notTrackingMyLocation) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerGps);
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                    }
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.d("MyMaps", "locationListenGPS - onStatusChanged TEMP UNAVAIL");
                    Toast.makeText(MapsActivity.this, "locationListenGPS - onStatusChanged - TEMP UNAVAIL", Toast.LENGTH_SHORT).show();

                    if (!notTrackingMyLocation) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerGps);
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                    }
                    break;
                default:
                    Log.d("MyMaps", "locationListenGPS - onStatusChanged default");
                    Toast.makeText(MapsActivity.this, "locationListenGPS - onStatusChanged - default", Toast.LENGTH_SHORT).show();
                    if (!notTrackingMyLocation) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerGps);
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                    }
                    break;
            }

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

}