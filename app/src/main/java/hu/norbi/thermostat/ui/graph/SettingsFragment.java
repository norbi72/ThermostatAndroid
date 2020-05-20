package hu.norbi.thermostat.ui.graph;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import hu.norbi.thermostat.MainActivity;
import hu.norbi.thermostat.R;

public class SettingsFragment extends Fragment {
    private long REFERENCE_TIMESTAMP;
    private MainActivity mainActivity;

    public SettingsFragment() {
    }

    private SettingsFragment(long reference_timestamp, MainActivity mainActivity) {
        REFERENCE_TIMESTAMP = reference_timestamp;
        this.mainActivity = mainActivity;
    }

    public static SettingsFragment newInstance(MainActivity mainActivity) {
        return new SettingsFragment(mainActivity.REFERENCE_TIMESTAMP, mainActivity);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // onAppStart and onRotate
        Log.d("settingsFragment", "Activity view created");
        return inflater.inflate(R.layout.settings_fragment, container, false);
    }
}
