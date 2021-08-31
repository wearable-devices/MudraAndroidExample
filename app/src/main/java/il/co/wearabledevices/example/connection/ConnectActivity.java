package il.co.wearabledevices.example.connection;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import MudraAndroidSDK.model.Mudra;
import MudraAndroidSDK.model.MudraDevice;
import il.co.wearabledevices.example.R;
import il.co.wearabledevices.example.home.MainActivity;
import no.nordicsemi.android.ble.observer.BondingObserver;

public class ConnectActivity extends AppCompatActivity
{
    private ScannedDeviceAdapter scannedDeviceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        startAdapter();
        scanForDevices();
    }

    /**
     * Start the mudraDevices adapter.
     */
    private void startAdapter()
    {
        RecyclerView recyclerView = findViewById(R.id.recyclerView_connection_devices);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        scannedDeviceAdapter = new ScannedDeviceAdapter()
        {
            @Override
            public View.OnClickListener onClickListener(MudraDevice mudraDevice)
            {
                return v -> connectDevice(mudraDevice);
            }
        };
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

        mudraDevice.connectDevice(this);
        mudraDevice.setOnBondingChange(new BondingObserver()
        {
            @Override
            public void onBondingRequired(@NonNull BluetoothDevice device)
            {
                connection.setText(R.string.connecting_connect);
                connection.setVisibility(View.VISIBLE);
                connection.setTextColor(Color.BLACK);
            }

            @Override
            public void onBonded(@NonNull BluetoothDevice device)
            {
                navigateNext();
                Mudra.getInstance().stopScan();
            }

            @Override
            public void onBondingFailed(@NonNull BluetoothDevice device)
            {
                connection.setText(R.string.failed_connect);
                connection.setTextColor(Color.RED);
            }
        });
    }

    /**
     * Scan for new devices.
     */
    private void scanForDevices()
    {
        ArrayList<MudraDevice> bondedDevices = Mudra.getInstance().getBondedDevices();
        if (bondedDevices.size() > 0)
            navigateNext();
        Mudra.getInstance().requestAccessPermissions(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (Mudra.getInstance().isBluetoothOn())
            Mudra.getInstance().scan(mudraDevices -> scannedDeviceAdapter.updateList(mudraDevices));
    }

    private void navigateNext()
    {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}