package com.example.myapplication2.app;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;


public class MainActivity extends ActionBarActivity {

    private ImageCarouselView.Carousel images;
    private ImageCarouselView pager;
    int scroll=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pager = (ImageCarouselView)findViewById(R.id.pager);


    }



}
