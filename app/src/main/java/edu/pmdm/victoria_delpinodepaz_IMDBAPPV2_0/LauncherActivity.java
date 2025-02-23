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

        // Obtener instancia de SharedPreferences
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean isFirstRun = prefs.getBoolean("isFirstRun", true);

        if (isFirstRun) {
            // Primera vez que se ejecuta después de una instalación → Cerrar sesión
            FirebaseAuth.getInstance().signOut();

            // Marcar que la app ya se ha ejecutado
            prefs.edit().putBoolean("isFirstRun", false).apply();
        }


        // Inicializa la base de datos local
        DBManager.init(this);

        // Obtiene la instancia de Firebase Auth y el usuario actual
        FirebaseUser fbUser= FirebaseAuth.getInstance().getCurrentUser();
        Context context= this;
        Handler handler= new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Comprobacion de que el usuario tiene los datos en firestore
                if (fbUser != null) {
                        FirestoreManager.getUser(fbUser.getEmail(),resultUser ->{
                            if (resultUser != null) {
                                AppPersistance.user = DBManager.getOrCreateUser( context,  fbUser, resultUser.getUser_id());
                                Log.d("AppPersis",AppPersistance.user.getEmail().toString());
                                Intent intent = new Intent(context, MainActivity.class);
                                startActivity(intent);
                            } else {
                                Log.e("FirestoreError", "No se pudo obtener el usuario desde Firestore");
                                startActivity(new Intent(context, LoginActivity.class));
                            }
                        });


                } else {
                    startActivity(new Intent(context, LoginActivity.class));
                    finish();
                    Log.w("FirebaseAuth", "El usuario no ha iniciado sesión.");
                }
            }
        },1500);

    }
}