package com.example.bluethoothtest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_ACCESS_COARSE_LOCATION  =1 ;
    public static final int REQUEST_ENABLE_BLE  =11 ;
    private ListView deviceliste;
    private Button btnscan ;
    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> listAdapter ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        deviceliste = findViewById(R.id.liste);
        btnscan  = findViewById(R.id.scann);

        listAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
        deviceliste.setAdapter(listAdapter);



    }

    @Override
    protected void onResume() {
        super.onResume();
        checkBluetoothState();


        registerReceiver(devicesFoundReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        registerReceiver(devicesFoundReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
        registerReceiver(devicesFoundReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));


        btnscan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bluetoothAdapter!=null && bluetoothAdapter.isEnabled()) {
                    if(checkCoarseLocationPermission()){
                        listAdapter.clear();
                        bluetoothAdapter.startDiscovery();
                        checkBluetoothState();
                    }
                }else {
                    checkBluetoothState();
                }
            }
        });


    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(devicesFoundReceiver);
    }

    private boolean checkCoarseLocationPermission() {

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_ACCESS_COARSE_LOCATION);
            return false ;
        }else{
            return true;
        }
    }

    private void checkBluetoothState() {
        if(bluetoothAdapter ==null) {
            Toast.makeText(this, "Ble is not supported on your device ", Toast.LENGTH_SHORT).show();
        }else {
            if(bluetoothAdapter.isEnabled()) {
                if(bluetoothAdapter.isDiscovering()){
                    Toast.makeText(this, "Device discovering process...", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(this, "Ble is ennabled", Toast.LENGTH_SHORT).show();
                    btnscan.setEnabled(true);
                }
            } else {
                Toast.makeText(this, "You need to enable Bluetooth", Toast.LENGTH_SHORT).show();
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent,REQUEST_ENABLE_BLE);
                if(bluetoothAdapter.isDiscovering()){
                    Toast.makeText(this, "Device discovering process...", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(this, "Ble is ennabled", Toast.LENGTH_SHORT).show();
                    btnscan.setEnabled(true);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_ENABLE_BLE) {
            checkBluetoothState();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case REQUEST_ACCESS_COARSE_LOCATION :
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Access coarse location allowed", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Access coarse location forbidden", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private final BroadcastReceiver devicesFoundReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d("dddd",device.getName()+"");
                listAdapter.add(device.getName()+ "\n" +device.getAddress() );
            }else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                btnscan.setText("Scanning Bluethooth Devices");
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
                btnscan.setText("Scanning in progress ....");
            }
        }
    };
}