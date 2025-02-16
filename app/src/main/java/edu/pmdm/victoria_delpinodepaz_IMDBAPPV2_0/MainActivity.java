package edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Persistance.AppPersistance;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.databinding.ActivityMainBinding;

//lase principal de la aplicación que gestiona la navegación y muestra la interfaz principal.
public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    private Button btnLogOut;
    private TextView txtEmail;
    private TextView txtUserName;
    private ImageView imgUserPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);




        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.appBarMain.toolbar);





        // Configuración del Navigation Drawer
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Obtiene la vista del encabezado del Navigation Drawer
        View headerView = navigationView.getHeaderView(0);

        // Busca y asigna los elementos de la interfaz dentro del header
        btnLogOut = headerView.findViewById(R.id.btnLogOut);
        txtEmail = headerView.findViewById(R.id.txtEmail);
        txtUserName = headerView.findViewById(R.id.txtUserName);
        imgUserPhoto = headerView.findViewById(R.id.imgUserPhoto);

        // Si hay un usuario autenticado, muestra su información
        if(AppPersistance.user!=null) {
            txtEmail.setText(AppPersistance.user.getEmail());
            txtUserName.setText(AppPersistance.user.getName());
            // Descarga y establece la imagen del usuario de forma asíncrona
            new Thread(() -> {
                Bitmap bitmap = downloadImage(AppPersistance.user.getImage());
                runOnUiThread(() -> {
                    if (bitmap != null) {
                        imgUserPhoto.setImageBitmap(bitmap);
                    } else {
                        imgUserPhoto.setImageResource(R.drawable.ic_launcher_foreground); // Imagen por defecto
                    }
                });
            }).start();
        }

        // Configura el botón de cierre de sesión
        btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, getString(R.string.session_closed), Toast.LENGTH_SHORT).show();
                FirebaseAuth.getInstance().signOut();
                LoginManager.getInstance().logOut();
               // startActivity(new Intent(MainActivity.this, LoginActivity.class));
                //finish();
                restartApp();
            }
        });
    }

    // Método para reiniciar la aplicación
    public void restartApp() {
        // Crear un Intent para lanzar la actividad principal
        Intent intent = new Intent(MainActivity.this, LauncherActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        // Iniciar la actividad
        startActivity(intent);
        // Finalizar la actividad actual
        finish();

    }

    /*Método para descargar una imagen desde una URL.
    Se utiliza para cargar la foto de perfil del usuario.*/
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

}