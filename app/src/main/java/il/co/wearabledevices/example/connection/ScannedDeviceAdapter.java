package il.co.wearabledevices.example.connection;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

import MudraAndroidSDK.model.MudraDevice;
import il.co.wearabledevices.example.R;

public class ScannedDeviceAdapter extends RecyclerView.Adapter<ScannedDeviceAdapter.DevicesViewHolder>
{

    private final ArrayList<MudraDevice> devices;

    public static class DevicesViewHolder extends RecyclerView.ViewHolder
    {
        final TextView nameText;
        final TextView shortAddress;
        final ConstraintLayout constraintLayout;

        public DevicesViewHolder(View view)
        {
            super(view);
            constraintLayout = view.findViewById(R.id.constraintLayout_mudraDeviceItem_main);
            nameText = view.findViewById(R.id.textView_mudraDeviceItem_name);
            shortAddress = view.findViewById(R.id.textView_mudraDeviceItem_shortAddress);
        }
    }

    public ScannedDeviceAdapter()
    {
        this.devices = new ArrayList<>();
    }

    public void updateList(ArrayList<MudraDevice> devices)
    {
        int size = this.devices.size();
        if (size < devices.size())
            this.devices.addAll(devices);
        notifyItemRangeChanged(size,this.devices.size());
    }

    @NonNull
    @Override
    public DevicesViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType)
    {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_mudra_device, viewGroup, false);
        return new DevicesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DevicesViewHolder devicesViewHolder, final int position)
    {
        MudraDevice mudraDevice = devices.get(position);
        devicesViewHolder.nameText.setText(mudraDevice.getBluetoothDeviceName());
        devicesViewHolder.shortAddress.setText(mudraDevice.getShortAddress());
        devicesViewHolder.constraintLayout.setOnClickListener(onClickListener(mudraDevice));
    }

    @Override
    public int getItemCount()
    {
        return devices.size();
    }

    public View.OnClickListener onClickListener(MudraDevice mudraDevice)
    {
        return v ->
        {
        };
    }
}