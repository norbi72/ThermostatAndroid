package hu.norbi.thermostat.helper;

import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class MainActivityViewModel extends ViewModel {

    public String targetTemp;
    public String currentTemp;
    public String icon;
    public String uptime;
    public String state;
    public List<TempRecord> tempRecords = new ArrayList<>();

    public static class TempRecord {
        public int roomId;
        public int sensor;
        public long newTimestamp;
        public float temperature;

        public TempRecord(int roomId, int sensor, long newTimestamp, float temperature) {
            this.roomId = roomId;
            this.sensor = sensor;
            this.newTimestamp = newTimestamp;
            this.temperature = temperature;
        }
    }
}
