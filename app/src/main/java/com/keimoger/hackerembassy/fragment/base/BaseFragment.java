package com.keimoger.hackerembassy.fragment.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.keimoger.hackerembassy.activity.MainActivity;

public abstract class BaseFragment extends Fragment {

    protected abstract int layoutId();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(layoutId(), container, false);
    }

    protected <T> T findViewById(@IdRes int id) {
        return (T) getView().findViewById(id);
    }

    protected MainActivity requireMainActivity() {
        return (MainActivity) getActivity();
    }
}
