package il.co.wearabledevices.example.calibration;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import MudraAndroidSDK.cloud.model.Calibration;
import MudraAndroidSDK.enums.GestureType;
import MudraAndroidSDK.enums.Quality;
import MudraAndroidSDK.model.Mudra;
import MudraAndroidSDK.model.MudraDevice;
import il.co.wearabledevices.example.R;

public class CalibrationActivity extends AppCompatActivity
{
    private MudraDevice mudraDevice;
    private GestureType gestureType;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);
        updateProgress(0);
        findAndConnect();
        startCalibration();
    }

    private void updateProgress(int progress)
    {
        String str = getString(R.string.progress_calibration, String.valueOf(progress+1), GestureType.values()[progress+1]);
        ((TextView)findViewById(R.id.textView_calibration_progress)).setText(str);
    }

    /**
     * Get mudraDevice and connect if disconnected.
     */
    private void findAndConnect()
    {
        ArrayList<MudraDevice> mudraDevices = Mudra.getInstance().getBondedDevices();
        if (mudraDevices.size() >= 1)
        {
            mudraDevice = mudraDevices.get(0);
            if (!mudraDevice.isConnected())
                mudraDevice.connectDevice(this);
        }
        else
            showError();
    }

    /**
     * Start the calibration with the first gesture: Mid.tap;
     */
    private void startCalibration()
    {
        if (mudraDevice == null)
            return;

        resetCalibration();

        mudraDevice.calibrateGesture(gestureType);
        mudraDevice.setOnCalibrationChange(this::onCalibrationChanged);
        mudraDevice.setOnCalibrationFinished(this, this::onCalibrationFinished);
    }

    private void resetCalibration()
    {
        mudraDevice.exitCalibration();
        mudraDevice.clearCurrentGestureCalibration();
        gestureType = GestureType.Mid_Tap;
    }

    /**
     * Please notice that the result of the calibration is in here.
     *
     * @param calibration - the calibration json file.
     */
    private void onCalibrationFinished(String calibration)
    {
        ((TextView) findViewById(R.id.textView_calibration_status)).setText(R.string.calibration_finished);
        Mudra.getInstance().getUserData().updateCalibration(this, new Calibration(calibration), null);
        mudraDevice.exitCalibration();
    }

    /**
     * Each time the calibration detect a gesture it will use this callback
     *
     * @param gestureType - the gesture we are calibrating
     * @param floats      - the data
     * @param quality     - the quality of the gesture
     * @param v           -
     * @param v1          -
     * @param v2          -
     */
    private void onCalibrationChanged(GestureType gestureType, float[] floats, Quality quality, float v, float v1, float v2)
    {
        String str = getString(R.string.calibration_current, gestureType, String.valueOf(mudraDevice.getCalibrationProgress()), quality);
        ((TextView) findViewById(R.id.textView_calibration_current)).setText(str);
        checkNextCalibration();
    }

    /**
     * Check if the current gesture has finished calibration.
     */
    private void checkNextCalibration()
    {
        if (mudraDevice.getCalibrationProgress() < 1)
            return;

        gestureType = GestureType.values()[gestureType.ordinal() + 1];
        if (gestureType.ordinal() >= GestureType.Twist.ordinal()) //Calibrate only Mid_Tap,Index_Tap,Thumb
            gestureType = GestureType.None; //Finish the calibration

        mudraDevice.calibrateGesture(gestureType);
        updateProgress(gestureType.ordinal());
    }

    private void showError()
    {
        TextView title = findViewById(R.id.textView_calibration_current);
        title.setText(R.string.error_callback);
        title.setTextColor(Color.RED);
        title.setTextSize(30.0f);
    }
}
