package com.example.android.mdpandroid;

import android.bluetooth.BluetoothAdapter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;
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
import android.support.v4.view.GravityCompat;
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
import android.hardware.SensorManager;

import java.lang.reflect.Array;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

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


    private SharedPreferences prefs;
    private boolean autoSelection;

    private String[] drawerListViewItems;
    private DrawerLayout drawerLayout;
    private ListView drawerListView;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    // Layout Views
    private ListView mConversationView;
    private EditText mOutEditText;
    private EditText setXCoord;
    private EditText setYCoord;
    private Button mSendButton;
    private Button saveCoord;
    private ToggleButton t;
    private Button updateMap;
    private Button forwardBtn;
    private Button rightBtn;
    private Button leftBtn;
    private int mInterval = 10000; // 10 seconds
    private Button exploreBtn;
    private Button runShortestBtn;

    //Handler for Maze
    private Handler mazeHandler;

    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Array adapter for the conversation thread
    private ArrayAdapter<String> mConversationArrayAdapter;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    // Local Bluetooth Adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    //private BluetoothService bTService = null; //need to add BluetoothService.java in the java files

    //Grid initialisation
    GridView gridView;
    int robot [] = new int[9];
    static final int[] arena_maze = new int [300];  //create an array of 300 spaces
    String robotpos = "forward";
    int savedXCoord, savedYCoord;
    static final int[] mapping = new int[300];
    int[][] mapFromLaptop = new int[20][15];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); //oncreate method is to save the data in case u log out and u want to log in back the app again.
        setContentView(R.layout.activity_main);

        // -------------------------------Robot & Maze-------------------------
        gridView = (GridView) findViewById(R.id.gridView1); //save the gridview1 into gridView.
        robot[0] = 0;   //mapping the robot to the gridview
        robot[1] = 1;
        robot[2] = 2;
        robot[3] = 20;
        robot[4] = 21;
        robot[5] = 22;
        robot[6] = 40;
        robot[7] = 41;
        robot[8] = 42;
        int x = 0;
        for(int i=0; i<arena_maze.length; i++) {    //populate the arena_maze array with data.
            if(x<9 && i==robot[x]) {                //This is to show the robot on the grid
                if( x != 5)                         //x=0 i=0 i=robot[0]=0 arena_maze[0]=1
                    arena_maze[i] = 1;              //x=1 i=1 i=robot[1]=1 arena_maze[1]=1
                else                                //x=2 i=2 i=robot[2]=2 arena_maze[2]=1
                    arena_maze[i] = 2;              //x=3 i=3 i!=robot[3]=20 arena_maze[3]=0
                x++;                                //x=3 i=4 i!=robot[3]=20 arena_maze[4]=0
            }else {                                 //x=3 i=20 i=robot[3]=20 arena_maze[20]=1   u get the point.
                arena_maze[i] = 0;                  //0 represents the areas not explored by the robot.
            }                                       //1 represents the the orange part of the robot. (the rest)
        }                                           //2 represents the white part, or the front of the robot.

        gridView.setAdapter(new Maze(this, arena_maze));    //sets a custom adapter in this case the arena_maze to be displayed on the screen.

        int mapVal = 0;
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 15; j++) {
                mapFromLaptop[i][j] = mapVal;
                mapping[i + j * 20] = mapFromLaptop[i][j];
                mapVal++;
            }
        }

        if(true) Log.v(TAG, "+++ ON CREATE +++");

        //Get local bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // get list items from strings.xml
        drawerListViewItems = getResources().getStringArray(R.array.items); //the array is inside the stings.xml
        // get ListView defined in activity_main.xml
        drawerListView = (ListView) findViewById(R.id.left_drawer);
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
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
