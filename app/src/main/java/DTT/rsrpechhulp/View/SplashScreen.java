package DTT.rsrpechhulp.View;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;


import DTT.rsrpechhulp.Presenter.Presenter;
import DTT.rsrpechhulp.R;

public class SplashScreen extends AppCompatActivity {

    private Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        //here we pass the dimensions of the screen in order to determine the type of device
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        presenter = new Presenter(displayMetrics.widthPixels, displayMetrics.heightPixels,
                (double) displayMetrics.densityDpi);

        //this makes the splash sleep for 3 seconds before opening the main activity
        Thread thread = new Thread() {

            @Override
            public void run() {
                try {
                    sleep(3000);
                    Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                    intent.putExtra("presenter", presenter);
                    finish();
                    startActivity(intent);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }
}
