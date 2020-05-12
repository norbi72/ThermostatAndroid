package hu.norbi.thermostat.helper;

import android.util.Pair;

public class RoomSensorPair extends Pair<Integer, Integer> {
    /**
     * Constructor for a Pair.
     *
     * @param roomId  the first object in the Pair
     * @param sensor the second object in the pair
     */
    public RoomSensorPair(Integer roomId, Integer sensor) {
        super(roomId, sensor);
    }

    public int getRoomId() {
        return this.first;
    }

    public int getSensor() {
        return this.second;
    }
}
