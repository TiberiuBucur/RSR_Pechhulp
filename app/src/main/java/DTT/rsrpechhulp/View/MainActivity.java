package DTT.rsrpechhulp.View;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import DTT.rsrpechhulp.R;

public class MainActivity extends AppCompatActivity implements MainMenu {

    private TextView textView;
    private Button closeBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.infoTV);
        String title = getResources().getString(R.string.title);
        String message = getResources().getString(R.string.message);
        textView.setText(Html.fromHtml("<b>" + title + "</b><br />" + message));

        Button mapBtn = (Button) findViewById(R.id.MapButton);
        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });

        closeBtn = (Button) findViewById(R.id.CloseButton);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.setVisibility(View.GONE);
                closeBtn.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if(id == R.id.infoButton){
            textView.setVisibility(View.VISIBLE);
            closeBtn.setVisibility(View.VISIBLE);
        }
        return super.onOptionsItemSelected(item);
    }

    public void loadPhone(){

    }

    public void loadTablet(){

    }

}
