package edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.Place;
import com.google.gson.Gson;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Data.PhoneCode;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Database.Remote.FirestoreManager;
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
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_GALLERY = 2;
    private Uri selectedImage;
    private String selectedPlaceName;
    private LatLng selectedLatlng;

    private final ActivityResultLauncher<Intent> addressLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Boolean selected= result.getData().getBooleanExtra("selected_address",false);
                    if(selected){
                        String addressName=result.getData().getStringExtra("address_name");
                        float addressLat=result.getData().getFloatExtra("address_lat",0f);
                        float addressLng=result.getData().getFloatExtra("address_lng",0f);
                        selectedPlaceName= addressName;
                        selectedLatlng= new LatLng(addressLat,addressLng);
                        eTxtAddress.setText(selectedPlaceName);

                    }else{
                        Toast.makeText(getApplicationContext(), "No se ha seleccionado ninguna ubicación",Toast.LENGTH_SHORT).show();
                    }
                }
            });


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
        btnAddress=findViewById(R.id.btnAddress);
        btnSave=findViewById(R.id.btnSave);

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


        // Configura el botón para seleccionar imagen
        btnPhoto.setOnClickListener(v -> {
            String[] opciones = {"Cámara", "Galería", "URL Externa"};
            new AlertDialog.Builder(EditUserActivity.this)
                    .setTitle("Seleccionar imagen")
                    .setItems(opciones, (dialog, which) -> {
                        switch (which) {
                            case 0: // Cámara
                                if (ContextCompat.checkSelfPermission(EditUserActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(EditUserActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_IMAGE_CAPTURE);
                                } else {
                                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                    startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
                                }
                                break;
                            case 1: // Galería
                                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                startActivityForResult(galleryIntent, REQUEST_IMAGE_GALLERY);
                                break;
                            case 2: // URL Externa
                                showUrlInputDialog();
                                break;
                        }
                    })
                    .show();
        });

        btnAddress.setOnClickListener(v->{
            Intent intent = new Intent(EditUserActivity.this, SelectAddressActivity.class);
            addressLauncher.launch(intent);
        });

        btnSave.setOnClickListener(v -> {
           /* FirestoreManager.updateUser(
                    AppPersistance.user.getUser_id(),
                    eTxtName.getText().toString(),
                    eTxtAddress.getText().toString(),
                    eTxtPhone.getText().toString(),
                    success -> {
                        if (success) {
                            Log.d("UpdateUser", "Usuario actualizado correctamente");
                        } else {
                            Log.e("UpdateUser", "Error al actualizar el usuario");
                        }
                    }
            );*/
        });


    }

    // Muestra un diálogo para introducir la URL de la imagen
    private void showUrlInputDialog() {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        new AlertDialog.Builder(this)
                .setTitle("Ingresar URL de la imagen")
                .setView(input)
                .setPositiveButton("OK", (dialog, which) -> {
                    String url = input.getText().toString().trim();
                    if (!url.isEmpty()) {
                        new Thread(() -> {
                            Bitmap bitmap = downloadImage(url);
                            runOnUiThread(() -> {
                                if (bitmap != null) {
                                    imgPhoto.setImageBitmap(bitmap);
                                } else {
                                    Toast.makeText(EditUserActivity.this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }).start();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // Método para descargar una imagen desde una URL
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
    // Manejo de resultados de las actividades (cámara y galería)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                // Captura de la cámara
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                imgPhoto.setImageBitmap(imageBitmap);
            } else if (requestCode == REQUEST_IMAGE_GALLERY) {
                // Selección de la galería

                selectedImage = data.getData();
                try {
                    InputStream imageStream = getContentResolver().openInputStream(selectedImage);
                    Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
                    imgPhoto.setImageBitmap(bitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "No se pudo cargar la imagen", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    // Manejo de la respuesta a la solicitud de permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, se inicia la cámara
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
            } else {
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }
}