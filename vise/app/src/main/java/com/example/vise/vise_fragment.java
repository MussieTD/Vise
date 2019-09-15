package com.example.vise;


import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Mussie on 12/2/2017.
 */

public class vise_fragment extends Fragment {


    public static MbtaScavenger newInstance() {
        return new MbtaScavenger();
    }
    private BluetoothAdapter BTAdapter = BluetoothAdapter.getDefaultAdapter();
    public Button backBtn;
    public TextView rssi_msg;
    private ArrayList<BluetoothDevice> ble_devices= null;
    private static final String ble_beacon = "playbulb40";
    private static Map<String, Double> beacon_distances = new HashMap<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        getActivity().registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));

//        ActivityCompat.requestPermissions(getActivity(),
//                new String[]{Manifest.permission.BLUETOOTH},
//                1);
        ActivityCompat.requestPermissions(getActivity(),
                new String[]{Manifest.permission.BLUETOOTH_ADMIN},
                1);

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.BLUETOOTH)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            Log.i(null,"BT not granted ");
            Toast.makeText(getContext(), "BT not granted ", Toast.LENGTH_SHORT).show();
        }

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.BLUETOOTH_ADMIN)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            Log.i(null,"BT ADmin not granted ");
            Toast.makeText(getContext(), "BT Admin not granted ", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.result_page, container, false);

        backBtn = v.findViewById(R.id.restate_btn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // fragment for map
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                MbtaScavenger llf = new MbtaScavenger();
                ft.replace(R.id.fragment_container, llf);
                ft.commit();
            }
        });

        rssi_msg = (TextView) v.findViewById(R.id.instructions_tv);
        Button find_path_btn = (Button) v.findViewById(R.id.restate_btn);
        find_path_btn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                BTAdapter.startDiscovery();
            }
        });

        return v;
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        getActivity().unregisterReceiver(receiver);

    }
    private final BroadcastReceiver receiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            String name = "a";
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                double txPower = -69; // given by our devices??
                double rssi_distance = Math.pow(10d, ((double) txPower - rssi) / (10 * 2));
                name = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
                Parcelable[] uuid = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID);
                if (name != null && (name.contains(ble_beacon))) {
                    Toast.makeText(context, "Action Found " + name, Toast.LENGTH_SHORT).show();
                    beacon_distances.put(name,rssi_distance);
                    rssi_distance = 0.0;
                    name = "";
                    rssi_msg.setText(rssi_msg.getText() + name + " => " + rssi + " dBm "+ rssi_distance +"\n");
                }


            }
            else{
                Log.i(null,"action not found: " + action);
                Toast.makeText(context, "Action Not Found: " + name , Toast.LENGTH_SHORT).show();

            }
            if (beacon_distances.size() >= 1){
                // get_request
                Log.i(null, PreferenceManager.getDefaultSharedPreferences(context)
                        .getString("GROCERY_LIST", ""));
                String url = "http://608dev.net/sandbox/sc/abah/getDirections.py";

                // Instantiate the RequestQueue.
                RequestQueue queue = Volley.newRequestQueue(getContext());
                // Request a string response from the provided URL.
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        new com.android.volley.Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                // Display the first 500 characters of the response string.
                                rssi_msg.setText("Response is: "+ response);
                                Log.i(null, "good response");

                            }
                        }, new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        rssi_msg.setText("That didn't work!");
                        Log.i(null, "good response");

                    }
                });

// Add the request to the RequestQueue.
                queue.add(stringRequest);
            }

        }
    };


}
