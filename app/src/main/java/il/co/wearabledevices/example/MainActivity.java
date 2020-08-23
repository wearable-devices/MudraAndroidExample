package il.co.wearabledevices.example;

import MudraAndroidSDK.GestureType;
import MudraAndroidSDK.Mudra;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import model.OpenGLRenderer;

import android.content.Context;
import android.content.res.Resources;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


import static il.co.wearabledevices.example.Constants.*;

public class MainActivity extends AppCompatActivity {

    private Mudra mMudra;
    private ImageView mRecognizeTapImageView, mRecognizeIndexImageView, mRecognizeThumbImageView;
    private GLSurfaceView mIMUsurface;
    private OpenGLRenderer mGLRenderer;
    private TextView deviceNameTextView;
    private ProgressBar mPressureBar;
    private int mScreenWidth, mScreenHeight;
    private float mAirMousePosX, mAirMousePosY;
    private FloatingActionButton mFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestPermissions();
        initViews();
        initializeMudraConnection();
    }

    private void initViews(){
        bindViews();
        initAirmouse();
        initCube();
    }

    private void bindViews(){
        mRecognizeTapImageView = findViewById(R.id.recognizeTap_imageView);
        mRecognizeIndexImageView = findViewById(R.id.recognizeIndex_imageView);
        mRecognizeThumbImageView = findViewById(R.id.recognizeThumb_imageView);
        mFab = findViewById(R.id.fab);
        mPressureBar = findViewById(R.id.pressureBar);
        deviceNameTextView = findViewById(R.id.txtDevicesNumber);
    }

    private void initCube() {
        ConstraintLayout mCubeLayout = findViewById(R.id.cubeLayout);
        if (mIMUsurface == null) {
            mIMUsurface = new GLSurfaceView(this);
            mIMUsurface.setEGLContextClientVersion(1);
            mGLRenderer = new OpenGLRenderer();
            mIMUsurface.setRenderer(mGLRenderer);
            mIMUsurface.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
            mCubeLayout.addView(mIMUsurface);
        }
    }

    private void initAirmouse(){
        mScreenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        mScreenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
        mAirMousePosX = mScreenWidth / 2.0f;
        mAirMousePosY = mScreenHeight / 2.0f;
    }

    private void initializeMudraConnection() {
        mMudra =  Mudra.autoConnectPaired(this);
            if (mMudra !=null) {
                mMudra.setOnGestureReady(onGestureReady);
                mMudra.setOnFingertipPressureReady(onFingertipPressureReady);
                mMudra.setOnAirMousePositionChanged(OnAirMousePositionChanged);
                mMudra.setOnImuQuaternionReady(onImuQuaternionReady);
                mMudra.setOnDeviceStatusChanged(onDeviceStatusChanged);
                mMudra.setOnBatteryLevelChanged(onBatteryLevelChanged);
            }
    }

    private void requestPermissions(){
        Mudra.requestAccessLocationPermissions(this);
        // Required permission for Mudra - note we do not access any of your files/locationj!
        // Location is required for bluetooth,
        // Storage is required for reading gesture calibration file saved during callibration
        requestPermissions(new String[]{"android.permission.ACCESS_FINE_LOCATION",
                        "android.permission.ACCESS_COARSE_LOCATION",
                        "android.permission.READ_EXTERNAL_STORAGE",
                        "android.permission.WRITE_EXTERNAL_STORAGE"},
                1);
    }


    //Mudra callbacks

    Mudra.OnImuQuaternionReady onImuQuaternionReady = new Mudra.OnImuQuaternionReady() {
        @Override
        public void run(long l, float[] floats) {
            mGLRenderer.setRotationQuaternion(floats);
            mIMUsurface.requestRender();
        }
    };

    Mudra.OnGestureReady onGestureReady = new Mudra.OnGestureReady() {
        @Override
        public void run(final GestureType gestureType) {
            Context context = getApplicationContext();
            switch (gestureType) {
                case Tap:
                    mRecognizeTapImageView.setImageDrawable(ContextCompat.getDrawable(context, MIDDLE_TAP_ICON_BLUE));
                    mRecognizeIndexImageView.setImageDrawable(ContextCompat.getDrawable(context, INDEX_ICON_GREY));
                    mRecognizeThumbImageView.setImageDrawable(ContextCompat.getDrawable(context, THUMB_ICON_GREY));
                    break;
                case Index:
                    mRecognizeTapImageView.setImageDrawable(ContextCompat.getDrawable(context, MIDDLE_TAP_ICON_GREY));
                    mRecognizeIndexImageView.setImageDrawable(ContextCompat.getDrawable(context, INDEX_ICON_BLUE));
                    mRecognizeThumbImageView.setImageDrawable(ContextCompat.getDrawable(context, THUMB_ICON_GREY));
                    break;
                case Thumb:
                    mRecognizeTapImageView.setImageDrawable(ContextCompat.getDrawable(context, MIDDLE_TAP_ICON_GREY));
                    mRecognizeIndexImageView.setImageDrawable(ContextCompat.getDrawable(context, INDEX_ICON_GREY));
                    mRecognizeThumbImageView.setImageDrawable(ContextCompat.getDrawable(context, THUMB_ICON_BLUE));
                    break;
            }
        }

    };

    Mudra.OnFingertipPressureReady onFingertipPressureReady = v -> runOnUiThread(() -> mPressureBar.setProgress((int) (v * 1000)));

    Mudra.OnAirMousePositionChanged OnAirMousePositionChanged = new Mudra.OnAirMousePositionChanged() {
        @Override
        public void run(float[] floats) {

            mAirMousePosX += floats[0] * mScreenWidth * HSPEED;
            mAirMousePosY += floats[1] * mScreenHeight * VSPEED;
            mAirMousePosX = mAirMousePosX <= 0 ? 0 : Math.min(mAirMousePosX, mScreenWidth);
            mAirMousePosY = mAirMousePosY <= 0 ? 0 : Math.min(mAirMousePosY, mScreenHeight);

            runOnUiThread(new Thread(() -> {
                mFab.setX(mAirMousePosX);
                mFab.setY(mAirMousePosY);
            }));
        }
    };


    Mudra.OnDeviceStatusChanged onDeviceStatusChanged = new Mudra.OnDeviceStatusChanged() {
        @Override
        public void run(boolean b) {
            if(b)
            {
                runOnUiThread(new Thread(() -> {
                    deviceNameTextView.setText(mMudra.getBluetoothDevice().getAddress());
                }));
            }
        }
    };

    Mudra.OnBatteryLevelChanged onBatteryLevelChanged = new Mudra.OnBatteryLevelChanged() {
        @Override
        public void run(int i) {
            runOnUiThread(new Thread(() -> {
                TextView txtBattery=findViewById(R.id.txtBatLevel);
                String batteryLevel = mMudra.getBatteryLevel() +"%";
                txtBattery.setText(batteryLevel);
            }));
        }
    };



}
