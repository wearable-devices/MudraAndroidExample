package il.co.wearabledevices.example.home;

import android.content.Intent;
import android.content.res.Resources;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import MudraAndroidSDK.enums.ConnectionState;
import MudraAndroidSDK.enums.GestureType;
import MudraAndroidSDK.interfaces.callback.OnAirMousePositionChanged;
import MudraAndroidSDK.interfaces.callback.OnBatteryLevelChanged;
import MudraAndroidSDK.interfaces.callback.OnGestureReady;
import MudraAndroidSDK.interfaces.callback.OnImuQuaternionReady;
import MudraAndroidSDK.interfaces.callback.OnPressureReady;
import MudraAndroidSDK.model.Mudra;
import MudraAndroidSDK.model.MudraDevice;
import il.co.wearabledevices.example.Constants;
import il.co.wearabledevices.example.R;
import il.co.wearabledevices.example.calibration.CalibrationActivity;
import model.OpenGLRenderer;

public class MainActivity extends AppCompatActivity
{
    private MudraDevice mudraDevice;
    private GLSurfaceView surfaceView;
    private OpenGLRenderer glRenderer;
    private int screenWidth;
    private int screenHeight;
    private float airMousePosX;
    private float airMousePosY;

    private TextView batteryTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initViews();
        connect();
    }

    private void initViews()
    {
        initAirMouse();
        initCube();

        batteryTextView = findViewById(R.id.textView_battery_main);
        batteryTextView.setText(getString(R.string.battery_main, 0));
        findViewById(R.id.button_main_calibrate).setOnClickListener(this::navigateNext);
    }

    private void navigateNext(View v)
    {
        Intent intent = new Intent(this, CalibrationActivity.class);
        startActivity(intent);
    }

    private void initAirMouse()
    {
        screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
        airMousePosX = screenWidth / 2.0f;
        airMousePosY = screenHeight / 2.0f;
    }

    private void initCube()
    {
        surfaceView = (GLSurfaceView) findViewById(R.id.cubeLayout_main);
        surfaceView.setEGLContextClientVersion(1);
        glRenderer = new OpenGLRenderer();
        surfaceView.setRenderer(glRenderer);
        surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    private void connect()
    {
        ArrayList<MudraDevice> mudraDevices = Mudra.getInstance().getBondedDevices();
        if (mudraDevices.size() < 1)
            return;

        mudraDevice = mudraDevices.get(0);

        TextView address = findViewById(R.id.textView_addressTitle_main);
        address.setText(getString(R.string.address_main, mudraDevice.getBleAddress()));

        if (!mudraDevice.isConnected())
            mudraDevice.connectDevice(this);
        mudraDevice.getConnectionState().observe(this, this::OnStatusUpdate);
    }

    private void startConnection()
    {
        mudraDevice.setOnGestureReady(getOnGestureReady());
        mudraDevice.setOnPressureReady(getOnFingertipPressureReady());

        mudraDevice.setOnAirMousePositionChanged(getOnAirMousePositionChanged());
        mudraDevice.setOnImuQuaternionReady(getOnImuQuaternionReady());
        mudraDevice.setOnBatteryLevelChanged(getOnBatteryLevelChanged());

        batteryTextView.setText(getString(R.string.battery_main, mudraDevice.getBatteryLevel()));
    }

    private OnBatteryLevelChanged getOnBatteryLevelChanged()
    {
        return level ->
                runOnUiThread(new Thread(() -> batteryTextView.setText(getString(R.string.battery_main, mudraDevice.getBatteryLevel()))));
    }

    private void OnStatusUpdate(ConnectionState connectionState)
    {
        TextView deviceNameTextView = findViewById(R.id.textView_status_main);
        deviceNameTextView.setText(getString(R.string.status_main, connectionState.name()));

        if (connectionState == ConnectionState.Connected)
            startConnection();
    }

    private OnImuQuaternionReady getOnImuQuaternionReady()
    {
        return (timestamp, data) ->
        {
            glRenderer.setRotationQuaternion(data);
            surfaceView.requestRender();
        };
    }

    private OnAirMousePositionChanged getOnAirMousePositionChanged()
    {
        FloatingActionButton floatingActionButton = findViewById(R.id.fab_main);

        return data ->
        {
            airMousePosX += data[0] * screenWidth * Constants.HSPEED;
            airMousePosY += data[1] * screenHeight * Constants.VSPEED;
            airMousePosX = airMousePosX <= 0 ? 0 : Math.min(airMousePosX, screenWidth);
            airMousePosY = airMousePosY <= 0 ? 0 : Math.min(airMousePosY, screenHeight);

            runOnUiThread(new Thread(() ->
            {
                floatingActionButton.setX(airMousePosX);
                floatingActionButton.setY(airMousePosY);
            }));
        };
    }

    private OnPressureReady getOnFingertipPressureReady()
    {
        ProgressBar pressureBar = findViewById(R.id.pressureBar_main);

        return proportional -> runOnUiThread(() -> pressureBar.setProgress((int) (proportional * 1000)));
    }

    private OnGestureReady getOnGestureReady()
    {
        ImageView tapImageView = findViewById(R.id.imageView_tap_main);
        ImageView indexImageView = findViewById(R.id.imageView_index_main);
        ImageView thumbImageView = findViewById(R.id.imageView_thumb_main);

        return gestureType ->
        {
            int tapImageId = gestureType == GestureType.Mid_Tap ? Constants.MIDDLE_TAP_ICON_BLUE : Constants.MIDDLE_TAP_ICON_GREY;
            int indexImageId = gestureType == GestureType.Index_Tap ? Constants.INDEX_ICON_BLUE : Constants.INDEX_ICON_GREY;
            int thumbImageId = gestureType == GestureType.Thumb ? Constants.THUMB_ICON_BLUE : Constants.THUMB_ICON_GREY;

            tapImageView.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), tapImageId));
            indexImageView.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), indexImageId));
            thumbImageView.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), thumbImageId));
        };
    }
}
