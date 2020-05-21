package hu.norbi.thermostat.helper;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import hu.norbi.thermostat.R;

public class MainActivityViewModel extends AndroidViewModel implements Serializable {

    private final long REFERENCE_TIMESTAMP;
    private long lastChangeTime;
    private String targetTemp;
    private String currentTemp;
    private String icon;
    private String uptime;
    private String state;
    private List<TempRecord> tempRecords = new ArrayList<>();
    private List<Integer> sliderPositions = new ArrayList<>();

    public MainActivityViewModel(@NonNull Application application) {
        super(application);
        REFERENCE_TIMESTAMP = application.getResources().getInteger(R.integer.referenceTimestamp);
    }

    long getNewNow() {
        return System.currentTimeMillis() / 1000L - REFERENCE_TIMESTAMP;
    }

    long getLastChangeTime() {
        return lastChangeTime;
    }

    String getTargetTemp() {
        return targetTemp;
    }

    void setTargetTemp(String targetTemp) {
        this.targetTemp = targetTemp;
        this.lastChangeTime = getNewNow();
    }

    String getCurrentTemp() {
        return currentTemp;
    }

    void setCurrentTemp(String currentTemp) {
        this.currentTemp = currentTemp;
        this.lastChangeTime = getNewNow();
    }

    String getIcon() {
        return icon;
    }

    void setIcon(String icon) {
        this.icon = icon;
        this.lastChangeTime = getNewNow();
    }

    String getUptime() {
        return uptime;
    }

    void setUptime(String uptime) {
        this.uptime = uptime;
        this.lastChangeTime = getNewNow();
    }

    String getState() {
        return state;
    }

    void setState(String state) {
        this.state = state;
        this.lastChangeTime = getNewNow();
    }

    public List<TempRecord> getTempRecords() {
        return tempRecords;
    }

    public void addTempRecord(TempRecord tempRecord) {
        this.tempRecords.add(tempRecord);
        this.lastChangeTime = getNewNow();
    }

    public void setTempRecords(ArrayList<TempRecord> tempRecords) {
        this.tempRecords = tempRecords;
    }

    public void setSliderPositions(int value, int value1, int value2) {
        this.sliderPositions.clear();
        this.sliderPositions.add(value);
        this.sliderPositions.add(value1);
        this.sliderPositions.add(value2);
    }

    public List<Integer> getSliderPositions() {
        return sliderPositions;
    }

    public void setSliderPosition(int slider, int value) {
        this.sliderPositions.set(slider, value);
    }

    public Integer getSliderPosition(int slider) {
        return sliderPositions.get(slider);
    }

    @Override
    @NonNull
    public String toString() {
        return "MainActivityViewModel{" +
                "REFERENCE_TIMESTAMP=" + REFERENCE_TIMESTAMP +
                ", lastChangeTime=" + lastChangeTime +
                ", targetTemp='" + targetTemp + '\'' +
                ", currentTemp='" + currentTemp + '\'' +
                ", icon='" + icon + '\'' +
                ", uptime='" + uptime + '\'' +
                ", state='" + state + '\'' +
                ", tempRecords=" + tempRecords +
                ", sliderPositions=" + sliderPositions +
                '}';
    }
}
