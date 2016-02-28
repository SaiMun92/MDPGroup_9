package com.example.android.mdpandroid;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.support.v4.app.ActionBarDrawerToggle;
import android.content.SharedPreferences;
import android.support.v4.widget.DrawerLayout;
import android.content.res.Configuration;
import android.app.FragmentManager;
import android.hardware.SensorEvent;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;
import android.widget.RelativeLayout;
import java.util.ArrayList;

public class MainActivity extends Activity implements SensorEventListener {

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    static final int[] arena_maze = new int[300];
    static final int[] MAPPING = new int[300];
    // Debugging
    private static final String BLUETOOTH_CHAT = "BluetoothChat";
    private static final boolean TRUE = true;
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;
    private static final int mRequestCode = 100;
    RelativeLayout layout_joystick;
    JoyStickClass js;
    // Grid initialization
    GridView mapView;
    int robotView[] = new int[9];
    String robotPosition = "forward";
    int[][] map = new int[20][15];
    private String[] drawerListItems;
    private DrawerLayout drawerLayout;
    private ListView drawerListView;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    // Layout Views
    private ListView mConversationView;
    private EditText mOutEditText;
    private Button sendButton;
    private ToggleButton toggleButton;
    private Button updateMap;
    private int interval = 10000;
    private Button exploreButton;
    private Button runshortestButton;
    private TextView robotDirection;
    private ToggleButton tiltToggle;
    private Button f1;
    private Button f2;
    //tilt sensor flag
    private boolean tilt = false;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;
    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;
    private float[] mR = new float[9];
    private float[] mOrientation = new float[3];
    private long lastUpdate;
    //Handler for Maze
    private Handler mazeHandler;
    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Array adapter for the conversation thread
    private ArrayAdapter<String> mConversationArrayAdapter;
    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:              //talks about the state of the bluetooth in the "up" button
                    if (TRUE)
                        Log.i(BLUETOOTH_CHAT, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            setStatus(getString(R.string.title_connected_to,mConnectedDeviceName));
                            mConversationArrayAdapter.clear();

                            break;
                        case BluetoothService.STATE_CONNECTING:
                            setStatus("Connecting...");
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            setStatus("Not connected");
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    mConversationArrayAdapter.add("Me:  " + writeMessage);  //the message that appears on top of the "send" button
                    mOutEditText.setText("");
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;                      //the status of the robotView
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    TextView text = (TextView) findViewById(R.id.tb_status);

                    if (readMessage.startsWith(getString(R.string.start_STATUS))) {
                        text.setText(readMessage);
                    } else if (readMessage.contains(getString(R.string.robot_status))) {
                        text.setText(readMessage.substring(11, readMessage.length() - 2));
                    } else if (readMessage.contains(getString(R.string.map_status))) {
                        String mazeInfo = readMessage.substring(12, readMessage.length() - 2);
                        decodeMapInfo(mazeInfo);
                    } else {
                        mConversationArrayAdapter.add(mConnectedDeviceName + ": " + readMessage);
                        switch (readMessage.charAt(1)) {            //5F, 5L, 5R
                            case 'F':
                                moveForward();
                                break;
                            case 'L':
                                robotPosition("L");
                                break;
                            case 'R':
                                robotPosition("R");
                                break;
                            default:
                                Log.d(BLUETOOTH_CHAT, "normal text");
                        }
                    }

                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    // Local Bluetooth Adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private BluetoothService bTService = null;
    //Auto Updating of Maze info
    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                sendMessage("sendArena");
                mazeHandler.postDelayed(mStatusChecker, interval);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // ----------------------------JoyStick----------------------------------
        layout_joystick = (RelativeLayout) findViewById(R.id.layout_joystick);

        js = new JoyStickClass(getApplicationContext()
                , layout_joystick, R.drawable.image_button);
        js.setStickSize(80, 80);      //red circle size
        js.setLayoutSize(300, 300);     //yellow circle size
        js.setLayoutAlpha(150);          //color intensity
        js.setStickAlpha(100);           //stick color intensity
        js.setOffset(50);                //distance from the edge of the yellow circle baka
        js.setMinimumDistance(80);      // konyoraro bakayaro

        layout_joystick.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                js.drawStick(arg1);
                if (arg1.getAction() == MotionEvent.ACTION_DOWN //do something when pressed
                        || arg1.getAction() == MotionEvent.ACTION_MOVE) {

                    int direction = js.get4Direction();
                    if (direction == JoyStickClass.STICK_UP) {
                        turnLeft();
                    } else if (direction == JoyStickClass.STICK_RIGHT) {
                        moveForward();
                        sendMessage("5F");
                    } else if (direction == JoyStickClass.STICK_DOWN) {
                        turnRight();
                    }
                }
                return true;        //return true when finger is lifted
            }
        });


        // -------------------------------Robot & Maze-----------------------------------

        mapView = (GridView) findViewById(R.id.gridView1);

        robotView[0] = 0;
        robotView[1] = 1;
        robotView[2] = 2;
        robotView[3] = 20;
        robotView[4] = 21;
        robotView[5] = 22;
        robotView[6] = 40;
        robotView[7] = 41;
        robotView[8] = 42;

        int x = 0;
        for (int i = 0; i < arena_maze.length; i++) {//populate the arena_maze array with data.
            if (x < 9 && i == robotView[x]) {                //This is to show the robotView on the grid
                if (x != 5)                         //x=0 i=0 i=robotView[0]=0 arena_maze[0]=1
                    arena_maze[i] = 1;              //x=1 i=1 i=robotView[1]=1 arena_maze[1]=1
                else                                //x=2 i=2 i=robotView[2]=2 arena_maze[2]=1
                    arena_maze[i] = 2;              //x=3 i=3 i!=robotView[3]=20 arena_maze[3]=0
                x++;                                //x=3 i=4 i!=robotView[3]=20 arena_maze[4]=0
            } else {                                 //x=3 i=20 i=robotView[3]=20 arena_maze[20]=1   u get the point.
                arena_maze[i] = 0;                  //0 represents the areas not explored by the robotView.
            }                                       //1 represents the the orange part of the robotView. (the rest)
        }                                           //2 represents the white part, or the front of the robotView.

        mapView.setAdapter(new Maze(this, arena_maze));    //sets a custom adapter in this case the arena_maze to be displayed on the screen.


        int mapVal = 0;
        for (int i = 0; i < 20; i++) {      //represents all 300 squares(0-299)
            for (int j = 0; j < 15; j++) {
                map[i][j] = mapVal;   //i is the row, j is the column
                MAPPING[i + j * 20] = map[i][j];  //MAPPING[20]=1
                mapVal++;
            }
        }

        //Robot head direction
        robotDirection = (TextView) findViewById(R.id.robotHead);  //the space beside the F1 button
        robotDirection.setText("Robot Head: " + robotPosition);         //robotPosition indicates the robotView position

        if (TRUE) Log.e(BLUETOOTH_CHAT, "+++ ON CREATE +++");

        // Get local Bluetooth adapter of the device
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        // If the adapter is null, then Bluetooth is not supported by device
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // --------------------------Navigation Drawer---------------------------------------
        drawerListItems = getResources().getStringArray(R.array.items);
        drawerListView = (ListView) findViewById(R.id.left_drawer); //slide from the left
        drawerListView.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_listview_item, drawerListItems));
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.drawable.ic_drawer,
                R.string.drawer_open,
                R.string.drawer_close
        );

        //  Set actionBarDrawerToggle as the DrawerListener
        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        // just styling only
        drawerListView.setOnItemClickListener(new DrawerItemClickListener());

        //------------------Auto and Manual Map Update---------------------------------------
        mazeHandler = new Handler();        //Handler send and process messages

        toggleButton = (ToggleButton) findViewById(R.id.toggle);        //auto manual toggle button
        updateMap = (Button) findViewById(R.id.btn_update);
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {    //make the update button appear when manual mode
                if (isChecked) {
                    updateMap.setVisibility(View.VISIBLE);
                    stopRepeatingTask();        //manual mode-> stopRepeatingTask();
                } else {
                    updateMap.setVisibility(View.INVISIBLE);        //auto mode
                    startRepeatingTask();
                }

                //SharedPreferences -> interface and modifying preference data
                //SharedPreferences is used to save and retrieve primitive data types: booleans, floats, ints, longs, will persisit even after app is killed
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("toggleButton", toggleButton.isChecked());
                editor.commit();
            }
        });

        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        toggleButton.setChecked(sharedPreferences.getBoolean("toggleButton", false));      //T = toggle button

        updateMap.setOnClickListener(new OnClickListener()      //manual mode when the update button is pressed
        {
            @Override
            public void onClick(View v) {
                sendMessage("sendArena");
            }
        });


        f1 = (Button) findViewById(R.id.F1Button);
        f2 = (Button) findViewById(R.id.F2Button);

        f1.setOnClickListener((new OnClickListener() {
            public void onClick(View v) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                String f1configs = prefs.getString("edittext_preference1", "");     //send the predefined string
                sendMessage(f1configs);
                Log.d("ble", f1configs);
            }
        }));

        f2.setOnClickListener((new OnClickListener() {
            public void onClick(View v) {

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                String f1configs = prefs.getString("edittext_preference2", "");
                sendMessage(f1configs);
                Log.d("ble", f1configs);
            }
        }));


        //tilt Sensor
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        lastUpdate = System.currentTimeMillis();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        actionBarDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        actionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    private void displayView(int position) {

        Fragment fragment = null;

        switch (position) {
            case 0:     //Set Coordinates
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                LinearLayout layout = new LinearLayout(this);
                layout.setOrientation(LinearLayout.HORIZONTAL);
                final EditText xcoordinate = new EditText(this);
                xcoordinate.setHint("xCoordinates");
                layout.addView(xcoordinate);
                final EditText ycoordinate = new EditText(this);
                ycoordinate.setHint("yCoordinates");
                layout.addView(ycoordinate);
                alert.setView(layout);
                alert.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String xcoord = xcoordinate.getText().toString();
                        String ycoord = ycoordinate.getText().toString();
                        String message = "coordinate (" + xcoordinate.getText().toString() + "," + ycoordinate.getText().toString() + ")";
                        sendMessage(message);       //sends the position of the robotView over to the computer
                        setRobotPos(Integer.parseInt(xcoord), Integer.parseInt(ycoord));
                        mOutStringBuffer = new StringBuffer("");
                    }
                });
                alert.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                });
                alert.setIcon(android.R.drawable.ic_dialog_alert);
                alert.show();
                drawerLayout.closeDrawer(Gravity.LEFT);
                break;
            case 1:
                Intent intent = new Intent(this, SetPreferenceActivity.class);  //open the SetPreferenceActivity.java
                startActivityForResult(intent, mRequestCode);

                //Log.d("f1config",two);
                break;
            default:
                break;
        }
        if (fragment != null) {
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, fragment).commit();
            // update selected item and title, then close the drawer
            drawerListView.setItemChecked(position, true);
            drawerListView.setSelection(position);
            drawerLayout.closeDrawer(Gravity.LEFT);
        } else {
            // error in creating fragment
            Log.e("MainActivity", "Error in creating fragment");
        }

    }

    //onStart mean auto pop up and ask do u want to connect
    @Override
    public void onStart() {
        super.onStart();
        if (TRUE) Log.e(BLUETOOTH_CHAT, "++ ON START ++");

        // Prompt user to turn on bluetooth if it is off
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else if (bTService == null) {
            setUpChat();
            //setCoordinate();
        }

    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if (TRUE) Log.e(BLUETOOTH_CHAT, "+ ON RESUME +");

        // onResume() when comes back from enabling bluetooth
        if (bTService != null) {
            // STATE_NONE means mChatService has not started
            if (bTService.getState() == BluetoothService.STATE_NONE) {
                // Start the Bluetooth chat services
                bTService.start();
            }
        }

        //tilt Sensor
        mLastAccelerometerSet = false;
        mLastMagnetometerSet = false;
        mSensorManager.registerListener(this, mAccelerometer,
                SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mMagnetometer,
                SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        if (TRUE) Log.e(BLUETOOTH_CHAT, "- ON PAUSE -");

        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (TRUE) Log.e(BLUETOOTH_CHAT, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
        if (bTService != null) {
            bTService.stop();
            stopRepeatingTask();
        }

        if (TRUE)
            Log.e(BLUETOOTH_CHAT, "--- ON DESTROY ---");
    }

    private void setUpChat() {      //send message that you can type to the robotView and send instructions
        Log.d(BLUETOOTH_CHAT, "setupChat()");     //initialise the buttons to be ready for bluetooth

        // Initialize the array adapter for the conversation thread
        mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
        mConversationView = (ListView) findViewById(R.id.chat_log);
        mConversationView.setAdapter(mConversationArrayAdapter);

        // Initialize the BluetoothService to perform bluetooth connections
        bTService = new BluetoothService(this, mHandler);

        // Initialize the send button with a listener that listens for click
        sendButton = (Button) findViewById(R.id.btn_send);
        sendButton.setEnabled(true);
        sendButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                TextView view = (TextView) findViewById(R.id.send_message);     //the send message line at the bottom
                String message = view.getText().toString();
                //message = message.concat(System.getProperty("line.separator"));
                sendMessage(message);
            }
        });
        mOutEditText = (EditText) findViewById(R.id.send_message);
        mOutEditText.setEnabled(true);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");

        //Initialise Explore and Shortest Path btn
        exploreButton = (Button) findViewById(R.id.btn_explore);
        exploreButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage("beginExplore");
            }
        });

        runshortestButton = (Button) findViewById(R.id.btn_run);
        runshortestButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage("beginFastest");
            }
        });

        // Initialise the arrow buttons
        /*forwardBtn = (Button) findViewById(R.id.btn_up);
        forwardBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                moveForward();
                sendMessage("5F");
            }
        });

        leftBtn = (Button) findViewById(R.id.btn_left);
        leftBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                turnLeft();
            }
        });
        rightBtn = (Button) findViewById(R.id.btn_right);
        rightBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                turnRight();
            }
        });
        */

        tiltToggle = (ToggleButton) findViewById(R.id.tilt_toogle);
        tiltToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    tilt = true;
                } else {
                    tilt = false;
                }
            }
        });
    }

    private void setRobotPos(int xCoordinate, int yCoordinate) {     //set the coordinates of the robotView on the map
        int cenGrid = xCoordinate + yCoordinate * 20;

        robotView[0] = cenGrid - 21;            //robotView takes up 9 squares (0-8)
        robotView[1] = cenGrid - 20;
        robotView[2] = cenGrid - 19;
        robotView[3] = cenGrid - 1;
        robotView[4] = cenGrid;
        robotView[5] = cenGrid + 1;
        robotView[6] = cenGrid + 19;
        robotView[7] = cenGrid + 20;
        robotView[8] = cenGrid + 21;

        int x = 0;
        for (int i = 0; i < arena_maze.length; i++) {        //display the robotView on the map -> see Maze.java
            if (x < 9 && i == robotView[x]) {
                if (x != 5)
                    arena_maze[i] = 1;
                else
                    arena_maze[i] = 2;
                x++;
            } else {
                arena_maze[i] = 0;
            }
        }
        mapView.setAdapter(new Maze(this, arena_maze));
    }

    private void startRepeatingTask() {
        mStatusChecker.run();           //keep sending the message "sendArena" every 10 seconds
    }   //used for the toggle button

    private void stopRepeatingTask() {
        mazeHandler.removeCallbacks(mStatusChecker);
    }


    private void sendMessage(String message) {              //used for sending messages over via bluetooth.
        // Check that we're actually connected before trying anything
        if (bTService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            bTService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
            mOutEditText.setText("");
        }
    }

    private final void setStatus(int resId) {       //not sure what is this though
        try {
            final ActionBar actionBar = getActionBar();
            actionBar.setSubtitle(resId);
        } catch (Exception e) {

        }
    }

    private final void setStatus(CharSequence subTitle) {
        try {
            final ActionBar actionBar = getActionBar();
            actionBar.setSubtitle(subTitle);
        } catch (Exception e) {

        }
    }

    // Decoding of hex send over
    public void decodeMapInfo(String mazeInfo) {
        ArrayList<Integer> obstaclesArr = new ArrayList<Integer>();
        int obstaclesNum = 0;
        Log.d(BLUETOOTH_CHAT, mazeInfo);
        for (int i = 0; i < 75; i++) {
            char gridChar = mazeInfo.charAt(i);
            int hex = Integer.parseInt(String.valueOf(gridChar), 16);
            if (hex > 0) {
                String bin = String.format("%4s", Integer.toBinaryString(hex)).replace(' ', '0');
                Log.d(BLUETOOTH_CHAT, "grid:" + i + " bin:" + bin);
                for (int j = 0; j < 4; j++) {
                    if (Integer.parseInt(String.valueOf(bin.charAt(j))) == 1) {
                        //method to display obstacles
                        obstaclesArr.add(obstaclesNum);
                    }
                    obstaclesNum++;
                }
            } else {
                obstaclesNum += 4;
            }

        }
        setObstacle(obstaclesArr);
    }

    //place the obstacles on the map
    public void setObstacle(ArrayList<Integer> maze) {
        Log.d(BLUETOOTH_CHAT, "----------SET OBSTACLE---------");
        for (int i = 0; i < maze.size(); i++) {
            for (int j = 0; j < 300; j++) {
                if (MAPPING[j] == maze.get(i)) {
                    arena_maze[j] = 3;          //color the maze yellow
                    break;
                }
            }

        }

        mapView.setAdapter(new Maze(this, arena_maze));
    }

    public void robotPosition(String direction) {

        if (robotPosition.equals("forward")) {
            arena_maze[robotView[5]] = 1;
        } else if (robotPosition.equals("left")) {
            arena_maze[robotView[1]] = 1;
        } else if (robotPosition.equals("right")) {
            arena_maze[robotView[7]] = 1;
        } else {
            arena_maze[robotView[3]] = 1;
        }

        if (direction.equals("R")) { //Turn right         //the direction in which the robotView is facing
            if (robotPosition.equals("forward")) {
                robotPosition = "right";

            } else if (robotPosition.equals("right")) {
                robotPosition = "reverse";

            } else if (robotPosition.equals("reverse")) {
                robotPosition = "left";

            } else {
                robotPosition = "forward";

            }
        } else if (direction.equals("L")) { //turn left
            if (robotPosition.equals("forward")) {
                robotPosition = "left";

            } else if (robotPosition.equals("right")) {
                robotPosition = "forward";

            } else if (robotPosition.equals("reverse")) {
                robotPosition = "right";

            } else {
                robotPosition = "reverse";

            }
        } else if (direction.equals("RV")) { // reverse
            if (robotPosition.equals("forward")) {
                robotPosition = "reverse";
            } else if (robotPosition.equals("right")) {
                robotPosition = "left";
            } else if (robotPosition.equals("left")) {
                robotPosition = "right";
            } else {
                robotPosition = "forward";
            }
        }

        robotDirection.setText("Robot Head: " + robotPosition);

        if (robotPosition.equals("forward")) {
            arena_maze[robotView[5]] = 2;
        } else if (robotPosition.equals("left")) {
            arena_maze[robotView[1]] = 2;
        } else if (robotPosition.equals("right")) {
            arena_maze[robotView[7]] = 2;
        } else {
            arena_maze[robotView[3]] = 2;
        }

        mapView.setAdapter(new Maze(this, arena_maze));
    }

    public void moveForward() {
        //robotPosition = "";
        switch (robotPosition) {
            case "forward":
                if ((robotView[2] % 20) != 19) {
                    for (int i = 0; i < 9; i++) {
                        if (i % 3 == 0)
                            arena_maze[robotView[i]] = 0; //arena
                        robotView[i] = robotView[i] + 1;
                        arena_maze[robotView[i]] = 1; //robotView body

                    }
                    arena_maze[robotView[5]] = 2; //header
                }

                break;

            case "right":
                if (robotView[6] < 280) {
                    for (int i = 0; i < 9; i++) {
                        if (i < 3) {
                            arena_maze[robotView[i]] = 0; //arena
                        }
                        robotView[i] = robotView[i] + 20;
                        arena_maze[robotView[i]] = 1; //robotView body
                    }
                    arena_maze[robotView[7]] = 2; //header

                }
                break;
            case "left":
                if (robotView[0] > 20) {
                    for (int i = 0; i < 9; i++) {
                        if (i > 5) {
                            arena_maze[robotView[i]] = 0; //arena
                        }
                        robotView[i] = robotView[i] - 20;
                        arena_maze[robotView[i]] = 1; //robotView body
                    }
                    arena_maze[robotView[1]] = 2; //header

                }
                break;
            case "reverse":
                if ((robotView[0] % 20) != 0) {
                    for (int i = 0; i < 9; i++) {
                        if ((i + 1) % 3 == 0) {
                            arena_maze[robotView[i]] = 0; //arena
                        }
                        robotView[i] = robotView[i] - 1;
                        arena_maze[robotView[i]] = 1; //robotView body
                    }
                    arena_maze[robotView[3]] = 2; //header

                }
                break;
            default:
                Toast.makeText(getApplicationContext(), "Error Moving", Toast.LENGTH_SHORT).show();
        }

        mapView.setAdapter(new Maze(this, arena_maze));
    }

    public void turnRight() {
        robotPosition("R");
        sendMessage("5R");
    }

    public void turnLeft() {
        robotPosition("L");
        sendMessage("5L");
    }

    public void moveReverse(View v) {
        robotPosition("RV");
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            TextView text = (TextView) findViewById(R.id.tb_status);
            if (TRUE) Log.d(BLUETOOTH_CHAT, "onActivityResult " + resultCode);
            switch (requestCode) {
                case REQUEST_CONNECT_DEVICE_SECURE:
                    // When DeviceListActivity returns with a device to connect
                    if (resultCode == Activity.RESULT_OK) {
                        connectDevice(data, true);
                    }
                    break;
                case REQUEST_CONNECT_DEVICE_INSECURE:
                    // When DeviceListActivity returns with a device to connect
                    if (resultCode == Activity.RESULT_OK) {
                        connectDevice(data, false);
                    }
                    break;
                case REQUEST_ENABLE_BT:
                    // When the intent to enable Bluetooth returns, onActivityResume() will execute
                    if (resultCode == Activity.RESULT_OK) {
                        // Bluetooth is now enabled, so set up a chat session
                        setUpChat();
                        //setCoordinate();
                    } else {
                        // Exit the whole application if user does not want to enable
                        Log.d(BLUETOOTH_CHAT, "Bluetooth not enabled");
                        Toast.makeText(this, "Bluetooth was not enabled. Leaving Bluetooth Chat.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    break;
                case mRequestCode:

                    if (resultCode == Activity.RESULT_OK) {
                        String f1config = data.getStringExtra("edittext_preference1");
                        String f2config = data.getStringExtra("f2configs");

                    } else {
                        Log.d(BLUETOOTH_CHAT, "nth to pass back");
                    }
                    break;
            }

        } catch (Exception e) {
        }
    }

    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        bTService.connect(device, secure);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                return true;

            case R.id.action_bluetooth:
                Intent intent = new Intent(this, BluetoothSettings.class);
                startActivity(intent);

                // as a favorite...
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void onSensorChanged(SensorEvent event) {


        long actualTime = event.timestamp;

        if (event.sensor == mAccelerometer) {
            System.arraycopy(event.values, 0, mLastAccelerometer, 0,
                    event.values.length);
            mLastAccelerometerSet = true;
        } else if (event.sensor == mMagnetometer) {
            System.arraycopy(event.values, 0, mLastMagnetometer, 0,
                    event.values.length);
            mLastMagnetometerSet = true;
        }
        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            SensorManager.getRotationMatrix(mR, null, mLastAccelerometer,
                    mLastMagnetometer);
            SensorManager.getOrientation(mR, mOrientation);
        }

        if (tilt) {


            if (((actualTime - lastUpdate) > 1500000000)) {
                float x = mOrientation[2];
                float y = mOrientation[1];
                if (Math.abs(x) > Math.abs(y)) {
                    if (x > 0.5) {
                        moveForward();
                        sendMessage("5F");
                        Log.i(BLUETOOTH_CHAT, "FORWARD");

                    }
                    if (x < -0.9) {

                    }
                } else {
                    if (y > 0.5) {
                        turnLeft();
                        Log.i(BLUETOOTH_CHAT, "TURN LEFT");
                    }
                    if (y < -0.5) {
                        turnRight();
                        Log.i(BLUETOOTH_CHAT, "TURN RIGHT");
                    }
                }

                lastUpdate = actualTime;

            }

        } else {
            return;
        }

    }

    //the stuff inside the nav drawer-> makes it clickable
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            displayView(position);  //position is an int
        }
    }
}


