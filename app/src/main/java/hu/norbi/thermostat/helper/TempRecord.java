package hu.norbi.thermostat.helper;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class TempRecord implements Serializable {
    private int roomId;
    private int sensor;
    public long newTimestamp;
    private float temperature;

    public TempRecord(int roomId, int sensor, long newTimestamp, float temperature) {
        this.roomId = roomId;
        this.sensor = sensor;
        this.newTimestamp = newTimestamp;
        this.temperature = temperature;
    }

    public int getRoomId() {
        return roomId;
    }

    public int getSensor() {
        return sensor;
    }

    public long getNewTimestamp() {
        return newTimestamp;
    }

    public float getTemperature() {
        return temperature;
    }

    @Override
    @NonNull
    public String toString() {
        return "TempRecord{" +
                "roomId=" + roomId +
                ", sensor=" + sensor +
                ", newTimestamp=" + newTimestamp +
                ", temperature=" + temperature +
                '}';
    }
}