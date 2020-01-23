package DTT.rsrpechhulp.Model;

import android.util.DisplayMetrics;

import java.io.Serializable;

public class User implements Serializable {

    private boolean isTablet;
    private int screenWidth;
    private double densityDpi;
    private int screenHeight;
    private float widthPercentage;
    private static final float PHONE_WIDTH_PERCENTAGE = 0.9f;
    private static final float TABLET_WIDTH_PERCENTAGE = 0.7f;
    private static final float CALL_DIALOG_WIDTH_PERCENTAGE = 0.65f;

    public User(int screenWidth, int screenHeight, double densityDpi){
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.densityDpi = densityDpi;
        isTablet = checkIsTablet();
        if(isTablet) {
            widthPercentage = TABLET_WIDTH_PERCENTAGE;
        } else {
            widthPercentage = PHONE_WIDTH_PERCENTAGE;
        }
    }

    private boolean checkIsTablet() {
        double widthInches = screenWidth / densityDpi;
        double heightInches = screenHeight / densityDpi;
        return Math.sqrt(Math.pow(widthInches, 2) + Math.pow(heightInches, 2)) > 7.0;
        //calculating the screen diagonal in inches and comparing to 7
    }

    public int getDialogWidth(){
        return (int) (screenWidth * widthPercentage);
    }

    public int getCallDialogWidth() {
        return (int) (screenWidth * CALL_DIALOG_WIDTH_PERCENTAGE);
    }

    public boolean isTablet(){
        return isTablet;
    }
}
