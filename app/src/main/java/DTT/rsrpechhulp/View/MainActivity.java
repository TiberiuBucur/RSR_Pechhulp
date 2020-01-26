package DTT.rsrpechhulp.View;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import DTT.rsrpechhulp.Presenter.Presenter;
import DTT.rsrpechhulp.R;

public class MainActivity extends AppCompatActivity implements UI {

    private Presenter presenter;
    private int menuId; // to change between phone and tablet menu
    // allows flexibility for future development of the UIs
    //alternatively I could have deleted the infoButton from the menu
    // in the loadTablet method through an OnPrepareOptionsMenu method
    private int dialogWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button mapBtn = (Button) findViewById(R.id.map_button);//the button which takes us
        //to the map activity
        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                intent.putExtra("presenter", presenter);
                startActivity(intent);
            }
        });

        presenter = (Presenter) getIntent().getSerializableExtra("presenter");
        dialogWidth = presenter.getDialogWidth();
        presenter.loadUI(this);
    }

    //this method inflates (integrates) the menu in the activity
    //useful for customizing the toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(menuId, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //this adds an on click listener to the info button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.info_button) {
            showDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    //shows the dialog with the privacy terms
    private void showDialog() {
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.info_layout);
        Window window = dialog.getWindow();
        window.setLayout(dialogWidth, WindowManager.LayoutParams.WRAP_CONTENT);//setting the
        // dimensions of the dialog, depending on the type of device

        TextView infoMessage = (TextView) dialog.findViewById(R.id.messageTV);
        infoMessage.setClickable(true);
        infoMessage.setMovementMethod(LinkMovementMethod.getInstance());

        Button closeBtn = (Button) dialog.findViewById(R.id.close_button);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    public void loadPhone() {
        menuId = R.menu.main;
    }

    public void loadTablet() {
        menuId = R.menu.main2;
        Button infoBtn = (Button) findViewById(R.id.info_button);//this button is only for tablets
        infoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });
    }

}
