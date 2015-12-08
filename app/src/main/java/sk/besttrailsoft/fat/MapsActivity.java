package sk.besttrailsoft.fat;

import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap map;
    private ArrayList<LatLng> waypoints = new ArrayList<>();
    private ArrayList<String> waypointsNames;
    private int nextPointStep = 1;
    private LocationManager locationManager;
    private ArrayList<Marker> markers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //remove when dont need mockup
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mockLocation();
        waypointsNames = getIntent().getStringArrayListExtra("places");
        if (waypointsNames == null || waypointsNames.size() < 2) {
            LatLng curLocation = null;
            curLocation = getCurrentLocation();
            if (curLocation != null)
                waypoints.add(getCurrentLocation());
        } else {
            LatLng place = null;
            for (String point : waypointsNames) {
                place = getLocationFromAddress(point);
                if (place != null)
                    waypoints.add(place);
            }
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        if (waypoints == null || waypoints.size() < 1)
            return;
        if (waypoints.size() > 1) {
            PolylineOptions polylineOptions = new PolylineOptions().addAll(getRoutePointsFromWaypoints())
                    .width(5).color(Color.BLUE).geodesic(true);
            map.addPolyline(polylineOptions);
        }
        map.setMyLocationEnabled(true);
        LatLng position = getCurrentLocation();
        if (position == null) {
            setCamera(waypoints.get(0));
        } else {
            setCamera(position);
        }
        updateMarkers();
    }

    private LatLng getCurrentLocation() {
        List<String> providers = locationManager.getAllProviders();
        Location location = null;
        for (String provider : providers) {
            Location l = null;
            try {
               l = locationManager.getLastKnownLocation(provider);
            }catch (SecurityException ex){}
            if (l == null) {
                continue;
            }
            if (location == null || l.getAccuracy() < location.getAccuracy()) {
                // Found best last known location: %s", l);
                location = l;
            }
        }
        return (location == null ? null : new LatLng(location.getLatitude(), location.getLongitude()));
    }

    private void setCamera(LatLng target) {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(target)
                .zoom(15)
                .bearing(0)
                .tilt(30)
                .build();

        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        map.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                LatLng curPos = getCurrentLocation();
                if(curPos == null)
                    return false;
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(curPos)
                        .zoom(15)
                        .bearing(0)
                        .tilt(30)
                        .build();
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                return true;
            }
        });
    }

    private List<LatLng> getRoutePointsFromWaypoints() {
        List<LatLng> points = new ArrayList<>();
        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
            points = DirectionsApiHelper.getRouteFromWaypoints(waypoints);
        }

        return points;
    }

    private void updateMarkers() {
        for(Marker marker : markers){
            marker.remove();
        }
        markers.clear();

        if(waypoints.size() > 0) {
            markers.add(map.addMarker(new MarkerOptions()
                    .position(waypoints.get(0)).title("Start")));
        }
        if(waypoints.size() < 2 || nextPointStep >= waypoints.size())
            return;
        markers.add(map.addMarker(new MarkerOptions()
                .position(waypoints.get(waypoints.size() - 1)).title("Finish")));
        if (waypoints.size() - 1 > nextPointStep) {
            markers.add(map.addMarker(new MarkerOptions()
                    .position(waypoints.get(nextPointStep)).title("Next")));
        }

        LatLng myMarker = getCurrentLocation();
        if(myMarker != null) {
            markers.add(map.addMarker(new MarkerOptions()
                    .position(myMarker).title("Me")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))));
        }
    }

    private LatLng getLocationFromAddress(String strAddress){

        Geocoder coder = new Geocoder(this);
        List<Address> address;
        LatLng p1 = null;

        try {
            address = coder.getFromLocationName(strAddress,5);

            if (address==null) {
                return null;
            }
            Address location=address.get(0);
            location.getLatitude();
            location.getLongitude();

            p1 = new LatLng(location.getLatitude(), location.getLongitude());

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return p1;
    }

    private void setMockLocation(double latitude, double longitude, float accuracy) {
        locationManager.addTestProvider(LocationManager.GPS_PROVIDER,
                "requiresNetwork" == "",
                "requiresSatellite" == "",
                "requiresCell" == "",
                "hasMonetaryCost" == "",
                "supportsAltitude" == "",
                "supportsSpeed" == "",
                "supportsBearing" == "",
                android.location.Criteria.POWER_LOW,
                android.location.Criteria.ACCURACY_FINE);

        Location newLocation = new Location(LocationManager.GPS_PROVIDER);

        newLocation.setLatitude(latitude);
        newLocation.setLongitude(longitude);
        newLocation.setAccuracy(accuracy);
        long time = System.currentTimeMillis();
        newLocation.setTime(time);
        try {
            Method locationJellyBeanFixMethod = Location.class.getMethod("makeComplete");
            if (locationJellyBeanFixMethod != null) {
                locationJellyBeanFixMethod.invoke(newLocation);
            }
        }catch (NoSuchMethodException ex) {
            Log.w("No makeComplete method",ex);
        }catch(InvocationTargetException ex) {
            Log.w("Exception during invocation of makeComplete", ex);
        }catch(IllegalAccessException ex) {
            Log.w("Exception during invocation of makeComplete", ex);
        }
        locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);

        locationManager.setTestProviderStatus(LocationManager.GPS_PROVIDER,
                LocationProvider.AVAILABLE,
                null, time);

        locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, newLocation);

    }

    private void mockLocation() {
        LatLng suradnice = getLocationFromAddress("Sadová 385, 058 01 Gánovce, Slovensko");
        setMockLocation(suradnice.latitude, suradnice.longitude, 1.0f);
    }
}
