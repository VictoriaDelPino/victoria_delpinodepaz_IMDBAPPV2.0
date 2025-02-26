package edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.hbb20.CountryCodePicker;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Database.Local.DBManager;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Persistance.AppPersistance;

public class EditUserActivity extends AppCompatActivity {

    //Declaración de variables para los elementos de la interfaz de usuario
    private EditText eTxtName;
    private EditText eTxtEmail;
    private EditText eTxtPhone;
    private EditText eTxtAddress;
    private Button btnAddress;
    private Button btnPhoto;
    private Button btnSave;
    private ImageView imgPhoto;

    // Constantes para identificar la fuente de la imagen seleccionada
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_GALLERY = 2;

    private Uri selectedImage;
    private String selectedPlaceName;
    private LatLng selectedLatlng;
    private CountryCodePicker ccp;

    // Constante para la solicitud de permisos de ubicación
    private static final int REQUEST_LOCATION_PERMISSION = 1001;

    //Lanzador de actividades para seleccionar una dirección
    private final ActivityResultLauncher<Intent> addressLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Boolean selected= result.getData().getBooleanExtra("selected_address",false);
                    if(selected){
                        //Obtiene la información de la dirección seleccionada
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

        //Enlace de elementos de la interfaz con variables
        eTxtName = findViewById(R.id.eTxtNameEditUser);
        eTxtEmail = findViewById(R.id.eTxtEmailEdit);
        eTxtAddress = findViewById(R.id.eTxtAddressEditUser);
        eTxtPhone = findViewById(R.id.eTxtPhoneEditUser);
        imgPhoto = findViewById(R.id.imgPhotoEditUser);
        btnPhoto = findViewById(R.id.btnPhotoEdit);
        btnAddress = findViewById(R.id.btnAddress);
        btnSave = findViewById(R.id.btnSave);
        ccp=findViewById(R.id.ccp);


        //Cargar los datos del usuario actual en los campos de texto
        eTxtName.setText(AppPersistance.user.getName());
        eTxtEmail.setText(AppPersistance.user.getEmail());
        eTxtAddress.setText(AppPersistance.user.getAddress());
        eTxtPhone.setText(AppPersistance.user.getPhone());

        // Muestra la imagen almacenada
        if (AppPersistance.user.getImage() != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(AppPersistance.user.getImage(), 0, AppPersistance.user.getImage().length);
            imgPhoto.setImageBitmap(bitmap);
        } else {
            imgPhoto.setImageResource(R.drawable.ic_launcher_foreground); // Imagen por defecto
        }

        //Configuración del botón para seleccionar imagen
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
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
                                    if (ContextCompat.checkSelfPermission(EditUserActivity.this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                                        ActivityCompat.requestPermissions(EditUserActivity.this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_IMAGE_GALLERY);
                                    } else {
                                        openGallery();
                                    }
                                } else { // Android 12 o inferior
                                    if (ContextCompat.checkSelfPermission(EditUserActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                        ActivityCompat.requestPermissions(EditUserActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_IMAGE_GALLERY);
                                    } else {
                                        openGallery();
                                    }
                                }

                                break;
                            case 2: // URL Externa
                                showUrlInputDialog();
                                break;
                        }
                    })
                    .show();
        });

        //Configuración del botón para seleccionar una dirección
        btnAddress.setOnClickListener(v -> {
            //Comprueba los permisos de ubicación
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            } else {
                openAddressSelector();
            }
        });

        ccp.registerCarrierNumberEditText(eTxtPhone);

        //Configuración del botón de guardar cambios
        btnSave.setOnClickListener(v -> {
            //Obtiene los valores de los distintos campos
            String newName = eTxtName.getText().toString().trim();
            String newEmail = eTxtEmail.getText().toString().trim();
            String newPhone = eTxtPhone.getText().toString().replaceAll("[^\\d]", "");
            String newAddress = eTxtAddress.getText().toString().trim();

            //Comprueba la validez del telefono
            if (!newPhone.matches("^\\d{9,15}$")) {
                Toast.makeText(this, "El número debe tener entre 9 y 15 dígitos.", Toast.LENGTH_SHORT).show();
                return;
            }
            // Actualiza el objeto usuario actual
            AppPersistance.user.setName(newName);
            AppPersistance.user.setEmail(newEmail);
            AppPersistance.user.setPhone(newPhone);
            AppPersistance.user.setAddress(newAddress);

            // Guarda imagen actualizada en AppPersistance
            imgPhoto.buildDrawingCache();
            Bitmap bitmap = imgPhoto.getDrawingCache();
            if (bitmap != null) {
                AppPersistance.user.setImage(convertBitmapToByteArray(bitmap));
            }
            // Actualiza el usuario en la base de datos
            DBManager.updateUser(EditUserActivity.this);
        });
    }

    //Convierte un Bitmap en un array de bytes para guardarlo en la base de datos
    private byte[] convertBitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        // Comprime la imagen en formato PNG con calidad 100%
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
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
                        //Descarga la imagen en un hilo secundario para evitar bloquear la interface
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

    // Descarga una imagen desde una URL y la devuelve como Bitmap
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

    //Abre la actividad para seleccionar una dirección
    private void openAddressSelector() {
        Intent intent = new Intent(EditUserActivity.this, SelectAddressActivity.class);
        addressLauncher.launch(intent);
    }

    //Abre la galeria
    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, REQUEST_IMAGE_GALLERY);
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
                    //Cargar la imagen desde la galería
                    InputStream imageStream = getContentResolver().openInputStream(selectedImage);
                    Bitmap bitmap = BitmapFactory.decodeStream(imageStream);

                    // Leer los metadatos EXIF para corregir la orientación de la imagen
                    androidx.exifinterface.media.ExifInterface exif = new androidx.exifinterface.media.ExifInterface(imageStream );
                    imageStream.close();

                    int orientation = exif.getAttributeInt(androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION,
                            androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL);
                    int rotation = 0;
                    if (orientation == androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_90) {
                        rotation = 90;
                    } else if (orientation == androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_180) {
                        rotation = 180;
                    } else if (orientation == androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_270) {
                        rotation = 270;
                    }

                    //Si la imagen tiene rotación, la correge antes de mostrarla
                    if (rotation != 0) {
                        android.graphics.Matrix matrix = new android.graphics.Matrix();
                        matrix.postRotate(rotation);
                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                    }

                    imgPhoto.setImageBitmap(bitmap);
                } catch (IOException e) {
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
        if (requestCode == REQUEST_IMAGE_GALLERY) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, abre la galeria
                openGallery();
            } else {
                Toast.makeText(this, "Permiso de galería denegado", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Permiso de ubicación concedido, abrir selector de direcciones
                openAddressSelector();
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }
}