package com.pglive.testweb;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.LinearGradient;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private String TAG = MainActivity.class.getSimpleName();

    private ProgressDialog pDialog;
    private ListView lv;
    private TextToSpeech t1;
//    private String[] allText;
    public List<String> allText = new ArrayList<String>();
    private String time = "60";

    // URL to get contacts JSON
//    private static String url = "http://10.0.2.2:8080/";
    private static String url="http://192.168.1.42:8080/";
//    ArrayList<HashMap<String, String>> contactList;
//    ArrayList<HashMap<String, String>> trafficList;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        t1 = new TextToSpeech (getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                Log.d("CheckArea","It started");
                if (status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.UK);
                }
                if (status == TextToSpeech.SUCCESS){
//                    trafficList = new ArrayList<>();
//                    lv = (ListView) findViewById(R.id.list);
                    new GetTrafficData().execute();
                }
            }
        });

        final ImageButton button = (ImageButton) findViewById(R.id.playbutton);
        button.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                new GetTrafficData().execute();
            }
        });

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.timeChoice, R.layout.spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(R.layout.spinner_dropdown);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);


    }





    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
//            parent.getItemAtPosition(pos);
        Log.e(TAG, String.valueOf(pos));
        switch (pos){
            case 0:
                time = "15";
                break;
            case 1:
                time = "30";
                break;
            case 2:
                time = "60";
                break;
            case 3:
                time = "120";
                break;
            case 4:
                time = "180";
                break;
            default:
                time = "15";
                break;
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }



    public void playContent (View view){
        new GetTrafficData().execute();
    }


    private class GetTrafficData extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();

            // Making a request to url and getting response
            Log.e(TAG,(url+"time/"+time) );
            String jsonStr = sh.makeServiceCall(url+"time/"+time);

            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONArray jsonObj = new JSONArray(jsonStr);

                    // looping through All Traffic
                    for (int i = 0; i < jsonObj.length(); i++) {
                        JSONObject c = jsonObj.getJSONObject(i);

                        String id = c.getString("id");
                        String text = c.getString("trafficText");

                        // tmp hash map for single contact
//                        HashMap<String, String> contact = new HashMap<>();

                        // adding each child node to HashMap key => value
//                        contact.put("id", id);
//                        contact.put("text", text);
                        // adding contact to contact list
//                        trafficList.add(contact);
                        allText.add(text);
                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
            /**
             * Updating parsed JSON data into ListView
             * */

            for (String s : allText){
                t1.speak(s, TextToSpeech.QUEUE_FLUSH,null,null);
                try {
                    TimeUnit.SECONDS.sleep(4);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            allText.clear();

        }

    }
}