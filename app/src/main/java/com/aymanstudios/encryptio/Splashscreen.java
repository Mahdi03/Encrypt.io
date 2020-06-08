package com.aymanstudios.encryptio;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class Splashscreen extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
        Similar to a setTimeout where this screen will be the main screen until
        5 seconds pass and the user will be taken to the next screen
         */
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //Creates a new Intent to call the MainActivity.java
                Intent mainActivityIntent = new Intent(Splashscreen.this, MainActivity.class);
                startActivity(mainActivityIntent);
                //This call ends the Splashscreen.java process so that when you press the back button it will not be restored
                finish();
            }
        }, 2000);
    }
}

