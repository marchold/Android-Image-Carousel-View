package com.example.myapplication2.app;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity {

    private ImageCarouselView.Carousel images;
    private ImageCarouselView pager;
    int scroll=0;
    private TextView text;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pager = (ImageCarouselView)findViewById(R.id.pager);
        text = (TextView)findViewById(R.id.text);
        pager.setPageChangeListener(new ImageCarouselView.OnPageChangeListener() {
            @Override
            public void onNewPage(int newPage) {
                text.setText("On Page "+newPage);
            }
        });

    }



}
