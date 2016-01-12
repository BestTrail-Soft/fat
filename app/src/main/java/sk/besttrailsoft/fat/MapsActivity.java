package sk.besttrailsoft.fat;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import sk.besttrailsoft.fat.mock.MovingObjectMock;
import sk.besttrailsoft.fat.program.ProgramIndexListener;
import sk.besttrailsoft.fat.program.ProgramIterator;
import sk.besttrailsoft.fat.program.ProgramManager;
import sk.besttrailsoft.fat.program.ProgramStep;
import sk.besttrailsoft.fat.route.CreateRouteActivity;

public class MapsActivity extends FragmentActivity implements LocationListener, OnMapReadyCallback, ProgramIndexListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private final String TAG = "FAT-MapsActivity";

    private GoogleMap map;
    private ArrayList<LatLng> waypoints = new ArrayList<>();
    private ArrayList<LatLng> pathPassed = new ArrayList<>();
    private ArrayList<String> waypointsNames;
    private int nextPointStep = 1;
    private ArrayList<Marker> markers = new ArrayList<>();
    private ProgramIterator programIterator = null;

    private TextView distancePassedTextView;
    private TextView instructionValueTextView;
    private ProgressDialog progressDialog;

    private float passedInMeters = 0;
    private long firstLocationReceived;
    private boolean showedMessageUnder10m = false;

    LocationRequest locationRequest;
    GoogleApiClient locationClient;

    //MOCK
    MovingObjectMock movingMock = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        progressDialog = ProgressDialog.show(MapsActivity.this, "Loading", "Connecting...", true);
        distancePassedTextView = (TextView) findViewById(R.id.passedDistanceValueText);
        instructionValueTextView = (TextView) findViewById(R.id.instructionValueText);

        //MOCK
        movingMock = new MovingObjectMock(getLocationFromAddress("Gánovská 221/30, Gánovce, Slovensko"));

        if(isPredefinedRoute()){
            LatLng place;
            waypointsNames = getIntent().getStringArrayListExtra("places");
            for (String point : waypointsNames) {
                place = getLocationFromAddress(point);
                if (place != null)
                    waypoints.add(place);
            }
            //MOCK
            movingMock.setItinerary(getRoutePointsFromWaypoints());
        }

        locationClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        locationClient.connect();

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
        map.setMyLocationEnabled(true);
    }

    @Override
    public void onIndexChanged() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (programIterator.isFinished())
                    instructionValueTextView.setText("None");
                else {
                    ProgramStep prg = programIterator.getCurrentStep();
                    String length = prg.getDistance() == null ?
                            prg.getTime() + "min" : prg.getDistance() + "m";
                    instructionValueTextView.setText(prg.getText() + " " + length);
                }
            }
        });

        if(!programIterator.isFinished())
            programIterator.startStep();
    }

    private boolean isPredefinedRoute() {
        ArrayList<String> route = getIntent().getStringArrayListExtra("places");
        return route!=null && route.size() >= 2;
    }

    private LatLng getCurrentLocation() {
        LatLng result = null;
        if(pathStarted())
            result = pathPassed.get(pathPassed.size()-1);

        return result;
    }

    @Override
    protected void onDestroy() {
        //MOCK
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
                        .zoom(17)
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

    private String getAddressFromPoint(LatLng point){

        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());
        StringBuilder address = new StringBuilder();

        try {
            addresses = geocoder.getFromLocation(point.latitude, point.longitude, 1);
            for (int i = 0; i<addresses.get(0).getMaxAddressLineIndex(); i++) {
                address.append(addresses.get(0).getAddressLine(i));
                if(i != addresses.get(0).getMaxAddressLineIndex()-1)
                    address.append(" ");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return address.toString();
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

            if(programIterator != null)
                programIterator.updateDistance(passed);
        }

        if(size > 0) {
            float distanceToFinish = DirectionsApiHelper.distance(pathPassed.get(size - 1), waypoints.get(waypoints.size()-1));
            if(distanceToFinish < 2)
                showAlertDialog("Finish is HERE!");
            else if(distanceToFinish < 10 && !showedMessageUnder10m) {
                showedMessageUnder10m = true;
                showAlertDialog("Finish is closer than 10 meters!");
            }
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
        movingMock.setLocationClient(locationClient);
        movingMock.startTimer(1000);

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
            setupItinerary(location);
            firstLocationReceived = System.currentTimeMillis();
            progressDialog.dismiss();
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

    private void setupItinerary(Location location) {
        waypointsNames = getIntent().getStringArrayListExtra("places");

        if (waypointsNames == null || waypointsNames.size() < 2) {

            if(waypointsNames != null && waypointsNames.size() == 1) {
                waypoints.add(waypoints.get(0));
            }
            waypoints.add(new LatLng(location.getLatitude(), location.getLongitude()));
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
                    .setText(String.format("%.2f", DirectionsApiHelper.distance(waypoints)/2));
        }

        LatLng position = getCurrentLocation();
        if (position == null) {
            setCamera(waypoints.get(0));
        } else {
            setCamera(position);
        }
    }

    private void showAlertDialog(String text) {
        new AlertDialog.Builder(this)
                .setTitle("Notice")
                .setMessage(text)
                .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public void generateEvaluationDialog(View view) {
        float timePassedInMinutes = ((float)(System.currentTimeMillis() - firstLocationReceived))/60000.0f;
        new AlertDialog.Builder(this)
                .setTitle("Evaluation")
                .setMessage("Distance passed: " + String.format("%.2f", passedInMeters) + "meters\n"+
                "Time:" + String.format("%.2f", timePassedInMinutes) + "min\n" +
                "Average speed:" + String.format("%.3f", ((float)passedInMeters/1000.0f)/(timePassedInMinutes/60)) + "km/h\n" )
                .setNeutralButton("Continue", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setPositiveButton("Save Route", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (!startSaveRouteActivityIntent()) {
                            new AlertDialog.Builder(getThis())
                                    .setTitle("Warning")
                                    .setMessage("Cant create routes right now")
                                    .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    })
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show();
                        }
                    }
                })
                .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        startMainActivityIntent();
                        finish();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }

    private void startMainActivityIntent() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private boolean startSaveRouteActivityIntent() {
        Intent intent = new Intent(this, CreateRouteActivity.class);
        ArrayList<String> places = new ArrayList<>();
        int index = 0;
        if(pathPassed.size()>4){
            int step = pathPassed.size() / 4;
            places.add(getAddressFromPoint(pathPassed.get(index)));
            index += step;
            places.add(getAddressFromPoint(pathPassed.get(index)));
            index += step;
            places.add(getAddressFromPoint(pathPassed.get(index)));
            index = pathPassed.size()-1;
            places.add(getAddressFromPoint(pathPassed.get(index)));
        } else {
            for(LatLng place : pathPassed) {
                places.add(getAddressFromPoint(place));
            }
        }
        for(String place : places) {
            if(place == null || place.isEmpty())
                return false;
        }
        intent.putExtra("places", places);
        startActivity(intent);
        return true;
    }

    private Context getThis() { return this; }
}
