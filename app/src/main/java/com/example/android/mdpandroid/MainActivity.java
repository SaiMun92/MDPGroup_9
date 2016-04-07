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
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
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
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends Activity implements SensorEventListener {

    // Debugging
    private static final String TAG = "BluetoothChat";
    private static final boolean D = true;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    private String[] drawerListViewItems;
    private DrawerLayout drawerLayout;
    private ListView drawerListView;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private static final int mRequestCode = 100;

    // Layout Views
    private ListView mConversationView;
    private EditText mOutEditText;
    private Button mSendButton;
    private ToggleButton t;
    private Button updateMap;
    private Button forwardBtn;
    private Button rightBtn;
    private Button leftBtn;
    private int mInterval = 10000; // 10 seconds
    private Button exploreBtn;
    private Button runShortestBtn;
    private TextView roboDir;
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
    private Handler exploreHandler;

    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Array adapter for the conversation thread
    private ArrayAdapter<String> mConversationArrayAdapter;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    // Local Bluetooth Adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private BluetoothService bTService = null;

    // Grid initialization
    GridView gridView;
    int robot[] = new int[9];
    static final int[] arena_maze = new int[300];
    String robotPos = "forward";
    int savedXCoord, savedYCoord;
    static final int[] mapping = new int[300];
    int[][] mapFromLaptop = new int[20][15];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // -------------------------------Robot & Maze-----------------------------------

        gridView = (GridView) findViewById(R.id.gridView1);

        robot[0] = 0;
        robot[1] = 1;
        robot[2] = 2;
        robot[3] = 20;
        robot[4] = 21;
        robot[5] = 22;
        robot[6] = 40;
        robot[7] = 41;
        robot[8] = 42;

        int x = 0;
        for (int i = 0; i < arena_maze.length; i++) {//populate the arena_maze array with data.
            if (x < 9 && i == robot[x]) {                //This is to show the robot on the grid
                if (x != 5)                         //x=0 i=0 i=robot[0]=0 arena_maze[0]=1
                    arena_maze[i] = 1;              //x=1 i=1 i=robot[1]=1 arena_maze[1]=1
                else                                //x=2 i=2 i=robot[2]=2 arena_maze[2]=1
                    arena_maze[i] = 2;              //x=3 i=3 i!=robot[3]=20 arena_maze[3]=0
                x++;                                //x=3 i=4 i!=robot[3]=20 arena_maze[4]=0
            } else {                                 //x=3 i=20 i=robot[3]=20 arena_maze[20]=1   u get the point.
                arena_maze[i] = 0;                  //0 represents the areas not explored by the robot.
            }                                       //1 represents the the orange part of the robot. (the rest)
        }                                           //2 represents the white part, or the front of the robot.

        gridView.setAdapter(new Maze(this, arena_maze));    //sets a custom adapter in this case the arena_maze to be displayed on the screen.


        int mapVal = 0;
        for (int i = 0; i < 20; i++) {      //represents all 300 squares(0-299)
            for (int j = 0; j < 15; j++) {
                mapFromLaptop[i][j] = mapVal;   //i is the row, j is the column
                mapping[i + j * 20] = mapFromLaptop[i][j];  //mapping[20]=1, i'm not sure what this does. will check back later
                mapVal++;
            }
        }

        //Robot head direction
        roboDir = (TextView) findViewById(R.id.robotHead);  //the space beside the F1 button
        roboDir.setText("Robot Head: " + robotPos);         //robotPos indicates the robot position

        if (D) Log.e(TAG, "+++ ON CREATE +++");

        // Get local Bluetooth adapter of the device
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        // If the adapter is null, then Bluetooth is not supported by device
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // --------------------------Navigation Drawer---------------------------------------
        // get list items from strings.xml
        drawerListViewItems = getResources().getStringArray(R.array.items);
        // get ListView defined in activity_main.xml
        drawerListView = (ListView) findViewById(R.id.left_drawer); //slide from the left
        // Set the adapter for the list view
        drawerListView.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_listview_item, drawerListViewItems));
        // 2. App Icon
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        // 2.1 create ActionBarDrawerToggle
        actionBarDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                drawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        );

        // 2.2 Set actionBarDrawerToggle as the DrawerListener
        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        // 2.3 enable and show "up" arrow
        //getActionBar().setDisplayHomeAsUpEnabled(true);           //the 3 lines at the top left hand corner, not working.
        // just styling option
        drawerListView.setOnItemClickListener(new DrawerItemClickListener());

        //------------------Auto and Manual Map Update---------------------------------------
        mazeHandler = new Handler();        //Handler send and process messages

        t = (ToggleButton) findViewById(R.id.toggle);        //auto manual toggle button

        updateMap = (Button) findViewById(R.id.btn_update);
        t.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
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
                editor.putBoolean("toggleButton", t.isChecked());
                editor.commit();
            }
        });

        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        t.setChecked(sharedPreferences.getBoolean("toggleButton", false));      //T = toggle button

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

                //String value = getIntent().getStringExtra("f1config");
                //String f1config = data.getStringExtra("edittext_preference1");
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                String f1configs = prefs.getString("edittext_preference1", "");     //send the predefined string
                sendMessage(f1configs);
                Log.d("ble", f1configs);        //log the screen
                //sendMessage("hi");


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

    //the stuff inside the nav drawer-> makes it clickable
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            displayView(position);  //position is an int
        }
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
                        sendMessage(message);       //sends the position of the robot over to the computer
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
        if (D) Log.e(TAG, "++ ON START ++");

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
        if (D) Log.e(TAG, "+ ON RESUME +");

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
        if (D) Log.e(TAG, "- ON PAUSE -");

        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (D) Log.e(TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
        if (bTService != null) {
            bTService.stop();
            stopRepeatingTask();
        }

        if (D)
            Log.e(TAG, "--- ON DESTROY ---");
    }

    private void setUpChat() {      //send message that you can type to the robot and send instructions
        Log.d(TAG, "setupChat()");     //initialise the buttons to be ready for bluetooth

        // Initialize the array adapter for the conversation thread
        mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
        mConversationView = (ListView) findViewById(R.id.chat_log);
        mConversationView.setAdapter(mConversationArrayAdapter);

        // Initialize the BluetoothService to perform bluetooth connections
        bTService = new BluetoothService(this, mHandler);

        // Initialize the send button with a listener that listens for click
        mSendButton = (Button) findViewById(R.id.btn_send);
        mSendButton.setEnabled(true);
        mSendButton.setOnClickListener(new OnClickListener() {
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
        exploreBtn = (Button) findViewById(R.id.explore);
        exploreBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage("e");
//                String readMessage = "hex 2 2 90 FE007000F801C003800300000000000000000000000000000000000000000000000000000003 000080";
//                decodeMapInfo(readMessage);

            }
        });

        runShortestBtn = (Button) findViewById(R.id.runButton);
        runShortestBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage("f");
            }
        });

        // Initialise the arrow buttons
        forwardBtn = (Button) findViewById(R.id.rightButton);
        forwardBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                moveForward();
                sendMessage("f");
            }
        });

        leftBtn = (Button) findViewById(R.id.upButton);
        leftBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                turnLeft();
            }
        });
//        rightBtn = (Button) findViewById(R.id.downButton);
//        rightBtn.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                turnRight();
//            }
//        });


        tiltToggle = (ToggleButton) findViewById(R.id.tilt_toggle);
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

    private void setRobotPos(int xCoordinate, int yCoordinate) {     //set the coordinates of the robot on the map
        int cenGrid = xCoordinate + yCoordinate * 20;

        robot[0] = cenGrid - 21;            //robot takes up 9 squares (0-8)
        robot[1] = cenGrid - 20;
        robot[2] = cenGrid - 19;
        robot[3] = cenGrid - 1;
        robot[4] = cenGrid;
        robot[5] = cenGrid + 1;
        robot[6] = cenGrid + 19;
        robot[7] = cenGrid + 20;
        robot[8] = cenGrid + 21;

        int x = 0;
        for (int i = 0; i < arena_maze.length; i++) {        //display the robot on the map -> see Maze.java
            if (x < 9 && i == robot[x]) {
                if (x != 5)
                    arena_maze[i] = 1;
                else
                    arena_maze[i] = 2;      //robot head
                x++;
            } else {
                arena_maze[i] = 0;
            }
        }
        gridView.setAdapter(new Maze(this, arena_maze));
    }

    private void setRobotPosWithDir(int xCoordinate, int yCoordinate, String robotDirection) {
        int cenGrid = xCoordinate + yCoordinate * 20;

        robot[0] = cenGrid - 21;            //robot takes up 9 squares (0-8)
        robot[1] = cenGrid - 20;
        robot[2] = cenGrid - 19;
        robot[3] = cenGrid - 1;
        robot[4] = cenGrid;
        robot[5] = cenGrid + 1;
        robot[6] = cenGrid + 19;
        robot[7] = cenGrid + 20;
        robot[8] = cenGrid + 21;

        int x = 0;
        for (int i = 0; i < arena_maze.length; i++) {        //display the robot on the map -> see Maze.java
            if (x < 9 && i == robot[x]) {

                arena_maze[i] = 1;

                if (robotDirection == "W") {
                    arena_maze[robot[1]] = 2;
                } else if (robotDirection == "N") {
                    arena_maze[robot[5]] = 2;
                } else if (robotDirection == "E") {
                    arena_maze[robot[7]] = 2;
                } else if (robotDirection == "S") {
                    arena_maze[robot[3]] = 2;
                }
                x++;
            } else {
//                arena_maze[i] = 0;
            }
        }
        gridView.setAdapter(new Maze(this, arena_maze));
    }


    //Auto Updating of Maze info
    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                sendMessage("sendArena");
                mazeHandler.postDelayed(mStatusChecker, mInterval);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

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

    private final void setStatus(int resId) {       //status in the action bar
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

    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:              //talks about the state of the bluetooth in the "up" button
                    if (D)
                        Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                            mConversationArrayAdapter.clear();

                            break;
                        case BluetoothService.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            setStatus(R.string.title_not_connected);
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
                    byte[] readBuf = (byte[]) msg.obj;                      //the status of the robot
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    TextView text = (TextView) findViewById(R.id.tb_status);
                    text.setMovementMethod(new ScrollingMovementMethod());

//                    if (readMessage.startsWith(getString(R.string.start_STATUS))) {
//                        text.setText(readMessage);
//                    } else if (readMessage.contains(getString(R.string.robot_status))) //status
//                    {
//                        text.setText(readMessage.substring(11, readMessage.length() - 2));
//                    } else if (readMessage.contains("hex")) {
                        //format must be the same
                        //remove hex
                    if (readMessage.length()> 0){
                        List<String> robotInfo = Arrays.asList(readMessage.split("\\s+"));
                        String xcoord = robotInfo.get(0);       //first 2 is the grid info
                        Log.d(TAG, "xpos" + xcoord);

                        String ycoord = robotInfo.get(1);
                        Log.d(TAG, "ypos" + ycoord);

                       String robotDirection = robotInfo.get(2);
                       Log.d(TAG, "dir" + robotDirection);

                        int n = 76;
                        char[] chars = new char[n];
                        Arrays.fill(chars, 'f');
                        String exploreInfo = new String(chars);
                        Log.d(TAG, "part 1 hex" + exploreInfo);

                        String obstacleInfo = robotInfo.get(3);
                        Log.d(TAG, "part 2 hex" + obstacleInfo);

                        text.setText(xcoord + ycoord + robotDirection +  exploreInfo + obstacleInfo);
                        decodeMapInfo(xcoord, ycoord, robotDirection, exploreInfo, obstacleInfo);
                        Log.d(TAG, readMessage);
//                        Log.d(TAG, mapInfo);
                    }
//                    else if(readMessage.contains(getString(R.string.map_status)))       //grid
//                    {
//                        String mazeInfo = readMessage.substring(12, readMessage.length() - 2);
//                        decodeMapInfo(mazeInfo);
//                        text.setText(readMessage);
//                        Log.d(TAG, readMessage);
//
//                    }
                    else {
                        mConversationArrayAdapter.add(mConnectedDeviceName + ": " + readMessage);   //chat_log
                        switch (readMessage.charAt(0)) {            //F, L, R
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
                                Log.d(TAG, "normal text");
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


    public void decodeMapInfo(String xcoord, String ycoord, String robotDirection, String exploreInfo, String obstacleInfo) {

        //Splitting the string by space - position of robot, direction and part 1 and part 2
//        List<String> robotInfo = Arrays.asList(Message.split("\\s+"));

        //Setting position of robot
//
//        String xcoord = robotInfo.get(1);       //first 2 is the grid info
//        Log.d(TAG, "xpos" + xcoord);
//
//        String ycoord = robotInfo.get(2);
//        Log.d(TAG, "ypos" + ycoord);
//
//        String robotDirection = robotInfo.get(3);
//        Log.d(TAG, "dir" + robotDirection);

        setRobotPosWithDir(Integer.parseInt(ycoord), Integer.parseInt(xcoord), robotDirection);
//        setRobotPos(Integer.parseInt(ycoord), Integer.parseInt(xcoord));


        //Getting part 1 and part 2 of hex string

//        String exploreInfo = robotInfo.get(4);
//
//        int n = 76;
//        char[] chars = new char[n];
//        Arrays.fill(chars, '1');
//        String exploreInfo = new String(chars);
//        Log.d(TAG, "part 1 hex" + exploreInfo);
//
//        String obstacleInfo = robotInfo.get(4);
//        Log.d(TAG, "part 2 hex" + obstacleInfo);


        //Converting hex string to binary string

        String exploreInt = hexToBin(exploreInfo);
//        String exploreInt = String.format("%0300d", 10000000);
//        exploreInt = "11" + exploreInt + "11";
        Log.d(TAG, "part1" + exploreInt);


        String obstacleInt = hexToBin(obstacleInfo);
        Log.d(TAG, "part2" + obstacleInt);


        ArrayList<Integer> exploreMap = new ArrayList<>();
        int exploredBits = 0;

        //Added all the bits to an arraylist

        for (int i = 2; i < exploreInt.length() - 3; i++) {   //get the gridInfo from the robotInfo ArrayList
            exploreMap.add(Character.getNumericValue(exploreInt.charAt(i)));    //arrayList
            if (exploreInt.charAt(i) == '1') {
                exploredBits++;
            }
        }
        Log.d(TAG, "Explored Bits" + String.valueOf(exploredBits));


        int paddedBits = (8 - exploredBits % 8) % 8;
        Log.d(TAG, "padded bits" + String.valueOf(paddedBits));

        //add 0's in front of OBstacleInt = exploredBits - obstacleInt+paddedBits
        int numZeroToAdd = exploredBits - obstacleInt.length() + paddedBits;
        Log.d(TAG, "num of zeroes to add" + String.valueOf(numZeroToAdd));


        for (int i = 0; i < numZeroToAdd; i++) {
            obstacleInt = "0" + obstacleInt;
            Log.d(TAG, "zeroes added" + String.valueOf(obstacleInt));

        }
        Log.d(TAG, "lenth obstacle int" + String.valueOf(obstacleInt.length()));
        //Remove padded bits
        obstacleInt =  obstacleInt.substring(0, obstacleInt.length() - paddedBits);
//        obstacleInt = "0";

        Log.d(TAG, "padded bits removed from end" + String.valueOf(obstacleInt));

        ArrayList<Integer> obstacleMap = new ArrayList<>();
        for (int i = 0; i <= obstacleInt.length() - 1; i++) {   //get the gridInfo from the robotInfo ArrayList
            obstacleMap.add(Character.getNumericValue(obstacleInt.charAt(i)));    //arrayList
            Log.d("HELLO", "Added to obstacle map! Size = "+ obstacleMap.size());
        }

        int z = 0;
        for (int j = 0; j < exploreMap.size() - 1; j++) {
            Log.d(TAG, "for loop one");

            Integer explored = exploreMap.get(j);
            try {
                if (explored == 1) {
                    Integer obstacle = obstacleMap.get(z);
                    if (obstacle == 0) {
                        Log.d(TAG, "if cond true");

//                        arena_maze[j]=4;
//                         arena_maze[j / 15 + (280 -(20 * (j % 15)))] = 4;
//                        arena_maze[j / 15 + (20 * (j % 15))] = 4;


                    }else{
                        Log.d(TAG, "if cond false");

//                        arena_maze[j]=3;
//                        arena_maze[j / 15 + (280 - (20 * (j % 15)))] = 3;
                        arena_maze[j / 15 + (20 * (j % 15))] = 3;

                    }
                    z++;

                }
                else {
                    arena_maze[j / 15 + (20 * (j % 15))] = 0;
                }
            } catch (NumberFormatException e) {

            }
//            setRobotPos(Integer.parseInt(ycoord), Integer.parseInt(xcoord));

            setRobotPosWithDir(Integer.parseInt(ycoord), Integer.parseInt(xcoord), robotDirection);
            Log.d(TAG, xcoord + " " + ycoord + " " + robotDirection);
            Log.d(TAG, "for loop");

        }



        gridView.setAdapter(new Maze(getApplicationContext(), arena_maze));

        exploreMap.clear();
        obstacleMap.clear();
    }

    static String hexToBin(String s) {
        return new BigInteger(s, 16).toString(2);
    }


    //place the obstacles on the map
    public void setObstacle(ArrayList<Integer> maze) {
        Log.d(TAG, "----------SET OBSTACLE---------");
        for (int i = 0; i < maze.size(); i++) {
            for (int j = 0; j < 300; j++) {
                if (mapping[j] == maze.get(i)) {
                    arena_maze[j] = 3;          //color the maze yellow
                    break;
                }
            }

        }

        gridView.setAdapter(new Maze(this, arena_maze));
    }

    public void robotPosition(String direction) {

        if (robotPos.equals("forward")) {               //to recolor back the robot head from white to orange
            arena_maze[robot[5]] = 1;
        } else if (robotPos.equals("left")) {
            arena_maze[robot[1]] = 1;
        } else if (robotPos.equals("right")) {
            arena_maze[robot[7]] = 1;
        } else {
            arena_maze[robot[3]] = 1;
        }

        if (direction.equals("R")) { //Turn right         //the direction in which the robot is facing
            if (robotPos.equals("forward")) {
                robotPos = "right";

            } else if (robotPos.equals("right")) {
                robotPos = "reverse";

            } else if (robotPos.equals("reverse")) {
                robotPos = "left";

            } else {
                robotPos = "forward";

            }
        } else if (direction.equals("L")) { //turn left
            if (robotPos.equals("forward")) {
                robotPos = "left";

            } else if (robotPos.equals("right")) {
                robotPos = "forward";

            } else if (robotPos.equals("reverse")) {
                robotPos = "right";

            } else {
                robotPos = "reverse";

            }
        } else if (direction.equals("RV")) { // reverse
            if (robotPos.equals("forward")) {
                robotPos = "reverse";
            } else if (robotPos.equals("right")) {
                robotPos = "left";
            } else if (robotPos.equals("left")) {
                robotPos = "right";
            } else {
                robotPos = "forward";
            }
        }

        roboDir.setText("Robot Head: " + robotPos);

        if (robotPos.equals("forward")) {
            arena_maze[robot[5]] = 2;
        } else if (robotPos.equals("left")) {
            arena_maze[robot[1]] = 2;
        } else if (robotPos.equals("right")) {
            arena_maze[robot[7]] = 2;
        } else {
            arena_maze[robot[3]] = 2;
        }

        gridView.setAdapter(new Maze(this, arena_maze));
    }

    public void moveForward() {
        //robotPos = "";
        switch (robotPos) {
            case "forward":
                if ((robot[2] % 20) != 19) {
                    for (int i = 0; i < 9; i++) {
                        if (i % 3 == 0)
                            arena_maze[robot[i]] = 0; //arena
                        robot[i] = robot[i] + 1;
                        arena_maze[robot[i]] = 1; //robot body

                    }
                    arena_maze[robot[5]] = 2; //header
                }

                break;

            case "right":
                if (robot[6] < 280) {
                    for (int i = 0; i < 9; i++) {
                        if (i < 3) {
                            arena_maze[robot[i]] = 0; //arena
                        }
                        robot[i] = robot[i] + 20;
                        arena_maze[robot[i]] = 1; //robot body
                    }
                    arena_maze[robot[7]] = 2; //header

                }
                break;
            case "left":
                if (robot[0] > 20) {
                    for (int i = 0; i < 9; i++) {
                        if (i > 5) {
                            arena_maze[robot[i]] = 0; //arena
                        }
                        robot[i] = robot[i] - 20;
                        arena_maze[robot[i]] = 1; //robot body
                    }
                    arena_maze[robot[1]] = 2; //header

                }
                break;
            case "reverse":
                if ((robot[0] % 20) != 0) {
                    for (int i = 0; i < 9; i++) {
                        if ((i + 1) % 3 == 0) {
                            arena_maze[robot[i]] = 0; //arena
                        }
                        robot[i] = robot[i] - 1;
                        arena_maze[robot[i]] = 1; //robot body
                    }
                    arena_maze[robot[3]] = 2; //header

                }
                break;
            default:
                Toast.makeText(getApplicationContext(), "Error Moving", Toast.LENGTH_SHORT).show();
        }

        gridView.setAdapter(new Maze(this, arena_maze));
    }

    public void turnRight() {
        robotPosition("R");
        sendMessage("r");
    }

    public void turnLeft() {
        robotPosition("L");
        sendMessage("l");
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            TextView text = (TextView) findViewById(R.id.tb_status);
            if (D) Log.d(TAG, "onActivityResult " + resultCode);
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
                        Log.d(TAG, "Bluetooth not enabled");
                        Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    break;
                case mRequestCode:

                    if (resultCode == Activity.RESULT_OK) {
                        String f1config = data.getStringExtra("edittext_preference1");
                        String f2config = data.getStringExtra("f2configs");

                        Log.i("hmm-", "" + f1config);

                    } else {
                        Log.d(TAG, "nth to pass back");
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
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent serverIntent = null;
        switch (item.getItemId()) {
            case R.id.secure_connect_scan:
                // Launch the DeviceListActivity to see devices and do scan
                serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
                break;
        }
        return false;
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
                        sendMessage("f");
                        Log.i(TAG, "FORWARD");

                    }
                    if (x < -0.9) {

                    }
                } else {
                    if (y > 0.5) {
                        turnLeft();
                        Log.i(TAG, "TURN LEFT");
                    }
                    if (y < -0.5) {
                        turnRight();
                        Log.i(TAG, "TURN RIGHT");
                    }
                }

                lastUpdate = actualTime;

            }

        } else {
            return;
        }

    }


}


