package com.iljak.android_bluetooth_serial.activity.adapter;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.iljak.android_bluetooth_serial.R;
import com.iljak.android_bluetooth_serial.model.ConsoleItemDataModel;

import java.util.ArrayList;

public class ConsoleItemDataAdapter extends ArrayAdapter<ConsoleItemDataModel> {

    private ArrayList<ConsoleItemDataModel> dataSet;
    private Context mContext;

    public ConsoleItemDataAdapter(Context context, ArrayList<ConsoleItemDataModel> data) {
        super(context, 0, data);
        this.dataSet = data;
        this.mContext=context;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null) {
            listItem = LayoutInflater.from(mContext).inflate(R.layout.item_console, parent, false);
        }

        ConsoleItemDataModel currentItem = dataSet.get(position);

        TextView dateLabel = (TextView) listItem.findViewById(R.id.dateLabel);

        String date = currentItem.isIncoming() ? "->" : "<-";
        date += " " + currentItem.getFormattedTimeStamp();

        dateLabel.setText(date);

        TextView messageLabel = (TextView) listItem.findViewById(R.id.messageLabel);
        messageLabel.setText(currentItem.getMessage());

        return listItem;
    }

    public ConsoleItemDataModel getDataModel(int position)
    {
        return dataSet.get(position);
    }
}
