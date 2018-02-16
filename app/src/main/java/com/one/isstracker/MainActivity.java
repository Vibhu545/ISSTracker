package com.one.isstracker;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements LocationListener{
    EditText etNumber;
    Button btSetLatLng, btVideoFeed;
    String URL;
    LocationManager locationManager;
    Location currentLocation;
    String numberOfPasses;
    RecyclerView recycler_view;
    RecyclerAdapter recyclerAdapter;
    String URLVideoFeed = "http://www.ustream.tv/channel/live-iss-stream";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etNumber = (EditText) findViewById(R.id.etNumber);
        btSetLatLng = (Button) findViewById(R.id.btSetLatLng);
        btVideoFeed = (Button) findViewById(R.id.btVideoFeed);
        recycler_view = (RecyclerView) findViewById(R.id.recycler_view);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, MainActivity.this);
        btSetLatLng.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numberOfPasses = etNumber.getText().toString().trim();
                if(numberOfPasses.equals("")) {
                    etNumber.setError("This is a required field");
                    return;
                }
                //Checking if the required Manifest permissions are entered
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    return;
                }
                currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                //If GPS is turned off, the user can get the co-ordinates using Network
                if(currentLocation == null)
                    currentLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if(currentLocation != null) {
                    //The URL's content is manipulated by adding the values of Latitude, Longitude and Number of Passes
                    URL = "http://api.open-notify.org/iss-pass.json?lat=" + String.valueOf(currentLocation.getLatitude()) +
                            "&lon=" + String.valueOf(currentLocation.getLongitude())
                            + "&n=" + numberOfPasses;
                    FindPasses task = new FindPasses();
                    //The modified URL is to be executed in the AsyncTask
                    task.execute(URL);
                }
            }
        });

        btVideoFeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(URLVideoFeed));
                startActivity(i);
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;//Update the currentLocation variable if device's location changes
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    class FindPasses extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            //We derive the actual web service response here
            StringBuffer response = null;
            try {
                String actualURL = params[0];
                java.net.URL url = new URL(actualURL);

                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                InputStream inputStream = con.getInputStream();

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(inputStream));

                String inputLine;

                response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
            //This is the required response of the API (Un-parsed)
            return response.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            //Calling parseResponse method
            ArrayList<Entity> passesArrayList = parseResponse(result);
            //The parsed data in the form of an Array List is passed to setData method
            setData(passesArrayList);
        }

        private void setData(ArrayList<Entity> passesArrayList) {
            //The recycler adapter is called here using the Arraylist
            recycler_view.setLayoutManager(new LinearLayoutManager(MainActivity.this));
            recyclerAdapter = new RecyclerAdapter(passesArrayList, getLayoutInflater());
            recycler_view.setAdapter(recyclerAdapter);
        }

        ArrayList<Entity> parseResponse(String body) {
            ArrayList<Entity> passesArrayList = new ArrayList<>();
            try {
                JSONObject jsonObject = new JSONObject(body);
                JSONArray jsonArray = jsonObject.getJSONArray("response");
                passesArrayList.clear();
                //The for loop fetches the data from the JSON Array
                for (int i = 0; i < jsonArray.length(); i++) {
                    Entity entity = new Entity();
                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    //Converting the timestamp into an actual date
                    long actualDate = Long.parseLong( jsonObject1.getString("risetime") );
                    Date riseTime = new Date( actualDate * 1000 );
                    entity.setRiseTimeEntity(String.valueOf(riseTime));
                    entity.setDurationEntity(jsonObject1.getString("duration"));
                    passesArrayList.add(entity);
                }
            } catch (Exception e) {
            }
            return passesArrayList;
        }
    }
}
