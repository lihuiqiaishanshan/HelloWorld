package com.hisilicon.launcher.view.setting;

import android.content.Context;
import android.net.NetworkInfo.DetailedState;

import com.hisilicon.launcher.R;

class Summary {
    static String get(Context context, DetailedState state, boolean isEphemeral) {
        if (state == DetailedState.CONNECTED && isEphemeral) {
            // Special case for connected + ephemeral networks.
            return context.getString(R.string.connected_via_wifi_assistant);
        }

        String[] formats = context.getResources().getStringArray(R.array.wifi_status);
        int index = state.ordinal();

        if (index >= formats.length || formats[index].length() == 0) {
            return "";
        }
        return formats[index];
    }
}
