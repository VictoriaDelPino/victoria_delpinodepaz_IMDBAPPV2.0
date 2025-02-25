package edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.gms.maps.OnMapReadyCallback;

import java.util.Arrays;
import java.util.List;

public class SelectAddressActivity extends AppCompatActivity implements OnMapReadyCallback{

    private Button btnConfirmAddress;
    private Button btnSlectAddress;
    private Place actualPlace;
    private MapView mapView;
    private GoogleMap google_Map;
    private TextView txtActualUb;
    private Boolean selectedYet=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_select_address);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Context context=getApplicationContext();

        // Inicializa de elementos de la interfaz
        btnSlectAddress=findViewById(R.id.btnSelectAddress);
        txtActualUb=findViewById(R.id.txtActualUb);
        mapView=findViewById(R.id.mapView);

        // Restaura estado del mapa
        Bundle mapBundle= null;
        if(savedInstanceState!=null){
            mapBundle=savedInstanceState.getBundle("map");
        }
        mapView.onCreate(mapBundle);
        mapView.getMapAsync(this);

        // Configura el botón para seleccionar dirección mediante Google Places
        btnSlectAddress.setOnClickListener(v->{
            List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME,Place.Field.LAT_LNG);
            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                    .build(context);
            startActivityForResult(intent, 1009);
        });

        // Configura el botón de confirmación de dirección
        btnConfirmAddress=findViewById(R.id.btnConfirmAddress);
        btnConfirmAddress.setOnClickListener(v->{
            Intent resultIntent = new Intent();
            resultIntent.putExtra("selected_address", selectedYet);
            if(selectedYet) {
                resultIntent.putExtra("address_name", actualPlace.getDisplayName());
                resultIntent.putExtra("address_lat", actualPlace.getLocation().latitude);
                resultIntent.putExtra("address_lng", actualPlace.getLocation().longitude);
            }
            setResult(RESULT_OK, resultIntent);
            finish();
        });



    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Maneja el resultado de la selección de dirección
        if(requestCode==1009 && data!=null){
            if(resultCode==RESULT_OK){
                actualPlace= Autocomplete.getPlaceFromIntent(data);
                txtActualUb.setText(actualPlace.getDisplayName()+", ");
                google_Map.moveCamera(CameraUpdateFactory.newLatLngZoom(actualPlace.getLocation()
                        , 10));
                if(selectedYet==false){
                    selectedYet=true;
                }
            }
        }
    }

    // Método llamado cuando el mapa está listo
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        google_Map=googleMap;
        LatLng ub= new LatLng(40.4168,-3.7038);
        google_Map.moveCamera(CameraUpdateFactory.newLatLngZoom(ub,15));
    }

    // Guarda estado del mapa
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle mapViewBundle = outState.getBundle("map");
        if(mapViewBundle==null){
            mapViewBundle= new Bundle();
            outState.putBundle("map", mapViewBundle);
        }
        mapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
}