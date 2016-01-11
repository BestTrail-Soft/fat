package sk.besttrailsoft.fat.mock;

import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Adriana on 6.12.2015.
 */
public class MovingObjectMock {
    private List<LatLng> itinerary = new ArrayList<>();
    private int index = 0;
    private static String locationProvider = LocationManager.GPS_PROVIDER;
    private Timer timer;
    private TimerTask task;
    GoogleApiClient locationClient;
    LatLng firstPosition;

    public MovingObjectMock(GoogleApiClient locationClient, LatLng firstPosition) {
        this.locationClient = locationClient;
        setMockLocation(firstPosition, 100.0f);
    }

    public MovingObjectMock(LatLng firstPosition) {
        if(firstPosition == null)
            throw new NullPointerException("firstPosition");

        this.firstPosition = firstPosition;
    }

    /**
     * @return false if already passes through itinerary - cannot be set
     **/
    public boolean setItinerary(List<LatLng> itinerary){
        if(index != 0)
            return false;

        this.itinerary.addAll(itinerary);

        return true;
    }

    public boolean isFinished() {
        return itinerary == null || index >= itinerary.size();
    }

    public void setLocationClient(GoogleApiClient locationClient) {
        if(locationClient == null)
            throw new NullPointerException("locationClient");

        this.locationClient = locationClient;
    }

    public LatLng getPosition() {
        if(isFinished())
            return null;

        return itinerary.get(index);
    }

    private void setMockLocation(LatLng position, float accuracy){
        Location newLocation = createNewLocation(position, accuracy);

        updateLocation(newLocation);
    }

    private void updateLocation(Location location) {
        LocationServices.FusedLocationApi.
                setMockLocation(locationClient, location);
    }

    /**
     * @return can startTimer - it is not the end of the itinerary
     */
    public boolean startTimer(long period) {
        if(locationClient == null || isFinished() || firstPosition == null)
            return false;

        setMockLocation(firstPosition, 100.0f);

        timer = new Timer();
        task = createTask();
        timer.schedule(task, period, period);
        return true;
    }

    public void stop(){
        if(task != null)
            task.cancel();
        if(timer != null)
            timer.cancel();
    }

    private TimerTask createTask(){
        if(isFinished())
            return null;

        return new TimerTask() {
            @Override
            public void run() {
                updateLocation(createNewLocation(itinerary.get(index),1.0f));
                index++;
                if(isFinished()) {
                    cancel();
                }
            }
        };
    }

    private Location createNewLocation(LatLng position, float accuracy) {
        Location newLocation = new Location(locationProvider);
        newLocation.setLatitude(position.latitude);
        newLocation.setLongitude(position.longitude);
        newLocation.setAccuracy(accuracy);
        long time = System.currentTimeMillis();
        newLocation.setTime(time);
        try {
            Method locationJellyBeanFixMethod = Location.class.getMethod("makeComplete");
            if (locationJellyBeanFixMethod != null) {
                locationJellyBeanFixMethod.invoke(newLocation);
            }
        }catch (NoSuchMethodException ex) {
            Log.w("No makeComplete method", ex);
        }catch(InvocationTargetException | IllegalAccessException ex) {
            Log.w("Invocation makeComplete", ex);
        }

        return newLocation;
    }
}
