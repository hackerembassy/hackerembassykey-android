package com.keimoger.hackerembassy.util.fragment;

import androidx.fragment.app.Fragment;

import com.keimoger.hackerembassy.fragment.DoorlockFragment;
import com.keimoger.hackerembassy.fragment.SettingsFragment;

public class MyFragmentBuilder implements FragmentFactory {
    @Override
    public Fragment getFragment(String tag) {
        switch (tag) {
            case "doorlock":
                return DoorlockFragment.newInstance();
            case "settings":
                return SettingsFragment.newInstance();
            /*case "info":
                return InfoFragment.newInstance();*/
            default:
                return null;
        }

    }
}
