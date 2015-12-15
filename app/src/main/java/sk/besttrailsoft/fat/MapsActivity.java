package sk.besttrailsoft.fat;

import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

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
import java.util.Arrays;
import java.util.List;

import sk.besttrailsoft.fat.mock.MovingObjectMock;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap map;
    private ArrayList<LatLng> waypoints = new ArrayList<>();
    private ArrayList<LatLng> pathPassed = new ArrayList<>();
    private ArrayList<String> waypointsNames;
    private int nextPointStep = 1;
    private LocationManager locationManager;
    private ArrayList<Marker> markers = new ArrayList<>();

    private TextView distancePassedTextView;

    private float passedInMeters = 0;

    //MOCK
    MovingObjectMock movingMock = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        distancePassedTextView = (TextView) findViewById(R.id.passedDistanceValueText);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        waypointsNames = getIntent().getStringArrayListExtra("places");
        //MOCK
        movingMock = new MovingObjectMock(locationManager, getLocationFromAddress("Gánovská 221/30, Gánovce, Slovensko"));
        if (waypointsNames == null || waypointsNames.size() < 2) {

            LatLng curLocation = getCurrentLocation();
            if(waypoints != null && waypoints.size() == 1) {
                waypoints.add(waypoints.get(0));
            }
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
            List<LatLng> way = getRoutePointsFromWaypoints();
            PolylineOptions polylineOptions = new PolylineOptions().addAll(way)
                    .width(5).color(Color.BLUE).geodesic(true);
            map.addPolyline(polylineOptions);
            //MOCK
            movingMock.setItinerary(way);
        }
        map.setMyLocationEnabled(true);
        LatLng position = getCurrentLocation();
        if (position == null) {
            setCamera(waypoints.get(0));
        } else {
            setCamera(position);
        }
        updateMarkers();

        //MOCK
        movingMock.startTimer(5000);
    }

    private LatLng getCurrentLocation() {
        if(pathPassed.size() > 0) {
            return pathPassed.get(pathPassed.size() - 1);
        }
        List<String> providers = locationManager.getAllProviders();
        Location location = null;
        String bestProvider = null;
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
                bestProvider = provider;
            }
        }
        if(location == null) {
            return null;
        } else {
            LatLng newPoint = new LatLng(location.getLatitude(), location.getLongitude());
            pathPassed.add(newPoint);
            LocationListener locationListener =  new LocationListener() {
                public void onLocationChanged(Location location) {
                    onLocationChangedEvent(location);
                }

                public void onStatusChanged(String provider, int status, Bundle extras) {}

                public void onProviderEnabled(String provider) {}

                public void onProviderDisabled(String provider) {}
            };
            try {
                locationManager.requestLocationUpdates(bestProvider, 0, 0, locationListener);
            } catch (SecurityException ex) {
                Log.w("ReguestLocationUpdates ", ex);
            }
            return newPoint;
        }
    }

    @Override
    protected void onDestroy() {
        movingMock.stop();
        super.onDestroy();
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
                if (curPos == null)
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
        if (waypoints.size() < 2 || nextPointStep >= waypoints.size())
            return;
        markers.add(map.addMarker(new MarkerOptions()
                .position(waypoints.get(waypoints.size() - 1)).title("Finish")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))));
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

    private void onLocationChangedEvent(Location location) {
        pathPassed.add(new LatLng(location.getLatitude(), location.getLongitude()));
        int size = pathPassed.size();
        if(size > 1) {
            map.addPolyline(new PolylineOptions()
                    .addAll(Arrays.asList(pathPassed.get(size - 2), pathPassed.get(size - 1)))
                    .width(5).color(Color.GREEN).geodesic(true));
            passedInMeters += DirectionsApiHelper.distance(pathPassed.get(size - 2), pathPassed.get(size - 1));
            distancePassedTextView.setText(String.format("%.2f", passedInMeters));
        }
        updateMarkers();
    }
}
