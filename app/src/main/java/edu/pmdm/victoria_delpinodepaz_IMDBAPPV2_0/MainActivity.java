package edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.libraries.places.api.Places;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Data.EmptyCallback;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Database.DBSync;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Database.Local.DBManager;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Database.Local.DBhelper;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Persistance.App;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Persistance.AppPersistance;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Persistance.SessionManager;
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

        if(!Places.isInitialized()){
            Places.initialize(getApplicationContext(), "AIzaSyAER7D-uvYpBOG3wZjz9z3AeGulqAci-OU");
        }

        SessionManager.setDateLogin();
        DBManager.updateUserLogin(getApplicationContext());


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
        // Si hay un usuario autenticado, muestra su información
        if(AppPersistance.user != null) {
            txtEmail.setText(AppPersistance.user.getEmail());
            txtUserName.setText(AppPersistance.user.getName());

            // Verifica si se ha almacenado una imagen (BLOB)
            if(AppPersistance.user.getImage() != null) {
                // Convierte el arreglo de bytes a Bitmap
                Bitmap bitmap = BitmapFactory.decodeByteArray(AppPersistance.user.getImage(), 0, AppPersistance.user.getImage().length);
                imgUserPhoto.setImageBitmap(bitmap);
            } else {
                imgUserPhoto.setImageResource(R.drawable.ic_launcher_foreground); // Imagen por defecto
            }
        }


        // Configura el botón de cierre de sesión
        btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, getString(R.string.session_closed), Toast.LENGTH_SHORT).show();
                SessionManager.setDateLogout();
                DBManager.updateUserLogout(getApplicationContext());
                SessionManager.saveSession(new EmptyCallback() {
                    @Override
                    public void onResult(Boolean b) {
                        if(b){
                            Log.d("CICLO_Vida","sessionToken - FINAL" );
                            AppPersistance.user.setUser_id("");
                            AppPersistance.user.setName("");
                            AppPersistance.user.setImage(new byte[0]);
                            AppPersistance.user.setEmail("");
                            AppPersistance.user.setAddress("");
                            AppPersistance.user.setLogin("");
                            AppPersistance.user.setLogout("");
                            AppPersistance.user.setPhone("");


                            FirebaseAuth.getInstance().signOut();
                            LoginManager.getInstance().logOut();
                            restartApp();
                        }else {
                            Toast.makeText(getApplicationContext(), "Fallo en la base de datos",Toast.LENGTH_SHORT).show();
                        }
                    }
                });

               // startActivity(new Intent(MainActivity.this, LoginActivity.class));
                //finish();

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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            // Acción al hacer clic en "Settings"
            Intent intent = new Intent(this, EditUserActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onStart() {
        super.onStart();
        DBSync.syncFavoritesWithSQLite(MainActivity.this);
        DBSync.syncFavoritesWithFirestore();
    }

    @Override
    protected void onResume() {
        super.onResume();
        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView txtUserName = headerView.findViewById(R.id.txtUserName);
        ImageView imgUserPhoto = headerView.findViewById(R.id.imgUserPhoto);

        txtUserName.setText(AppPersistance.user.getName());
        // Convertir byte[] a Bitmap para mostrar la imagen:
        if (AppPersistance.user.getImage() != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(AppPersistance.user.getImage(), 0, AppPersistance.user.getImage().length);
            imgUserPhoto.setImageBitmap(bitmap);
        } else {
            imgUserPhoto.setImageResource(R.drawable.ic_launcher_foreground);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        SessionManager.setDateLogout();
        DBManager.updateUserLogout(this);
        SessionManager.saveSession(new EmptyCallback() {
            @Override
            public void onResult(Boolean b) {
                Log.d("CICLO_Vida","sessionToken - FINAL" );
                finishAffinity();
                System.exit(0);
            }
    });

    }
}