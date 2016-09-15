package rydberg.blueframe.display_activities;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.jjoe64.graphview.series.DataPoint;

import java.util.UUID;

import rydberg.blueframe.R;
import rydberg.blueframe.connection.ConnectThread;
import rydberg.blueframe.connection.ConnectedThread;
import rydberg.blueframe.util.NavigationUtil;
import rydberg.blueframe.view.GraphView;
import rydberg.blueframe.view.LoadingLayout;

/**
 * Created by rydberg on 7/5/2016.
 */
public class GraphActivity extends AppCompatActivity {

    private static final String UUID_STRING = "00001101-0000-1000-8000-00805F9B34FB"; //Standard SerialPortService ID
    private double graph2LastXValue = 5d;
    private BluetoothDevice bluetoothDevice;
    private GraphView graph;
    private boolean pause;
    private BluetoothAdapter blueToothAdapter;
    private ConnectedThread connectedThread;
    private ConnectThread connectThread;
    private LoadingLayout loadingLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        graph = (GraphView) findViewById(R.id.graph);
        loadingLayout = (LoadingLayout) findViewById(R.id.loading_layout);

        loadingLayout.setLoadingListener(new LoadingLayout.LoadingListener() {
            @Override
            public void OnRetryPressed() {
                disconnectBT();
                connectBT();
            }
        });

        bluetoothDevice = getIntent().getParcelableExtra(NavigationUtil.BLUETOOTH_DEVICE);
        blueToothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(blueToothDisconnectedReceiver, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver();
        connectBT();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(blueToothDisconnectedReceiver);
        disconnectBT();
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            if (!pause) {

                byte[] readBuf = (byte[]) msg.obj;

                switch (msg.what) {
                    case 1:

                        String Message = new String(readBuf, 0, msg.arg1);

                        loadingLayout.loadingSuccesssfull();

                        if (!pause) {
                            try {
                                // the String to int conversion happens here

                                    int number = Integer.parseInt(Message.trim());
                                if(String.valueOf(number).length() > 3) {
                                    graph2LastXValue += 1d;
                                    DataPoint dataPointA = new DataPoint(graph2LastXValue, number);
                                    graph.appendData(dataPointA);
                                    Log.d("Data: ", number + "");
                                }

                            }
                            catch (NumberFormatException nfe)
                            {
                                break;
                            }

                        }

                        //  Message = Message.substring(begin, end);
                       // Log.d("Data: ", number + ""); // "Data",readMessage + "Message: " + Message + " arg.1: " +
                }
            }
        }
    };

    private void connectBT() {
        loadingLayout.loadingStart();
        final Handler handler = new Handler();
        ConnectThread.ConnectListener connectListener = new ConnectThread.ConnectListener() {
            @Override
            public void onConnected(BluetoothSocket socket) {
                ConnectedThread.ConnectedListener connectedListener = new ConnectedThread.ConnectedListener() {
                    @Override
                    public void obtainMessage(final int bytes) {

                        if (!pause) {


                            handler.post(new Runnable() {
                                public void run() {
                                   // Log.d("Data", bytes + ", " + ", ");
                                    loadingLayout.loadingSuccesssfull();
                                    if (!pause) {
                                        graph2LastXValue += 1d;
                                        DataPoint dataPointA = new DataPoint(graph2LastXValue, bytes);
                                        graph.appendData(dataPointA);
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void onConnectedFailure(final Exception e) {
                        handler.post(new Runnable() {
                            public void run() {
                                loadingLayout.loadingFailed(e.getMessage());
                            }
                        });
                    }
                };
                connectedThread = new ConnectedThread(socket, connectedListener, mHandler);
                connectedThread.start();
            }

            @Override
            public void onConnectFailure(final Exception e) {
                handler.post(new Runnable() {
                    public void run() {
                        loadingLayout.loadingFailed(e.getMessage());
                    }
                });
            }
        };

        UUID uuid = UUID.fromString(UUID_STRING);
        connectThread = new ConnectThread(bluetoothDevice, blueToothAdapter, uuid, connectListener);
        connectThread.start();
    }

    private void disconnectBT() {
        if (connectThread != null)
            connectThread.cancel();
        if (connectedThread != null)
            connectedThread.cancel();
    }

    private final BroadcastReceiver blueToothDisconnectedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadingLayout.loadingFailed(getResources().getString(R.string.error_bluetooth_connection_lost));
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.graph_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.pause:
                pause = !pause;

                if (pause) {
                    item.setIcon(getResources().getDrawable(R.drawable.ic_play_arrow_white_24dp));
                } else {
                    item.setIcon(getResources().getDrawable(R.drawable.ic_pause_white_24dp));
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
