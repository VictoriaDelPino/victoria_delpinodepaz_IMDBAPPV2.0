package edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Database.Local.DBManager;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Database.Remote.FirestoreManager;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Persistance.AppPersistance;

//Primera actividad de la aplicación
public class LauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_launcher);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Obtiene la instancia de SharedPreferences para verificar si es la primera ejecución
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean isFirstRun = prefs.getBoolean("isFirstRun", true);

        if (isFirstRun) {
            // Si es la primera vez que se ejecuta después de una instalación cierra sesión
            FirebaseAuth.getInstance().signOut();

            // Guarda en las preferencias que la app ya se ha ejecutado al menos una vez
            prefs.edit().putBoolean("isFirstRun", false).apply();
        }


        // Inicializa la base de datos local
        DBManager.init(this);

        // Obtiene la instancia de FirebaseAuth para verificar si hay un usuario autenticado
        FirebaseUser fbUser= FirebaseAuth.getInstance().getCurrentUser();
        Context context= this;

        //Usa un Handler para retrasar la ejecución del siguiente bloque de código 1.5 segundos
        //y mientras se muestra la pantalla de "carga"
        Handler handler= new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Comprobacion de que el usuario tiene los datos en firestore
                if (fbUser != null) {
                    //Busca los datos del usuario en Firestore para asegurarse de que existen
                        FirestoreManager.getUser(fbUser.getEmail(),resultUser ->{
                            if (resultUser != null) {
                                //Si el usuario existe en Firestore, lo almacena en la persistencia de la app
                                AppPersistance.user = DBManager.getOrCreateUser( context,  fbUser, resultUser.getUser_id());
                                Log.d("AppPersis",AppPersistance.user.getEmail().toString());

                                //Inicia la actividad principal de la aplicación
                                Intent intent = new Intent(context, MainActivity.class);
                                startActivity(intent);
                            } else {
                                //Si no se encuentra el usuario en Firestore, redirige al LoginActivity
                                Log.e("FirestoreError", "No se pudo obtener el usuario desde Firestore");
                                startActivity(new Intent(context, LoginActivity.class));
                            }
                        });


                } else {

                    //Si no hay usuario autenticado, redirige directamente al LoginActivity
                    startActivity(new Intent(context, LoginActivity.class));
                    finish();
                    Log.w("FirebaseAuth", "El usuario no ha iniciado sesión.");
                }
            }
        },500);

    }


}