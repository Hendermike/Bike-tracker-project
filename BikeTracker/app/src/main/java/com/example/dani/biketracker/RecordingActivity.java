package com.example.dani.biketracker;

import android.Manifest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import android.location.Location;

import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;

import android.util.Log;
import android.view.View;

import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

//Control module import
import com.example.dani.biketracker.controlModule.*;

import static java.lang.System.currentTimeMillis;

public class RecordingActivity extends AppCompatActivity implements LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, Observer, SensorEventListener, ServiceConnection, SerialListener {

    public Thread sessionThread;
    //-----------------------------------------------
    // Key DATA
    //-----------------------------------------------
    public static long sendTimestamp;
    public static long receiveTimestamp;

    float desiredLeaderSpeed;
    double desiredSpacing;

    //-----------------------------------------------
    //UI fields
    //----------------------------------------------
    private Button trigger_session;
    private TextView suggestionView, header, state;
    //Compass management
    private ImageView compass;
    private float currentDegree = 0f;
    //private SensorManager mSensorManager;
    //-----------------------------------------------
    //GPS Google API fields
    //-----------------------------------------------
    Location location;
    private GoogleApiClient googleApiClient;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private LocationRequest locationRequest;
    private long UPDATE_INTERVAL = 1000, FASTEST_INTERVAL = 1000; // 3 seconds
    //PERMISSION LIST
    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejacted;
    private ArrayList<String> permissions = new ArrayList<>();
    //Integer permission result request
    private static final int ALL_PERMISSIONS_RESULT = 1011;
    //-----------------------------------------------
    //BLE link management fields
    //-----------------------------------------------
    private enum Connected { False, Pending, True }
    private String deviceAddress = MainActivity.getMAC();
    private String newline = "\r\n";
    private SerialSocket socket;
    private SerialService service;
    private boolean initialStart = true;
    public static Connected connected = Connected.False;
    boolean mBound = false;
    private String msg_received;
    private int bleQueryInterval = 500;
    //-----------------------------------------------
    //Others
    //-----------------------------------------------
    private static String TAG = "DEBUGGING";
    private int contador = 0;
    private boolean sessionState = false;
    RMPC rmpc = new RMPC(5);
    double u_k = 0;

    //State machine
    public boolean newChild;
    public boolean newLocation;
    //-----------------------------------------------
    //Firebase Database fields
    //-----------------------------------------------
    FirebaseDatabase database;
    DatabaseReference downLink;
    //DatabaseReference downLink2;
    DatabaseReference usersRef;
    DatabaseReference userType;
    errorBuffer buffer = new errorBuffer();
    EstAcc accelerationEstimator = new EstAcc();
    Post previousPost = new Post();
    //Post previousPost2 = new Post(); //RTT measure: 3 bike test
    String USER_TYPE = UserConfigActivity.getUserType();
    int counter = 0;

    //-----------------------------------------------
    // Lifecycle methods
    //-----------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording);
        //Keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if(USER_TYPE!="LEADER") {desiredSpacing = FollowerSpacingActivity.getDesiredSpacing();}
        else {desiredLeaderSpeed = LeaderSpeed.getLeaderSpeed();}
        newChild = true;
        //GPS
        //initGPS();
        //UI settings
        initUI();
        newSessionButton();
        //For RT Database management
        database = FirebaseDatabase.getInstance();
        linkSetting();
    }

    @Override
    public void onStart() {
        super.onStart();
        startGPS();
        startBLE();
    }

    @Override
    public void onStop() {
        //BLE
        stopBLE();
        super.onStop();
    }

    public boolean newChild() {
        return this.newChild;
    }

    public void setNewChild(boolean newChild) {
        this.newChild = newChild;
    }

    @Override
    protected void onResume(){
        super.onResume();
        //GPS:
        if (!checkPlayServices()) {
            //locationTv.setText("You need to install Google Play Services to use App properly");
            Toast.makeText(this, "You need to install Google Play Services to use App properly", Toast.LENGTH_SHORT).show();
        }
        //BLE
        resumeBLE();
        Log.d(TAG, "onResume");
        /*if(newLocation() && newChild() && USER_TYPE!="LEADER") {
            MyAcc.updateAcc(location);
            //double time = System.currentTimeMillis();
            suggestionUpdate();
            //time = System.currentTimeMillis() - time;
            toRTFireBase(location.getLatitude(), location.getLongitude(), location.getSpeed(), location.getTime());
            //Wait for new updates
            setNewLocation(false);
            setNewChild(false);
        } else if (newLocation() && USER_TYPE=="LEADER") {
            suggestionUpdate();
            toRTFireBase(location.getLatitude(), location.getLongitude(), location.getSpeed(), location.getTime());
            setNewLocation(false);
        }*/
    }

    @Override
    protected void onPause() {
        super.onPause();
        //GPS: Stop location updates
        pauseGPS();
    }

    @Override
    public void onDestroy() {
        if (connected != Connected.False) {
            disconnect();
        }
        stopService(new Intent(this, SerialService.class));
        try {
            unbindService(this);
        } catch (Exception ignored) {
        } //Added

        //uploadRTTBuffer();

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        send("{0}");
        compass.setImageResource(R.drawable.perfect_compass);
        super.onBackPressed();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {}

    //-----------------------------------------------
    // View methods
    //-----------------------------------------------
    private void initUI() {
        this.header = findViewById(R.id.header);
        this.compass = findViewById(R.id.compass);
    }

    int sessionNumber = 0;
    protected void newSessionButton() {
        this.trigger_session = findViewById(R.id.trigger_session);
        this.trigger_session.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sessionState) {
                    trigger_session.setText(R.string.start_session);
                    compass.setImageResource(R.drawable.inicial_compass);
                    Log.d(TAG, "Fin de la sesión...");
                    sessionThread.interrupt();
                    //send("{0}");
                    sessionState = false;
                } else {
                    sessionNumber = sessionNumber + 1;
                    contador = 0;
                    /*if (USER_TYPE != "LEADER") {
                        childAdded();
                    }*/
                    trigger_session.setText(R.string.finish_session);
                    linkSetting();
                    //RTTPacket2DB("RTT_TEST");
                    sessionState = true;
                    sessionThread = new Thread(new Runnable() {
                        public void run() {
                            Log.d(TAG, "En sesión...");
                            // a potentially time consuming task
                            while(sessionState) {
                                if (newChild) {
                                    RTTPacket2DB("RTT_TEST");
                                    newChild = false;
                                } else {
                                    Log.d(TAG, "No new packets available...");
                                }
                            }
                        }
                    });
                    sessionThread.start();
                    //newChild = true;
                }
            }});
    }

    @Override
    public void update(Observable observable, Object o) {}

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {}

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}

    //-----------------------------------------------
    // GPS methods
    //-----------------------------------------------
    protected void initGPS () {
        //Adding permissions
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissionsToRequest = permissionsToRequest(permissions);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionsToRequest.size() > 0) {
                requestPermissions(permissionsToRequest.
                        toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
            }
        }
        googleApiClient = new GoogleApiClient.Builder(this).
                addApi(LocationServices.API).
                addConnectionCallbacks(this).
                addOnConnectionFailedListener(this).build();

    }

    protected void startGPS() {
        //GPS API connection:
        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    protected void pauseGPS () {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
        }
    }

    protected void startLocationUpdates() {
        Log.d(TAG, "Running startLocationUpdates");
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "You need to enable permissions to display location !", Toast.LENGTH_SHORT).show();
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    public void onLocationChanged(Location location) {
        Log.d(TAG, "Running onLocationChanged");
        if (location != null) {
            // Se actualiza la ubicación con la última adquirida
            this.location = location;
            if(sessionState) {

                // Se determina la aceleración efectuada desde la última iteración
                accelerationEstimator.update(location);
                // Se calcula y envía a la interfaz la acción de control sugerida
                //suggestionUpdate();
                // Se actualiza el estado de la bicicleta en la base de datos
                //toRTFireBase(location.getLatitude(), location.getLongitude(), location.getSpeed(), location.getTime());
                RTTPacket2DB("NORMAL");
            }
        }

    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {}

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.d(TAG, "Running onRequestPermissionsResult");
        switch (requestCode) {
            case ALL_PERMISSIONS_RESULT:
                for (String perm : permissionsToRequest) {
                    if (!hasPermission(perm)) {
                        permissionsRejacted.add(perm);
                    }
                }
                if (permissionsRejacted.size() > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permissionsRejacted.get(0))) {
                            new AlertDialog.Builder(this).
                                    setMessage("These permissions are mandatory to gt your location. You need to allow them.").
                                    setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions(permissionsRejacted.toArray(new String[permissionsRejacted.size()]),
                                                        ALL_PERMISSIONS_RESULT);
                                            }
                                        }
                                    }).
                                    setNegativeButton("Cancel", null).create().show();
                            return;

                        }
                    }
                } else {
                    if (googleApiClient != null) {
                        googleApiClient.connect();
                    }
                }

                break;
        }

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "onConnected");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        // Permissions ok, we get last location
        location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (location != null) {
            //locationTv.setText("Latitude : " + location.getLatitude() + "\nLongitude : " + location.getLongitude());
        }
        startLocationUpdates();
    }

    private ArrayList<String> permissionsToRequest(ArrayList<String> wantedPermissions) {
        ArrayList<String> result = new ArrayList<>();
        for (String perm : wantedPermissions) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }
        return result;
    }

    private boolean hasPermission(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST);
            } else {
                finish();
            }
            return false;
        }
        return true;
    }

    //-----------------------------------------------
    // Firebase methods
    //-----------------------------------------------
    public void toRTFireBase(double LAT, double LON, float speed, long time) {

            DatabaseReference upLink = userType.child(Integer.toString(cuenta()));
            //Data reference
            Post newPost;

            if (USER_TYPE == "LEADER") {
                newPost = new Post(Double.toString(LAT),
                        Double.toString(LON),
                        speed,
                        speed,
                        accelerationEstimator.getEstimation(),
                        time);
            }
            else {
                newPost = new Post(Double.toString(LAT),
                        Double.toString(LON),
                        speed,
                        previousPost.getLeaderSpeed(),
                        accelerationEstimator.getEstimation(),
                        time);
                newPost.setUk(u_k);
                //, Double.toString(currentSpacing), Double.toString(u_k));
            }

            /*if (USER_TYPE != "LEADER") {
                newPost.setSendTime(previousPost.getSendTime()); //RTT measure
                //newPost.setTravelTime(halfRTT);
                //RTT measure
            }
            else {
                newPost.setSendTime(System.currentTimeMillis());
                newPost.setTravelTime(RTT);
            }*/
            newPost.setCurrentSpacing(currentSpacing);
            //newPost.setSendTime(System.currentTimeMillis());
            upLink.setValue(newPost);

            //if(USER_TYPE!="LEADER") {
             //   setNewChild(false);
            //}
    }

    public void RTTPacket2DB(String MODE) {

        DatabaseReference upLink = userType.child(Integer.toString(cuenta()));
        //Data reference
        Post newPost;
        float speed;
        double time;
        String latitude;
        String longitude;
        /* Operation mode setting */
        if (MODE == "RTT_TEST") {
            speed = 99;
            time = 99;
            latitude = Double.toString(99);
            longitude = Double.toString(99);
        } else {
            speed = location.getSpeed();
            time = location.getTime();
            latitude = Double.toString(location.getLatitude());
            longitude = Double.toString(location.getLongitude());
        }

        if (USER_TYPE == "LEADER") {
            newPost = new Post(latitude,
                    longitude,
                    speed,
                    speed,
                    accelerationEstimator.getEstimation(),
                    time);

            newPost.setSendTime(System.currentTimeMillis());
            newPost.setTravelTime(RTT);
        }
        else {
            newPost = new Post(latitude,
                    longitude,
                    speed,
                    previousPost.getLeaderSpeed(),
                    accelerationEstimator.getEstimation(),
                    time);

            newPost.setUk(u_k);
            newPost.setSendTime(previousPost.getSendTime());
        }

        newPost.setCurrentSpacing(currentSpacing);

        upLink.setValue(newPost);
        Log.d(TAG, "Paquete enviado a la base de datos...");
    }

    public void linkSetting() {

        // Referencia a la base de datos
        usersRef = database.getReference();
        // Referencia a la sesión específica
        usersRef = usersRef.child(Integer.toString(sessionNumber));

        // Ejemplo : si soy el seguidor 1, entonces permaneceré atento a las actualizaciones del seguidor 0.
        if (USER_TYPE == "FOLLOWER1") {
            // "UpLink" a la base de datos.
            userType = usersRef.child("FOLLOWER1");
            // "DownLink" desde la base de datos.
            downLink = usersRef.child("FOLLOWER0");
        }
        else if (USER_TYPE == "FOLLOWER0") {
            userType = usersRef.child("FOLLOWER0");
            downLink = usersRef.child("LEADER");
        }
        else if (USER_TYPE == "LEADER") {
            userType = usersRef.child("LEADER");
            // Empleado en la medición del RTT.
            downLink = usersRef.child("FOLLOWER0");
        }

        if (USER_TYPE != "LEADER") {
            // En general, solo los seguidores están atentos a actualizaciones.
            childAdded();
        } else {
            // Empleado en la medición del RTT.
            childAdded();
        }

    }

    long RTT = 99; //RTT measure

    public void childAdded() {
        // Se incorpora el escuchador de eventos.
        downLink.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                previousPost = dataSnapshot.getValue(Post.class);                                   //Se obtiene el último dato de la bicicleta previa
                if (USER_TYPE == "LEADER") {                                                        //Condicional para la estimación del RTT
                    RTT = System.currentTimeMillis() - previousPost.getSendTime();
                }
                newChild = true;
                Log.d(TAG, "Se obtuvo el nuevo dato de la siguiente bicicleta");
                }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {}

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    double currentSpacing = 99;
    public double currentSpacing() {
        Location previousBike = new Location("previousBike");
        previousBike.setLongitude(Double.parseDouble(previousPost.getLon()));
        previousBike.setLatitude(Double.parseDouble(previousPost.getLat()));
        previousBike.setAltitude(location.getAltitude());

        //currentSpacing = Math.sqrt(dLAT + dLON);
        currentSpacing = location.distanceTo(previousBike);
        return currentSpacing;
    }

    public pairStatePackage preparePairStatePackage(Post previousPost) {
        pairStatePackage paquete = new pairStatePackage(0,
                location.getSpeed(),
                currentSpacing(),
                previousPost.userSpeed,
                previousPost.leaderSpeed,
                desiredSpacing);
        return paquete;
    }

    private int cuenta() {
        this.contador += 1;
        return contador;
    }

    public void suggestionUpdate() {

            if (USER_TYPE != "LEADER") {
                AsyncTask.execute(() -> {
                    double currentError = u_k - accelerationEstimator.getEstimation();
                    buffer.update(currentError);
                    pairStatePackage paquete = preparePairStatePackage(previousPost);
                    u_k = rmpc.getControlAction(buffer, paquete);
                });
            }

            //
             if (counter >= 5 || USER_TYPE == "LEADER") {
                bleSend(u_k);
             } else {
                counter += 1;
             }

    }

    double mean_acc = 0.001562809973235024;
    double std_acc = 0.2448071444938314;

    public void bleSend(double u_k) {
        if(USER_TYPE != "LEADER") { //FOLLOWER CASE

            /*if(Math.abs(location.getSpeed() - previousPost.getLeaderSpeed()) < 0.5 && Math.abs(currentSpacing - desiredSpacing) < 0.5) {
                send("{0}");
                compass.setImageResource(R.drawable.inicial_compass);
            } else {
                if(currentSpacing - desiredSpacing > 0) {
                    send("{+}");
                    compass.setImageResource(R.drawable.speed_up_compass);
                } else if (currentSpacing - desiredSpacing < 0) {
                    send("{-}");
                    compass.setImageResource(R.drawable.slow_down_compass);
                } else {
                }*/

                if (u_k > mean_acc + 0.5 * std_acc) {
                    // Notificación a la interfaz háptica
                    send("{+}");
                    // Notificación mediante interfaz visual
                    compass.setImageResource(R.drawable.speed_up_compass);
                } else if (u_k < mean_acc - 0.5 * std_acc) {
                    send("{-}");
                    compass.setImageResource(R.drawable.slow_down_compass);
                } else {
                    send("{0}");
                    compass.setImageResource(R.drawable.inicial_compass);
                }

        } else { //LEADER CASE
            if (Math.abs(location.getSpeed() - desiredLeaderSpeed) > 0.5) {
                if (location.getSpeed() - desiredLeaderSpeed < 0) {
                    send("{+}");
                    compass.setImageResource(R.drawable.speed_up_compass);
                } else if (location.getSpeed() - desiredLeaderSpeed > 0) {
                    send("{-}");
                    compass.setImageResource(R.drawable.slow_down_compass);
                }
            } else {
                send("{0}");
                compass.setImageResource(R.drawable.inicial_compass);
            }
            }
        }

    //-----------------------------------------------
    // BLE methods
    //-----------------------------------------------
    protected void startBLE() {
        //BLE link service bind and start:
        bindService(new Intent(this, SerialService.class), this, Context.BIND_AUTO_CREATE);
        if(service != null){
            service.attach((SerialListener) this);}
        else{
            startService(new Intent(this, SerialService.class)); // prevents service destroy on unbind from recreated activity caused by orientation change
        }
    }

    protected void stopBLE() {
        if(service != null && !this.isChangingConfigurations()) {
            service.detach();
        }
    }

    protected void resumeBLE() {
        if (initialStart && service != null) {
            initialStart = false;
            runOnUiThread(this::connect);
        }
    }

    private void connect() {
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);

            String deviceName = device.getName() != null ? device.getName() : device.getAddress();
            status("connecting...");
            connected = Connected.Pending;

            socket = new SerialSocket();
            service.connect(this, "Connected to " + deviceName);
            socket.connect(this, service, device);
        } catch (Exception e) {
            onSerialConnectError(e);
        }
    }

    private void disconnect() {
        connected = Connected.False;
        service.disconnect();
        //socket.disconnect();
        socket = null;
    }

    private void send(String str) {
        if(connected != Connected.True) {
            Toast.makeText(this, "BLE link not connected", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            //receiveText.append(spn);
            byte[] data = (str + newline).getBytes();
            //Call to actually send data
            socket.write(data);

        } catch (Exception e) {
            onSerialIoError(e);
        }
    }

    private void receive(byte[] data) {
        msg_received = new String(data);
        //Toast.makeText(getApplicationContext(), msg_received, Toast.LENGTH_SHORT).show();
    }

    //TODO: verificar que commnets no crasheen la app
    private void status(String str) {
        //SpannableStringBuilder spn = new SpannableStringBuilder(str+'\n');
        //spn.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorStatusText)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        //receiveText.append(spn);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        service = ((SerialService.SerialBinder) binder).getService();
        if(initialStart) {
            initialStart = false;
            this.runOnUiThread(this::connect);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        service = null;
    }

    @Override
    public void onSerialConnect() {
        status("connected");
        connected = Connected.True;
    }

    @Override
    public void onSerialConnectError(Exception e) {
        status("connection failed: " + e.getMessage());
        disconnect();
    }

    @Override
    public void onSerialRead(byte[] data) {
        receive(data);
    }

    @Override
    public void onSerialIoError(Exception e) {
        status("connection lost: " + e.getMessage());
        disconnect();
    }

}