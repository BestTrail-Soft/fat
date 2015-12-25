package sk.besttrailsoft.fat.program;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Adriana on 15.12.2015.
 */

public class ProgramIterator{
    private Program program;
    private int index = 0;
    private boolean started = false;
    private boolean paused = false;
    private boolean finished = true;
    private Timer timer = new Timer();
    private List<ProgramIndexListener> listeners = new ArrayList<>();

    private float passedDistance = 0;
    private boolean locationStep = false;

    //String provider;
    //LocationManager locationManager;

    public ProgramIterator(Program program) {
        if(program == null)
            throw new NullPointerException("program cannot be null");
        if(program.getSteps().size() < 1)
            throw new IllegalArgumentException("program cannot be empty");
        //if(locationClient == null)
        //    throw new NullPointerException("locationClient cannot be null");
        //if(locationManager == null)
        //    throw new NullPointerException("locationManager cannot be null");

        this.program = program;
        finished = false;
    }

    public void addListener(ProgramIndexListener listener) {
        listeners.add(listener);
    }

    public boolean isFinished() {
        if(program.getSteps().size() - 1 <= index)
            finished = true;
        return finished;
    }

    public void updateDistance(float distance) {
        if(locationStep) {
            passedDistance += distance;
            if(passedDistance >= program.getSteps().get(index).getDistance()) {
                if(!finished)
                    index++;
                started = false;
                notifyListeners();
                locationStep = false;
            }
        } else {
            passedDistance = 0;
        }
    }

    public boolean startStep() {
        if(finished || started)
            return false;
        if(index >= program.getSteps().size() - 1) {
            finished = true;
            return false;
        }
        started = true;
        ProgramStep step = program.getSteps().get(index);
        if(step.getTime() == null) {
            onLocationStep();
        } else {
            onTimeStep();
        }
        return true;
    }

    public ProgramStep getCurrentStep() {
        return program.getSteps().get(index);
    }

    public ProgramStep getNextStep() {
        if(program.getSteps().size() < index + 2)
            return null;
        return program.getSteps().get(index + 1);
    }

    public ArrayList<ProgramStep> getProgramSteps() {
        return program.getSteps();
    }

    private void onLocationStep() {
        locationStep = true;
        //try {
        //    locationManager.requestLocationUpdates(provider, 60*60*60*1000, program.getSteps().get(index).getDistance(), createLocationListener(), Looper.getMainLooper());
        //} catch (SecurityException ex) {
        //    Log.w("ReguestLocationUpdates ", ex);
        //}
    }

    private void onTimeStep() {
        long period = program.getSteps().get(index).getTime() * 60 * 1000;
        timer.schedule(createTimerTask(), period, period);
    }

    private TimerTask createTimerTask(){
        return new TimerTask() {
            @Override
            public void run() {
                if(!finished)
                    index++;
                this.cancel();
                started = false;
                notifyListeners();
                timer.purge();
            }
        };
    }

    //private LocationListener createLocationListener() {
    //    return new LocationListener() {
    //        public void onLocationChanged(Location location) {
    //            if(!finished)
    //                index++;
    //            started = false;
    //            notifyListeners();
    //            try {
    //                locationManager.removeUpdates(this);
    //            }catch (SecurityException ex) {
    //                Log.w("ProgramIterator", ex.toString());
    //            }
//
    //        }
//
    //        public void onStatusChanged(String provider, int status, Bundle extras) {}
//
    //        public void onProviderEnabled(String provider) {}
//
    //        public void onProviderDisabled(String provider) {}
    //    };
    //}

    private void notifyListeners() {
        for (ProgramIndexListener listener : listeners)
            listener.onIndexChanged();
    }
}
