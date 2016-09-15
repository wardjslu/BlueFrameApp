package rydberg.blueframe.display_activities;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.UUID;

import rydberg.blueframe.R;
import rydberg.blueframe.connection.ConnectThread;
import rydberg.blueframe.connection.ConnectedThread;
import rydberg.blueframe.util.NavigationUtil;

/**
 * Created by rydberg on 7/5/2016.
 */
public class TerminalActivity extends AppCompatActivity {



    private static final String UUID_STRING = "00001101-0000-1000-8000-00805F9B34FB"; //Standard SerialPortService ID
    private BluetoothDevice bluetoothDevice;
    private boolean pause;
    private BluetoothAdapter blueToothAdapter;
    private ConnectedThread connectedThread;
    private ConnectThread connectThread;
    private File fog;
    private BufferedWriter out;

    ArrayAdapter<String> m_adapter;
    ArrayList<String> nums = new ArrayList<String>();

    ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terminal);

        lv = (ListView) findViewById(R.id.listView);
        m_adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, nums);

        bluetoothDevice = getIntent().getParcelableExtra(NavigationUtil.BLUETOOTH_DEVICE);
        blueToothAdapter = BluetoothAdapter.getDefaultAdapter();

        int permissioncheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(permissioncheck != PackageManager.PERMISSION_GRANTED){
            AlertDialog.Builder a = new AlertDialog.Builder(this);
            a.setMessage("SORRRYYR YR");
        }
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

                              nums.add(Message);           // update the list view
                              lv.setAdapter(m_adapter);   // with incoming data

                              lv.setSelection(m_adapter.getCount() - 1); // automatically scroll to bottom of list view

                      Log.d("Data: ", Message);

                    // delay if needed in future
                    // try {
                    //     Thread.sleep(10);
                    // } catch(InterruptedException ex) {
                    //     Thread.currentThread().interrupt();
                    // }

              }
          }
      }
    };


    public void excel(View view) {

        Log.i("Status", "STARTING");

        String listString = "";

        for (String s : nums)
        {
            listString += s + "\t";
        }

        if(!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
            Toast.makeText(this, "External SD card not mounted", Toast.LENGTH_LONG).show();
        }

        try{
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR);

            PermissionChecker.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

            int code = 0x11;

            String[] permissions = {"android.permission.WRITE_EXTERNAL_STORAGE"};

            ActivityCompat.requestPermissions(this, permissions, code);

            String h = DateFormat.format("MM-dd-yyyyy-h-mmssaa", System.currentTimeMillis()).toString();

            fog = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), h + ".xls");

            boolean isDirectoryCreated=fog.exists();
            if (!isDirectoryCreated) {
                isDirectoryCreated = fog.createNewFile();
            }

          //  String[] array = listString.split("(?<=\\G.{4})"); splits into equal sized strings if needed in future

            out = new BufferedWriter(new FileWriter(fog.getAbsolutePath()));
            out.write(listString);
            out.close();

            Log.i("STATUS", "FILE GENERATED WITH THE NAME" + h + ".xls");

            AlertDialog.Builder successWrite = new AlertDialog.Builder(this);
            successWrite.setMessage("File callled" + fog + "created in the folder: " + fog.getAbsolutePath());
            successWrite.show();

        } catch (Exception e){
            Log.i("ERROR", e +"");
            AlertDialog.Builder PermDenied = new AlertDialog.Builder(this);
            PermDenied.setMessage("Permission Denied: ");
            PermDenied.show();
        }
    }

    private void connectBT() {
        final Handler handler = new Handler();
        ConnectThread.ConnectListener connectListener = new ConnectThread.ConnectListener() {
            @Override
            public void onConnected(BluetoothSocket socket) {
                ConnectedThread.ConnectedListener connectedListener = new ConnectedThread.ConnectedListener() {
                    @Override
                    public void obtainMessage(final int bytes) {
                        if (!pause) {

                            final String n = Integer.toString(bytes); // list adapter takes Strings

                            handler.post(new Runnable() {
                                public void run() {
                                   // Log.d("Data", bytes + ", " + n + ", ");
                                    if (!pause) {
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void onConnectedFailure(final Exception e) {
                        handler.post(new Runnable() {
                            public void run() {

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

        }
    };

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

    public void toggle(View view) {
        if(!pause){
            pause = true;
        }
        else {
            pause = false;
        }
    }

}
