package com.iljak.android_bluetooth_serial.activity.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.iljak.android_bluetooth_serial.R;
import com.iljak.android_bluetooth_serial.model.BtDeviceDataModel;

import java.util.ArrayList;

public class BtDeviceListAdapter extends ArrayAdapter<BtDeviceDataModel> {

    private ArrayList<BtDeviceDataModel> dataSet;
    private Context mContext;

    public BtDeviceListAdapter(Context context, ArrayList<BtDeviceDataModel> data) {
        super(context, 0, data);
        this.dataSet = data;
        this.mContext=context;
    }

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null) {
            listItem = LayoutInflater.from(mContext).inflate(R.layout.item_bluetooth, parent, false);
        }

        BtDeviceDataModel currentItem = dataSet.get(position);

        TextView nameLabel = (TextView) listItem.findViewById(R.id.statLabel);
        if (currentItem.getName() == null || currentItem.getName().equals("")) {
            nameLabel.setText("<unnamed>");
        } else {
            nameLabel.setText(currentItem.getName());
        }

        TextView addresLabel = (TextView) listItem.findViewById(R.id.statValueLabel);
        addresLabel.setText(currentItem.getAddress());

        return listItem;
    }

    public BtDeviceDataModel getDataModel(int position)
    {
        return dataSet.get(position);
    }

    public BtDeviceDataModel getDataModel(String address)
    {
        for (BtDeviceDataModel model : dataSet) {
            if (model.getAddress().equals(address)) {
                return model;
            }
        }
        return null;
    }
}
