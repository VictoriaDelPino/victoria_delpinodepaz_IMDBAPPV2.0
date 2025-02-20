package edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Data.PhoneCode;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Persistance.AppPersistance;

public class EditUserActivity extends AppCompatActivity {

    private EditText eTxtName;
    private EditText eTxtEmail;
    private EditText eTxtPhone;
    private EditText eTxtAddress;
    private Button btnAddress;
    private Button btnPhoto;
    private Button btnSave;
    private ImageView imgPhoto;
    private Spinner spinnerPhoneCodes;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_user);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        eTxtName=findViewById(R.id.eTxtNameEditUser);
        eTxtEmail=findViewById(R.id.eTxtEmailEdit);
        eTxtAddress=findViewById(R.id.eTxtAddressEditUser);
        eTxtPhone=findViewById(R.id.eTxtPhoneEditUser);
        imgPhoto=findViewById(R.id.imgPhotoEditUser);
        btnPhoto=findViewById(R.id.btnPhotoEdit);

        eTxtName.setText(AppPersistance.user.getName());
        eTxtEmail.setText(AppPersistance.user.getEmail());
        eTxtAddress.setText(AppPersistance.user.getAddress());
        eTxtPhone.setText(AppPersistance.user.getPhone());
        new Thread(() -> {
            Bitmap bitmap = downloadImage(AppPersistance.user.getImage());
            runOnUiThread(() -> {
                if (bitmap != null) {
                    imgPhoto.setImageBitmap(bitmap);
                } else {
                    imgPhoto.setImageResource(R.drawable.ic_launcher_foreground); // Imagen por defecto
                }
            });
        }).start();

        InputStream inputStream = getResources().openRawResource(R.raw.phone_codes);
        InputStreamReader reader = new InputStreamReader(inputStream);
        Gson gson = new Gson();
        PhoneCode[] phoneCodesArray = gson.fromJson(reader, PhoneCode[].class);
        List<PhoneCode> phoneCodesList = Arrays.asList(phoneCodesArray);
        Spinner spinnerPhoneCodes = findViewById(R.id.spinnerPhone);
        PhoneCodeAdapter adapter = new PhoneCodeAdapter(this, phoneCodesList);
        spinnerPhoneCodes.setAdapter(adapter);
// Suponiendo que 'phoneCodesList' es la lista de PhoneCode
        int defaultIndex = 0;
        for (int i = 0; i < phoneCodesList.size(); i++) {
            // Puedes comparar por nombre o por código ISO, según prefieras
            if (phoneCodesList.get(i).getName().equalsIgnoreCase("España")) {
                defaultIndex = i;
                break;
            }
        }

// Asigna la selección por defecto en el Spinner
        spinnerPhoneCodes.setSelection(defaultIndex);






    }

    private Bitmap downloadImage(String urlString) {
        Bitmap bitmap = null;
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            bitmap = BitmapFactory.decodeStream(input);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }
}