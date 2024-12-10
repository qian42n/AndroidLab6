package com.cst7335.weatherapp;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private LinearLayout weatherLinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        weatherLinearLayout = findViewById(R.id.linearLayout_weather);
        Button refreshButton = findViewById(R.id.button_refresh);

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Execute the AsyncTask
                new FetchWeatherTask().execute("http://api.openweathermap.org/data/2.5/forecast?id=524901&appid=bd5e378503939ddaee76f12ad7a97608");
            }
        });
    }

    private class FetchWeatherTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();
                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }
                Log.d("Weather Data", result);
                return result;
            } catch (Exception e) {
                e.printStackTrace();
                return "Failed to fetch data";
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            weatherLinearLayout.removeAllViews(); // Clear previous views
            try {
                JSONObject jsonObject = new JSONObject(result);
                JSONArray listArray = jsonObject.getJSONArray("list");

                for (int i = 0; i < listArray.length(); i++) {
                    JSONObject listObject = listArray.getJSONObject(i);
                    long dateTimeInMillis = listObject.getLong("dt") * 1000; // Convert to milliseconds
                    Date date = new Date(dateTimeInMillis);
                    SimpleDateFormat dateFormatter = new SimpleDateFormat("EEEE, MMMM d, yyyy h:mm a", Locale.getDefault());
                    String dateText = dateFormatter.format(date);

                    // Determine the time of day for background color
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);
                    int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
                    int backgroundColor;

                    // Define colors for different times of day
                    if (hourOfDay >= 6 && hourOfDay < 9) { // Dawn
                        backgroundColor = Color.parseColor("#FFD54F"); // Example color for dawn
                    } else if (hourOfDay >= 9 && hourOfDay < 17) { // Day
                        backgroundColor = Color.parseColor("#FFEB3B"); // Example color for day
                    } else if (hourOfDay >= 17 && hourOfDay < 20) { // Dusk
                        backgroundColor = Color.parseColor("#FF7043"); // Example color for dusk
                    } else { // Night
                        backgroundColor = Color.parseColor("#2196F3"); // Example color for night
                    }

                    // Create a container for each weather entry
                    LinearLayout entryContainer = new LinearLayout(MainActivity.this);
                    entryContainer.setOrientation(LinearLayout.VERTICAL);
                    entryContainer.setPadding(20, 20, 20, 20);
                    GradientDrawable background = new GradientDrawable();
                    background.setColor(backgroundColor);
                    background.setCornerRadius(10);
                    entryContainer.setBackground(background);

                    // Create a new TextView for the date and add it to the entryContainer
                    TextView dateTextView = new TextView(MainActivity.this);
                    dateTextView.setText(dateText);
                    entryContainer.addView(dateTextView);

                    // Create a new ImageView for the weather icon
                    ImageView weatherIcon = new ImageView(MainActivity.this);
                    // Set the image resource based on the weather description
                    // Example: if (description.contains("cloud")) weatherIcon.setImageResource(R.drawable.ic_cloud);
                    // Note: You'll need to implement your own logic here to set the correct icon
                    entryContainer.addView(weatherIcon);

                    // Create a new TextView for the weather description
                    JSONObject weather = listObject.getJSONArray("weather").getJSONObject(0);
                    String description = weather.getString("description");
                    TextView descriptionTextView = new TextView(MainActivity.this);
                    descriptionTextView.setText(description);
                    entryContainer.addView(descriptionTextView);

                    // Create a new TextView for the temperature and add it to the entryContainer
                    JSONObject main = listObject.getJSONObject("main");
                    double temp = main.getDouble("temp") - 273.15; // Convert from Kelvin to Celsius
                    String temperature = String.format(Locale.getDefault(), "%.1f°C", temp);
                    TextView tempTextView = new TextView(MainActivity.this);
                    tempTextView.setText(temperature);
                    entryContainer.addView(tempTextView);

                    // Add the entry container to the main layout
                    weatherLinearLayout.addView(entryContainer);

                    // Add a divider or some space after each entry
                    View divider = new View(MainActivity.this);
                    divider.setLayoutParams(new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            1
                    ));
                    divider.setBackgroundColor(Color.LTGRAY);
                    weatherLinearLayout.addView(divider);
                }

            } catch (Exception e) {
                e.printStackTrace();
                // Handle the error appropriately
            }
        }

    }
}
