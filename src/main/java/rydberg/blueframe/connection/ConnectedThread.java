package rydberg.blueframe.connection;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by rydberg on 7/5/2016.
 */


public class ConnectedThread extends Thread {


    private final Handler mHandler;
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private final ConnectedListener connectedListener;

    public interface ConnectedListener {
        void obtainMessage(int bytes);

        void onConnectedFailure(Exception e);
    }

    public ConnectedThread(BluetoothSocket socket, ConnectedListener connectedListener, Handler handler) {
        mmSocket = socket;
        this.connectedListener = connectedListener;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        mHandler = handler;

        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            connectedListener.onConnectedFailure(e);
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void run() {

        Log.i("Status", "BEGIN mConnectedThread");

        byte[] readBuffer = new byte[1024];
        int bytes;

        while (true) {
            try {

                if (mmInStream.available() > 0) {

                    bytes = mmInStream.read(readBuffer);

                    // Log.d("Data", bytes + " - ");

                    // connectedListener.obtainMessage(bytes);
                    mHandler.obtainMessage(1, bytes, -1, readBuffer).sendToTarget();
                }

            } catch (IOException e) {
                break;
            }
        }
    }

    public void cancel() {
        try {
            mmOutStream.close();
            mmInStream.close();
            mmSocket.close();
        } catch (IOException e) {
            connectedListener.onConnectedFailure(e);
        }
    }

}
