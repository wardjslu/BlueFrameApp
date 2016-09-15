package rydberg.blueframe.util;


import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;

import rydberg.blueframe.display_activities.GraphActivity;
import rydberg.blueframe.display_activities.TerminalActivity;

/**
 * Created by rydberg on 7/5/2016.
 */
public class NavigationUtil {

    public static final String BLUETOOTH_DEVICE = "BLUETOOTH_DEVICE";

    public static void startGraphActivity(Activity activity, BluetoothDevice bluetoothDevice) {
        Intent intent = new Intent(activity, GraphActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(BLUETOOTH_DEVICE, bluetoothDevice);
        intent.putExtras(bundle);
        intent.putExtras(bundle);
        activity.startActivity(intent, bundle);
    }

    public static void startTerminalActivity(Activity activity, BluetoothDevice bluetoothDevice) {
        Intent intent = new Intent(activity, TerminalActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(BLUETOOTH_DEVICE, bluetoothDevice);
        intent.putExtras(bundle);
        intent.putExtras(bundle);
        activity.startActivity(intent, bundle);
    }

}
