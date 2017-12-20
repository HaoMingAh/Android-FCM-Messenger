package com.jis.enigmamessenger.common;

import android.content.Intent;
import android.os.Bundle;

import com.jis.enigmamessenger.main.SplashActivity;


/**
 * Created by HGS on 12/11/2015.
 */
public class RestartActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        if(!Common.g_isAppRunning){

            Intent goIntro = new Intent(this, SplashActivity.class);
            startActivity(goIntro);
        }

        finish();
    }


}
