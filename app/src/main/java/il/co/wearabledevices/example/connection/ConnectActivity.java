package il.co.wearabledevices.example.connection;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import mudraAndroidSDK.enums.Feature;
import mudraAndroidSDK.model.Mudra;
import mudraAndroidSDK.model.MudraDevice;
import il.co.wearabledevices.example.R;
import il.co.wearabledevices.example.home.MainActivity;

public class ConnectActivity extends AppCompatActivity
{
    private static final long SCAN_DELAY = 5000;
    private ScannedDeviceAdapter scannedDeviceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        startAdapter();
        setMudraEnvironment();
    }

    /**
     * Start the mudraDevices adapter.
     */
    private void startAdapter()
    {
        RecyclerView recyclerView = findViewById(R.id.recyclerView_connection_devices);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        scannedDeviceAdapter = new ScannedDeviceAdapter(this::connectDevice);
        recyclerView.setAdapter(scannedDeviceAdapter);
    }

    /**
     * When clicking a device try to pair and bond the device.
     *
     * @param mudraDevice -
     */
    private void connectDevice(MudraDevice mudraDevice)
    {
        TextView connection = findViewById(R.id.textView_connection_connecting);
        connection.setText(R.string.connecting_connect);
        connection.setVisibility(View.VISIBLE);
        connection.setTextColor(Color.BLACK);

        Mudra.getInstance().stopScan();
        mudraDevice.connectDevice(this);

        mudraDevice.setOnDeviceStatusChanged(this::checkIfDeviceConnected);
    }

    private void checkIfDeviceConnected(boolean isConnected)
    {
        if (isConnected)
        {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }

    /**
     * Request the needed permissions, set the licenses and after the permissions was granted, start the scan for new devices.
     */
    private void setMudraEnvironment()
    {
        Mudra.getInstance().requestAccessPermissions(this);
        Mudra.getInstance().setLicense(Feature.DoubleTap, "LicenseType::DoubleTap");
        Mudra.getInstance().setLicense(Feature.Main, "LicenseType::Main");
    }

    /**
     * Update any bonded devices and start the scan for new devices.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        startScan();
    }

    private void startScan()
    {
        if (Mudra.getInstance().isBluetoothOn(this))
        {
            scannedDeviceAdapter.updateList(Mudra.getInstance().getBondedDevices(this));
            Mudra.getInstance().scan(this, mudraDevices -> scannedDeviceAdapter.updateList(mudraDevices));
        }
        else
        {
            Toast.makeText(this, "Please start bluetooth",Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(this::startScan, SCAN_DELAY);
        }
    }

}