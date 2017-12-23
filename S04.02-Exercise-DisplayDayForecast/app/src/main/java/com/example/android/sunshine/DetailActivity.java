package com.example.android.sunshine;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class DetailActivity extends AppCompatActivity {

    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";

    private TextView mWeatherDetailTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // COMPLETED (2) Display the weather forecast that was passed from MainActivity
        mWeatherDetailTextView = (TextView) findViewById(R.id.tv_display_forecast_detail);

        Intent intentOpenedThisActivity = getIntent();

        String weatherDetail = "";

        if(intentOpenedThisActivity.hasExtra(Intent.EXTRA_TEXT)) {
            weatherDetail = intentOpenedThisActivity.getStringExtra(Intent.EXTRA_TEXT);
        }

        mWeatherDetailTextView.setText(weatherDetail);
    }
}