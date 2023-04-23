package com.keimoger.hackerembassy.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.keimoger.hackerembassy.R;
import com.keimoger.hackerembassy.fragment.base.BaseFragment;
import com.keimoger.hackerembassy.util.PreferencesUtil;
import com.keimoger.hackerembassy.viewmodel.DoorlockViewModel;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

public class DoorlockFragment extends BaseFragment {
    private DoorlockViewModel mDoorlockViewModel;
    private TextView mConnectionStatusTextView;

    private MaterialButton mOpenDoorButton;

    private boolean retrying = false;

    public static Fragment newInstance() {
        return new DoorlockFragment();
    }

    @Override
    protected int layoutId() {
        return R.layout.fragment_doorlock;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(16);

        mDoorlockViewModel = new ViewModelProvider(requireActivity()).get(DoorlockViewModel.class);
        mConnectionStatusTextView = findViewById(R.id.connection_status_text);

        mDoorlockViewModel.getClientReady().setValue(null);

        mOpenDoorButton = findViewById(R.id.open_door_button);
        setOpenDoorButtonMode(OpenDoorButtonMode.DISABLED_READY_LOADING, mOpenDoorButton);

        PreferencesUtil preferencesUtil = new PreferencesUtil(requireContext());

        mDoorlockViewModel.setupMQTT(preferencesUtil.getUsername(), preferencesUtil.getPassword(), preferencesUtil.getHost());
        mConnectionStatusTextView.setText(R.string.doorlock_connection_status_connecting);
        mConnectionStatusTextView.setTextColor(getResources().getColor(R.color.gray, requireContext().getTheme()));
        mConnectionStatusTextView.setVisibility(View.VISIBLE);

        AlertDialog responseErrorDialog = new AlertDialog.Builder(requireContext())
                .setPositiveButton("OK", (dialogInterface, id) -> {
                    dialogInterface.dismiss();
                    mOpenDoorButton.setEnabled(true);
                })
                .setTitle(R.string.login_alert_error_title)
                .setCancelable(true)
                .setIcon(R.drawable.ic_round_error_outline_24)
                .setMessage(R.string.alert_no_lock_response)
                .create();

        mDoorlockViewModel.getClientReady().observe(requireActivity(), ready -> {
            if (ready != null && ready) {
                mOpenDoorButton.setOnClickListener(v -> {
                    mDoorlockViewModel.openDoor();

                    /*if (retrying) setOpenDoorButtonMode(OpenDoorButtonMode.DISABLED_READY_LOADING, mOpenDoorButton);
                    else setOpenDoorButtonMode(OpenDoorButtonMode.DISABLED_RETRY, mOpenDoorButton);*/

                    Future doorOpeningErrorFuture = executorService.schedule(() -> {
                        retrying = true;
                        setOpenDoorButtonMode(OpenDoorButtonMode.ENABLED_RETRY, mOpenDoorButton);
                        setStatusTextViewMode(TextViewMode.CONNECTED_NO_RESPONSE, mConnectionStatusTextView);
                        requireActivity().runOnUiThread(responseErrorDialog::show);
                    }, 1, java.util.concurrent.TimeUnit.SECONDS);
                    mDoorlockViewModel.getDoorOpenedData().observe(requireActivity(), opened -> {
                        if (opened != null && opened) {
                            doorOpeningErrorFuture.cancel(true);
                            //mDoorlockViewModel.getDoorOpenedData().postValue(null);
                            setOpenDoorButtonMode(OpenDoorButtonMode.ENABLED_READY, mOpenDoorButton);
                        }
                    });
                });

                setOpenDoorButtonMode(OpenDoorButtonMode.ENABLED_READY, mOpenDoorButton);
                setStatusTextViewMode(TextViewMode.CONNECTED_READY, mConnectionStatusTextView);
            } else if (ready != null) {
                mOpenDoorButton.setOnClickListener(v -> {
                    mDoorlockViewModel.setupMQTT(preferencesUtil.getUsername(), preferencesUtil.getPassword(), preferencesUtil.getHost());
                });

                setOpenDoorButtonMode(OpenDoorButtonMode.ENABLED_RETRY, mOpenDoorButton);
                setStatusTextViewMode(TextViewMode.NOT_CONNECTED, mConnectionStatusTextView);
            }
        });
        mDoorlockViewModel.getMQTTConnectionError().observe(requireActivity(), error -> {
            AlertDialog dialog = new AlertDialog.Builder(requireContext())
                    .setPositiveButton("OK", (dialogInterface, id) -> dialogInterface.dismiss())
                    .setTitle(R.string.login_alert_error_title)
                    .setCancelable(true)
                    .setIcon(R.drawable.ic_round_error_outline_24)
                    .setMessage(mDoorlockViewModel.getMQTTConnectionError().getValue())
                    .create();
            dialog.show();
            setOpenDoorButtonMode(OpenDoorButtonMode.ENABLED_RETRY, mOpenDoorButton);
        });
    }

    private void setOpenDoorButtonMode(OpenDoorButtonMode mode, MaterialButton button) {
        requireActivity().runOnUiThread(() -> {
                    switch (mode) {
                        case ENABLED_READY:
                            button.setText(R.string.open_door_button_text);
                            button.setEnabled(true);
                            button.setBackgroundColor(getResources().getColor(R.color.red_200, requireContext().getTheme()));
                            break;
                        case ENABLED_RETRY:
                            button.setText(R.string.retry_connection_button_text);
                            button.setEnabled(true);
                            button.setBackgroundColor(getResources().getColor(R.color.yellow, requireContext().getTheme()));
                            break;
                        case DISABLED_READY_LOADING:
                            button.setText(R.string.open_door_button_text);
                            button.setEnabled(false);
                            button.setBackgroundColor(getResources().getColor(R.color.gray, requireContext().getTheme()));
                            break;
                        case DISABLED_RETRY_LOADING:
                            button.setText(R.string.retry_connection_button_text);
                            button.setEnabled(false);
                            button.setBackgroundColor(getResources().getColor(R.color.gray, requireContext().getTheme()));
                            break;
                    }
                }
        );
    }

    private void setStatusTextViewMode(TextViewMode mode, TextView textView) {
        requireActivity().runOnUiThread(() -> {
                    switch (mode) {
                        case CONNECTED_READY:
                            textView.setVisibility(View.VISIBLE);
                            textView.setText(R.string.connection_status_text_connected);
                            textView.setTextColor(getResources().getColor(R.color.success, requireContext().getTheme()));
                            break;
                        case CONNECTED_NO_RESPONSE:
                            textView.setVisibility(View.VISIBLE);
                            textView.setText(R.string.connection_status_text_connected_no_response);
                            textView.setTextColor(getResources().getColor(R.color.yellow, requireContext().getTheme()));
                            break;
                        case NOT_CONNECTED:
                            textView.setVisibility(View.VISIBLE);
                            textView.setText(R.string.connection_status_text_not_connected);
                            textView.setTextColor(getResources().getColor(R.color.red_200, requireContext().getTheme()));
                            break;
                    }
                }
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mDoorlockViewModel.closeConnection();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden)
            mDoorlockViewModel.getClientReady().postValue(false);
    }

    private enum OpenDoorButtonMode {
        ENABLED_READY,
        ENABLED_RETRY,
        DISABLED_READY_LOADING,
        DISABLED_RETRY_LOADING
    }

    private enum TextViewMode {
        CONNECTED_READY,
        CONNECTED_NO_RESPONSE,
        NOT_CONNECTED
    }
}
