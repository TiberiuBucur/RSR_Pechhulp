package DTT.rsrpechhulp.View;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import DTT.rsrpechhulp.R;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private final View window;

    public CustomInfoWindowAdapter(Context context) {
        window = LayoutInflater.from(context).inflate(R.layout.image_window, null);
    }

    private void setWindowText(Marker marker, View view) {
        String snippet = marker.getSnippet();
        TextView addressTV = (TextView) view.findViewById(R.id.info_address);
        addressTV.setText(snippet);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        setWindowText(marker, window);
        return window;
    }

    @Override
    public View getInfoContents(Marker marker) {
        setWindowText(marker, window);
        return window;
    }
}
