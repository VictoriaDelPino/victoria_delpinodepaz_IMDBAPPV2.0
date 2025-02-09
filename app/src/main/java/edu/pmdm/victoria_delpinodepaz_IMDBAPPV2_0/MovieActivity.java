package edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Movies.Movie;

//Esta actividad muestra los detalle de una película
public class MovieActivity extends AppCompatActivity {
    private static final int REQUEST_CONTACT_PERMISSION = 100;
    private static final int REQUEST_PICK_CONTACT = 101;
    private static final int REQUEST_SMS_PERMISSION = 102;

    private TextView txtTitle, txtReleaseDate, txtDescription, txtRanking;
    private ImageView imgPhoto;
    private String title, releaseDate, description, ranking, urlPhoto;
    private Button btnShare;
    private String selectedPhoneNumber; // Guardar el número del contacto seleccionado
    // Executor para manejar descargas de imágenes en segundo plano
    private final ExecutorService executorServiceMovie = Executors.newFixedThreadPool(4);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_movie);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Vincula las vistas
        btnShare = findViewById(R.id.btnSMS);
        txtTitle = findViewById(R.id.txtMovieTitle);
        txtDescription = findViewById(R.id.txtDescription);
        txtReleaseDate = findViewById(R.id.txtReleaseDate);
        txtRanking = findViewById(R.id.txtRating);
        imgPhoto = findViewById(R.id.imgMoviePhoto);

        // Obtiene el objeto Movie desde el intent
        Movie movie = getIntent().getParcelableExtra("movie");
        if (movie != null) {
            // Asigna datos de la película a variables locales
            title = movie.getTitle();
            description = movie.getDescription();
            releaseDate = movie.getReleaseDate();
            ranking = movie.getRating();
            urlPhoto = movie.getPhoto();

            // Asigna datos a los elementos UI
            txtTitle.setText(title);
            txtDescription.setText(description);
            txtReleaseDate.setText(getString(R.string.released_date) + " " + releaseDate);
            txtRanking.setText(!ranking.equals("") ? getString(R.string.rating) + " " + ranking : "");

            // Imagen por defecto mientras se carga la imagen real
            imgPhoto.setImageResource(R.drawable.ic_launcher_foreground);

            // Descarga imagen en un hilo secundario
            executorServiceMovie.execute(() -> {
                Bitmap bitmap = downloadImage(urlPhoto);
                if (bitmap != null) {
                    imgPhoto.post(() -> imgPhoto.setImageBitmap(bitmap));
                }
            });
        }

        // Configura listener para compartir por SMS
        btnShare.setOnClickListener(view -> checkContactPermission());
    }

    //Verifica si tiene permisos de contactos, si no los tiene los solicita.
    private void checkContactPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_CONTACT_PERMISSION);
        } else {
            openContactPicker();
        }
    }

    //Abre la lista de contactos para que el usuario seleccione uno.
    private void openContactPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(intent, REQUEST_PICK_CONTACT);
    }

    //Maneja la respuesta de la solicitud de permisos y selección de contactos.
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CONTACT_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openContactPicker();
            } else {
                Toast.makeText(this, getString(R.string.permission_requested), Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_SMS_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendSms();
            } else {
                Toast.makeText(this, getString(R.string.sms_permission_denied), Toast.LENGTH_SHORT).show();
            }
        }
    }

    //Maneja el resultado de la selección de un contacto.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_CONTACT && resultCode == RESULT_OK && data != null) {
            Uri contactUri = data.getData();
            if (contactUri != null) {
                retrievePhoneNumber(contactUri);
            }
        }
    }

    //Obtiene el número de teléfono del contacto seleccionado.
    private void retrievePhoneNumber(Uri contactUri) {
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(contactUri, new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            selectedPhoneNumber = cursor.getString(0);
            cursor.close();
        }

        if (selectedPhoneNumber != null) {
            checkSmsPermission();
        } else {
            Toast.makeText(this, "No se pudo obtener el número de teléfono", Toast.LENGTH_SHORT).show();
        }
    }

    /*Verifica si la aplicación tiene permisos para enviar SMS.
      Si no los tiene, los solicita.*/
    private void checkSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, REQUEST_SMS_PERMISSION);
        } else {
            sendSms();
        }
    }

    //Envía un SMS con la información de la película.
    private void sendSms() {
        if (selectedPhoneNumber != null) {
            String message = "¡Hola! Te recomiendo esta película:\n" +
                    "🎬 " + title + "\n" +
                    "📅 Estreno: " + releaseDate + "\n" +
                    "⭐ Puntuación: " + ranking + "\n" +
                    description;

            Intent smsIntent = new Intent(Intent.ACTION_VIEW);
            smsIntent.setData(Uri.parse("sms:" + selectedPhoneNumber));
            smsIntent.putExtra("sms_body", message);
            startActivity(smsIntent);
        } else {
            Toast.makeText(this, "No se ha seleccionado un contacto válido", Toast.LENGTH_SHORT).show();
        }
    }

    //Descarga una imagen desde una URL.
    private Bitmap downloadImage(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream inputStream = connection.getInputStream();
            return BitmapFactory.decodeStream(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
