package hu.norbi.thermostat.helper;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import java.util.ArrayList;
import java.util.List;

import hu.norbi.thermostat.R;

public class MainActivityViewModel extends AndroidViewModel {

    private final long REFERENCE_TIMESTAMP;
    private long lastChangeTime;
    private String targetTemp;
    private String currentTemp;
    private String icon;
    private String uptime;
    private String state;
    private List<TempRecord> tempRecords = new ArrayList<>();

    public MainActivityViewModel(@NonNull Application application) {
        super(application);
        REFERENCE_TIMESTAMP = application.getResources().getInteger(R.integer.referenceTimestamp);
    }

    public static class TempRecord {
        int roomId;
        int sensor;
        public long newTimestamp;
        float temperature;

        public TempRecord(int roomId, int sensor, long newTimestamp, float temperature) {
            this.roomId = roomId;
            this.sensor = sensor;
            this.newTimestamp = newTimestamp;
            this.temperature = temperature;
        }
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
}
