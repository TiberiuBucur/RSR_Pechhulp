package DTT.rsrpechhulp.Model;

import android.util.DisplayMetrics;

import java.io.Serializable;

public class User implements Serializable {

    //all these fields are usfeul for determining the size of the widgetin an UI
    //as they differ depending on the type of device (phone or tablet)
    private final boolean isTablet;
    private final int screenWidth;
    private final double densityDpi;
    private final int screenHeight;
    private final float widthPercentage;
    private static final float PHONE_WIDTH_PERCENTAGE = 0.9f;
    private static final float TABLET_WIDTH_PERCENTAGE = 0.7f;
    private static final float CALL_DIALOG_WIDTH_PERCENTAGE = 0.65f;

    public User(int screenWidth, int screenHeight, double densityDpi) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.densityDpi = densityDpi;
        isTablet = checkIsTablet();
        if (isTablet) {
            widthPercentage = TABLET_WIDTH_PERCENTAGE;
        } else {
            widthPercentage = PHONE_WIDTH_PERCENTAGE;
        }
    }

    //the width for the privacy dialog, as well as the internet and location dialogs
    //from the map activity
    public int getDialogWidth() {
        return (int) (screenWidth * widthPercentage);
    }

    //similarly for the calling dialog in the map activity
    public int getCallDialogWidth() {
        return (int) (screenWidth * CALL_DIALOG_WIDTH_PERCENTAGE);
    }

    public boolean isTablet() {
        return isTablet;
    }

    //this method calculates the diagonal of the screen in inches and compares it to 7
    //if it is greater than 7, the device is a tablet
    private boolean checkIsTablet() {
        double widthInches = screenWidth / densityDpi;
        double heightInches = screenHeight / densityDpi;
        return Math.sqrt(Math.pow(widthInches, 2) + Math.pow(heightInches, 2)) > 7.0;
        //calculating the screen diagonal in inches and comparing to 7
    }
}
