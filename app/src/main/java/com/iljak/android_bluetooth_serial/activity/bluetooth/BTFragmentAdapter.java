package com.iljak.android_bluetooth_serial.activity.bluetooth;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class BTFragmentAdapter extends FragmentStateAdapter {

    private Fragment activeFragment;
    private FragmentManager fragmentManager;

    public BTFragmentAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
        this.fragmentManager = fragmentManager;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0: return new BTPairedFragment();
            case 1: return new BTScanFragment();
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return 2;
    }

}