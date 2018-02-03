/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.sunshine;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.android.sunshine.utilities.SunshineDateUtils;
import com.example.android.sunshine.utilities.SunshineWeatherUtils;

import static com.example.android.sunshine.data.WeatherContract.WeatherEntry.COLUMN_DATE;
import static com.example.android.sunshine.data.WeatherContract.WeatherEntry.COLUMN_DEGREES;
import static com.example.android.sunshine.data.WeatherContract.WeatherEntry.COLUMN_HUMIDITY;
import static com.example.android.sunshine.data.WeatherContract.WeatherEntry.COLUMN_MAX_TEMP;
import static com.example.android.sunshine.data.WeatherContract.WeatherEntry.COLUMN_MIN_TEMP;
import static com.example.android.sunshine.data.WeatherContract.WeatherEntry.COLUMN_PRESSURE;
import static com.example.android.sunshine.data.WeatherContract.WeatherEntry.COLUMN_WEATHER_ID;
import static com.example.android.sunshine.data.WeatherContract.WeatherEntry.COLUMN_WIND_SPEED;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
//      COMPLETED (21) Implement LoaderManager.LoaderCallbacks<Cursor>

    /*
     * In this Activity, you can share the selected day's forecast. No social sharing is complete
     * without using a hashtag. #BeTogetherNotTheSame
     */
    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";

//  COMPLETED (18) Create a String array containing the names of the desired data columns from our ContentProvider
    final static String[] WEATHER_DETAIL_PROJECTION = {COLUMN_DATE, COLUMN_WEATHER_ID, COLUMN_MAX_TEMP, COLUMN_MIN_TEMP,
                                    COLUMN_HUMIDITY, COLUMN_WIND_SPEED, COLUMN_DEGREES, COLUMN_PRESSURE};

//  COMPLETED (19) Create constant int values representing each column name's position above
    final static int INDEX_DATE = 0;
    final static int INDEX_WEATHER_ID = 1;
    final static int INDEX_MAX_TEMP = 2;
    final static int INDEX_MIN_TEMP = 3;
    final static int INDEX_HUMIDITY = 4;
    final static int INDEX_WIND_SPEED = 5;
    final static int INDEX_DEGREE = 6;
    final static int INDEX_PRESSURE = 7;

//  COMPLETED (20) Create a constant int to identify our loader used in DetailActivity
    private static final int ID_DETAIL_LOADER = 45;

    /* A summary of the forecast that can be shared by clicking the share button in the ActionBar */
    private String mForecastSummary;

//  COMPLETED (15) Declare a private Uri field called mUri
    private Uri mUri;

//  COMPLETED (10) Remove the mWeatherDisplay TextView declaration

//  COMPLETED (11) Declare TextViews for the date, description, high, low, humidity, wind, and pressure
    private TextView dateTextView;
    private TextView descriptionTextView;
    private TextView highTempTextView;
    private TextView lowTempTextView;
    private TextView humidityTextView;
    private TextView windTextView;
    private TextView pressureTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
//      COMPLETED (12) Remove mWeatherDisplay TextView

//      COMPLETED (13) Find each of the TextViews by ID
        dateTextView        = (TextView) findViewById(R.id.tv_date);
        descriptionTextView = (TextView) findViewById(R.id.tv_description);
        highTempTextView    = (TextView) findViewById(R.id.tv_high_temp);
        lowTempTextView     = (TextView) findViewById(R.id.tv_low_temp);
        humidityTextView    = (TextView) findViewById(R.id.tv_humidity);
        windTextView        = (TextView) findViewById(R.id.tv_wind_speed);
        pressureTextView    = (TextView) findViewById(R.id.tv_pressure);

//      COMPLETED (14) Remove the code that checks for extra text
        Intent intentThatStartedThisActivity = getIntent();

//      COMPLETED (16) Use getData to get a reference to the URI passed with this Activity's Intent
//      COMPLETED (17) Throw a NullPointerException if that URI is null
        if (intentThatStartedThisActivity != null) {
            mUri = intentThatStartedThisActivity.getData();
        } else {
            throw new NullPointerException("Null uri");
        }
//
//      COMPLETED (35) Initialize the loader for DetailActivity
        getSupportLoaderManager().initLoader(ID_DETAIL_LOADER, null, this);
    }

    /**
     * This is where we inflate and set up the menu for this Activity.
     *
     * @param menu The options menu in which you place your items.
     *
     * @return You must return true for the menu to be displayed;
     *         if you return false it will not be shown.
     *
     * @see #onPrepareOptionsMenu
     * @see #onOptionsItemSelected
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        MenuInflater inflater = getMenuInflater();
        /* Use the inflater's inflate method to inflate our menu layout to this menu */
        inflater.inflate(R.menu.detail, menu);
        /* Return true so that the menu is displayed in the Toolbar */
        return true;
    }

    /**
     * Callback invoked when a menu item was selected from this Activity's menu. Android will
     * automatically handle clicks on the "up" button for us so long as we have specified
     * DetailActivity's parent Activity in the AndroidManifest.
     *
     * @param item The menu item that was selected by the user
     *
     * @return true if you handle the menu click here, false otherwise
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /* Get the ID of the clicked item */
        int id = item.getItemId();

        /* Settings menu item clicked */
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        /* Share menu item clicked */
        if (id == R.id.action_share) {
            Intent shareIntent = createShareForecastIntent();
            startActivity(shareIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Uses the ShareCompat Intent builder to create our Forecast intent for sharing.  All we need
     * to do is set the type, text and the NEW_DOCUMENT flag so it treats our share as a new task.
     * See: http://developer.android.com/guide/components/tasks-and-back-stack.html for more info.
     *
     * @return the Intent to use to share our weather forecast
     */
    private Intent createShareForecastIntent() {
        Intent shareIntent = ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setText(mForecastSummary + FORECAST_SHARE_HASHTAG)
                .getIntent();
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        return shareIntent;
    }

//  COMPLETED (22) Override onCreateLoader
//          COMPLETED (23) If the loader requested is our detail loader, return the appropriate CursorLoader
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        switch (id) {

            case ID_DETAIL_LOADER:

                return new CursorLoader(this,
                        mUri,
                        WEATHER_DETAIL_PROJECTION,
                        null,
                        null,
                        null);

            default:
                throw new RuntimeException("Loader Not Implemented: " + id);
        }
    }

//  COMPLETED (24) Override onLoadFinished
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //      COMPLETED (25) Check before doing anything that the Cursor has valid data
        /*
         * Before we bind the data to the UI that will display that data, we need to check the
         * cursor to make sure we have the results that we are expecting. In order to do that, we
         * check to make sure the cursor is not null and then we call moveToFirst on the cursor.
         * Although it may not seem obvious at first, moveToFirst will return true if it contains
         * a valid first row of data.
         *
         * If we have valid data, we want to continue on to bind that data to the UI. If we don't
         * have any data to bind, we just return from this method.
         */
        boolean cursorHasValidData = false;
        if (data != null && data.moveToFirst()) {
            /* We have valid data, continue on to bind the data to the UI */
            cursorHasValidData = true;
        }

        if (!cursorHasValidData) {
            /* No data to display, simply return and do nothing */
            return;
        }

//      COMPLETED (26) Display a readable data string
        /****************
         * Weather Date *
         ****************/
        /*
         * Read the date from the cursor. It is important to note that the date from the cursor
         * is the same date from the weather SQL table. The date that is stored is a GMT
         * representation at midnight of the date when the weather information was loaded for.
         *
         * When displaying this date, one must add the GMT offset (in milliseconds) to acquire
         * the date representation for the local date in local time.
         * SunshineDateUtils#getFriendlyDateString takes care of this for us.
         */
        long localDateMidnightGmt = data.getLong(INDEX_DATE);
        String dateText = SunshineDateUtils.getFriendlyDateString(this, localDateMidnightGmt, true);

        dateTextView.setText(dateText);

//      COMPLETED (27) Display the weather description (using SunshineWeatherUtils)
        /***********************
         * Weather Description *
         ***********************/
        /* Read weather condition ID from the cursor (ID provided by Open Weather Map) */
        int weatherId = data.getInt(INDEX_WEATHER_ID);
        /* Use the weatherId to obtain the proper description */
        String description = SunshineWeatherUtils.getStringForWeatherCondition(this, weatherId);

        /* Set the text */
        descriptionTextView.setText(description);

//      COMPLETED (28) Display the high temperature
        /**************************
         * High (max) temperature *
         **************************/
        /* Read high temperature from the cursor (in degrees celsius) */
        double highInCelsius = data.getDouble(INDEX_MAX_TEMP);
        /*
         * If the user's preference for weather is fahrenheit, formatTemperature will convert
         * the temperature. This method will also append either °C or °F to the temperature
         * String.
         */
        String highString = SunshineWeatherUtils.formatTemperature(this, highInCelsius);

        /* Set the text */
        highTempTextView.setText(highString);

//      COMPLETED (29) Display the low temperature
        /*************************
         * Low (min) temperature *
         *************************/
        /* Read low temperature from the cursor (in degrees celsius) */
        double lowInCelsius = data.getDouble(INDEX_MIN_TEMP);
        /*
         * If the user's preference for weather is fahrenheit, formatTemperature will convert
         * the temperature. This method will also append either °C or °F to the temperature
         * String.
         */
        String lowString = SunshineWeatherUtils.formatTemperature(this, lowInCelsius);

        /* Set the text */
        lowTempTextView.setText(lowString);

//      COMPLETED (30) Display the humidity
        /************
         * Humidity *
         ************/
        /* Read humidity from the cursor */
        float humidity = data.getFloat(INDEX_HUMIDITY);
        String humidityString = getString(R.string.format_humidity, humidity);

        /* Set the text */
        humidityTextView.setText(humidityString);

//      COMPLETED (31) Display the wind speed and direction
        /****************************
         * Wind speed and direction *
         ****************************/
        /* Read wind speed (in MPH) and direction (in compass degrees) from the cursor  */
        float windSpeed = data.getFloat(INDEX_WIND_SPEED);
        float windDirection = data.getFloat(INDEX_DEGREE);
        String windString = SunshineWeatherUtils.getFormattedWind(this, windSpeed, windDirection);

        /* Set the text */
        windTextView.setText(windString);

//      COMPLETED (32) Display the pressure
        /************
         * Pressure *
         ************/
        /* Read pressure from the cursor */
        float pressure = data.getFloat(INDEX_PRESSURE);

        /*
         * Format the pressure text using string resources. The reason we directly access
         * resources using getString rather than using a method from SunshineWeatherUtils as
         * we have for other data displayed in this Activity is because there is no
         * additional logic that needs to be considered in order to properly display the
         * pressure.
         */
        String pressureString = getString(R.string.format_pressure, pressure);

        /* Set the text */
        pressureTextView.setText(pressureString);

//      COMPLETED (33) Store a forecast summary in mForecastSummary
        /* Store the forecast summary String in our forecast summary field to share later */
        mForecastSummary = String.format("%s - %s - %s/%s",
                dateText, description, highString, lowString);
    }

    //  COMPLETED (34) Override onLoaderReset, but don't do anything in it yet
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

}