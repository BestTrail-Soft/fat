package sk.besttrailsoft.fat;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
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

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sk.besttrailsoft.fat.mock.MovingObjectMock;
import sk.besttrailsoft.fat.program.ProgramIndexListener;
import sk.besttrailsoft.fat.program.ProgramIterator;
import sk.besttrailsoft.fat.program.ProgramManager;

public class MapsActivity extends FragmentActivity implements LocationListener, OnMapReadyCallback, ProgramIndexListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private final String TAG = "FAT-MapsActivity";

    private GoogleMap map;
    private ArrayList<LatLng> waypoints = new ArrayList<>();
    private ArrayList<LatLng> pathPassed = new ArrayList<>();
    private ArrayList<String> waypointsNames;
    private int nextPointStep = 1;
    private LocationManager locationManager;
    private ArrayList<Marker> markers = new ArrayList<>();
    private ProgramIterator programIterator = null;
    private String providerType;

    private TextView distancePassedTextView;
    private TextView instructionValueTextView;
    private ProgressDialog progressDialog;

    private float passedInMeters = 0;

    LocationRequest locationRequest;
    GoogleApiClient locationClient;

    //MOCK
    MovingObjectMock movingMock = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        //progressDialog = ProgressDialog.show(getApplicationContext(), "Loading", "Please wait...", true);
        distancePassedTextView = (TextView) findViewById(R.id.passedDistanceValueText);
        instructionValueTextView = (TextView) findViewById(R.id.instructionValueText);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //MOCK
        movingMock = new MovingObjectMock(locationManager, getLocationFromAddress("Gánovská 221/30, Gánovce, Slovensko"));
        if(isPredefinedRoute()){
            LatLng place = null;
            waypointsNames = getIntent().getStringArrayListExtra("places");
            for (String point : waypointsNames) {
                place = getLocationFromAddress(point);
                if (place != null)
                    waypoints.add(place);
            }

            setupMock(getRoutePointsFromWaypoints());
        }

        locationClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        //progressDialog = ProgressDialog.show(MapsActivity.this, "Loading", "Please wait...", true);
        locationClient.connect();

//        progressDialog.dismiss();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //progressDialog.dismiss();
    }

    /**
     * Manipulates the map once available.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMyLocationEnabled(true);
    }

    @Override
    public void onIndexChanged() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (programIterator.isFinished())
                    instructionValueTextView.setText("None");
                else
                    instructionValueTextView.setText(programIterator.getCurrentStep().getText());
            }
        });

        if(!programIterator.isFinished())
            programIterator.startStep();
    }

    private boolean isPredefinedRoute() {
        ArrayList<String> route = getIntent().getStringArrayListExtra("places");
        return route!=null && route.size() >= 2;
    }

    private LatLng getCurrenttLocation() {
        if(pathPassed.size() > 0) {
            return pathPassed.get(pathPassed.size() - 1);
        }
        List<String> providers = locationManager.getAllProviders();
        Location location = null;
        for (String provider : providers) {
            Location l = null;
            try {
               l = locationManager.getLastKnownLocation(provider);
            }catch (SecurityException ex){
                ex.printStackTrace();
            }
            if (l == null) {
                continue;
            }
            if (location == null || l.getAccuracy() < location.getAccuracy()) {
                // Found best last known location: %s", l);
                location = l;
                providerType = provider;
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
                //locationManager.requestLocationUpdates(providerType, 0, 0, locationListener);
                setupProgramIterator();
            } catch (SecurityException ex) {
                Log.w("ReguestLocationUpdates ", ex);
            }
            return newPoint;
        }
    }

    private LatLng getCurrentLocation() {
        LatLng result = null;
        if(pathStarted())
            result = pathPassed.get(pathPassed.size()-1);

        return result;
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
            float passed = DirectionsApiHelper.distance(pathPassed.get(size - 2), pathPassed.get(size - 1));
            passedInMeters += passed;
            distancePassedTextView.setText(String.format("%.2f", passedInMeters));

            programIterator.updateDistance(passed);
        }
        updateMarkers();
    }

    private void setupProgramIterator() {
        String programName = getIntent().getStringExtra("program");
        if(programName != null && !programName.isEmpty()) {
            try {
                programIterator = new ProgramIterator(new ProgramManager(getApplicationContext()).getProgram(programName));
                programIterator.addListener(this);
                onIndexChanged();
            } catch (Exception ex) {
                Log.w("MapsActivity", ex.toString());
            }
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        //MOCK
        LocationServices.FusedLocationApi.setMockMode(locationClient, true);

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000); // Update location every second

        LocationServices.FusedLocationApi.requestLocationUpdates(
                locationClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "GoogleApiClient connection has been suspend");
    }

    @Override
    public void onLocationChanged(Location location) {
        if(!pathStarted()) {
            setupProgramIterator();
            setupItinerary();
            //progressDialog.dismiss();
        }
        onLocationChangedEvent(location);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "GoogleApiClient connection has failed");
    }

    private boolean pathStarted() {
        return (pathPassed != null && pathPassed.size() > 0);
    }

    private void setupItinerary() {
        waypointsNames = getIntent().getStringArrayListExtra("places");

        if (waypointsNames == null || waypointsNames.size() < 2) {

            LatLng curLocation = getCurrentLocation();
            if(waypointsNames != null && waypointsNames.size() == 1) {
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

        if (waypoints.size() > 1) {
            List<LatLng> way = getRoutePointsFromWaypoints();
            PolylineOptions polylineOptions = new PolylineOptions().addAll(way)
                    .width(5).color(Color.BLUE).geodesic(true);
            map.addPolyline(polylineOptions);
            ((TextView) findViewById(R.id.totalDistanceValueText))
                    .setText(String.format("%.2f", DirectionsApiHelper.distance(way)));

            //MOCK
            //setupMock(way);
        }

        LatLng position = getCurrentLocation();
        if (position == null) {
            setCamera(waypoints.get(0));
        } else {
            setCamera(position);
        }
    }

    private void setupMock(List<LatLng> way) {
        movingMock.setItinerary(way);
        movingMock.startTimer(5000);
    }
}
