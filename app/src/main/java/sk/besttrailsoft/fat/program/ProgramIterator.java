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

    public ProgramIterator(Program program) {
        if(program == null)
            throw new NullPointerException("program cannot be null");
        if(program.getSteps().size() < 1)
            throw new IllegalArgumentException("program cannot be empty");

        this.program = program;
        finished = false;
    }

    public void addListener(ProgramIndexListener listener) {
        listeners.add(listener);
    }

    public boolean isFinished() {
        if(program.getSteps().size() - 1 < index)
            finished = true;
        return finished;
    }

    public void updateDistance(float distance) {
        if(locationStep) {
            passedDistance += distance;
            if(passedDistance >= program.getSteps().get(index).getDistance()) {
                passedDistance = 0.0f;
                index++;
                started = false;
                locationStep = false;
                notifyListeners();
            }
        }
    }

    public boolean startStep() {
        if(finished || started)
            return false;
        if(index == program.getSteps().size()) {
            finished = true;
        } else if(index > program.getSteps().size()) {
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

    public ArrayList<ProgramStep> getProgramSteps() {
        return program.getSteps();
    }

    private void onLocationStep() {
        locationStep = true;
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

    private void notifyListeners() {
        for (ProgramIndexListener listener : listeners)
            listener.onIndexChanged();
    }
}
