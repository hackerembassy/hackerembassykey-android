package com.keimoger.hackerembassy.util.fragment;

import androidx.fragment.app.Fragment;

public interface FragmentFactory {
    Fragment getFragment(String tag);
}
