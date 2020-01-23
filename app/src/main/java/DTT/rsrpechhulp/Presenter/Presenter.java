package DTT.rsrpechhulp.Presenter;

import android.util.DisplayMetrics;

import java.io.Serializable;

import DTT.rsrpechhulp.Model.User;
import DTT.rsrpechhulp.View.UI;

public class Presenter implements Serializable {

    private User user;

    public Presenter(int screenWidth, int screenHeight, double densityDpi) {
        user = new User(screenWidth, screenHeight, densityDpi);
    }

    public void loadUI(UI menu) {
        if (user.isTablet()) {
            menu.loadTablet();
        } else {
            menu.loadPhone();
        }
    }

    public int getDialogWidth() {
        return user.getDialogWidth();
    }

    public int getCallDialogWidth() {
        return user.getCallDialogWidth();
    }
}
