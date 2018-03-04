package com.example.android.mastercontrol;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import static android.R.attr.name;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    //grid variables
    public boolean gridMode = true;
    public boolean autoMode = false;

    //variables for logging
    float[] mGrav;
    float[] mAcc;
    float[] mGyro;
    float[] mGeo;
    File rrFile;
    File jpgFile;
    File recordingFile;
    FileOutputStream fosRR;
    Boolean logging;
    Location dest_loc;
    PixelGridView pixelGrid;
    double[][] map;
    double [][] orig_map = new double[4][4];
    ArrayList<Location> waypoints;
    Location[][] gridLocations;
    int currRow;
    int currCol;

    //Master variables
    String fromMinion = "", //strings received from WiFi router
            toMinion = "";  //strings to send through WiFi router
    enum Robots {
        DOC, MR, MRS, CARLITO, CARLOS, CARLY, CARLA, CARLETON,
    }
    Robots minion = Robots.DOC; //initial minion
    double[] gps_coords = {0,0};
    int minion_grid_num_1, minion_grid_num_2, minion_grid_num_3; //add more for more robots
    boolean isGridFullySearched = false,
            isObstacleFound = false,
            isMannequinFound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //setup pixel grid
        pixelGrid = new PixelGridView(this);

        pixelGrid.setMapColors(orig_map, minion, (int) gps_coords[0], (int) gps_coords[1],
                isMannequinFound);
        pixelGrid.setId(View.generateViewId());
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(700, 700);
        pixelGrid.setLayoutParams(lp);

        ((LinearLayout) findViewById(R.id.leftll)).addView(pixelGrid);

        //add functionality to gridMode button
        Button buttonGrid = (Button) findViewById(R.id.btnGrid);
        buttonGrid.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!gridMode) {
                    v.setBackgroundResource(R.drawable.button_grid_on);
                    gridMode = true;
                    pixelGrid.setVisibility(View.VISIBLE);
                } else {
                    v.setBackgroundResource(R.drawable.button_grid_off);
                    gridMode = false;
                    pixelGrid.setVisibility(View.INVISIBLE);
                }
            }
        });

    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    //Called whenever the value of a sensor changes
    @Override
    public final void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_GRAVITY)
            mGrav = event.values;
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE)
            mGyro = event.values;
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mAcc = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeo = event.values;


        receive_from_m();
        updateMap();

        if (isGridFullySearched) {
            //give new grid number

        }

        send_to_m(minion_grid_num_1);
    }

/**************************************************************************************************/


    //receives info in form of string & parses to variables' appropriate types
    void receive_from_m () {
        String string_name = fromMinion.substring(fromMinion.indexOf("NAME"),fromMinion.indexOf("GPS")),
                string_gps = fromMinion.substring(fromMinion.indexOf("GPS"),fromMinion.indexOf("OBS")),
                string_obs = fromMinion.substring(fromMinion.indexOf("OBS"),fromMinion.indexOf("MANN")),
                string_mann = fromMinion.substring(fromMinion.indexOf("MANN"),fromMinion.indexOf("SEARCH")),
                string_search = fromMinion.substring(fromMinion.indexOf("SEARCH"),fromMinion.length());

        //get name of minion that sent string
        string_name = string_name.substring(string_name.indexOf(":")+2,string_name.indexOf(","));

        switch (string_name) {
            case "DOC":
                minion = Robots.DOC;
                break;
            case "MR":
                minion = Robots.MR;
                break;
            case "MRS":
                minion = Robots.MRS;
                break;
            case "CARLITO":
                minion = Robots.CARLITO;
                break;
            case "CARLOS":
                minion = Robots.CARLOS;
                break;
            case "CARLY":
                minion = Robots.CARLY;
                break;
            case "CARLA":
                minion = Robots.CARLA;
                break;
            case "CARLETON":
                minion = Robots.CARLETON;
                break;
            default:
                Log.i("ERROR","Invalid robot name. Refer to Robot name list in code.");
        }

        //get GPS coordinates from message string
        gps_coords = getCoords(string_gps);

        //get obstacle boolean
        if (string_obs.contains("true")) {
            isObstacleFound = true;
        } else {
            isObstacleFound = false;
        }

        //get mannequin boolean
        if (string_mann.contains("true")) {
            isMannequinFound = true;
        }
        else {
            isMannequinFound = false;
        }

        //get search boolean
        if (string_search.contains("true")) {
            isGridFullySearched = true;
        } else {
            isGridFullySearched = false;
        }
    }

    void updateMap () {
        pixelGrid.setMapColors(orig_map, minion, (int) gps_coords[0], (int) gps_coords[1],
                isMannequinFound);
    }

    //send grid # for m to search
    void send_to_m (int current_grid_number) {
        toMinion = "" + current_grid_number;

        //actually send info here
    }

    double[] getCoords (String str) {
        int find_comma, find_colon;

        //find lat
        find_colon = str.indexOf(':');
        find_comma = str.indexOf(',');
        String first_num = str.substring(find_colon+1, find_comma-1);

        //cut out lat
        str = str.substring(find_comma + 1, str.length());

        //find lon
        find_colon = str.indexOf(':');
        find_comma = str.indexOf(']');
        String sec_num = str.substring(find_colon+1, find_comma-1);


        double[] coords={0,0};
        double d = Double.parseDouble(first_num);
        double d2 = Double.parseDouble(sec_num);
        coords[0] = d;
        coords[1] = d2;
        return coords;
    }

}
