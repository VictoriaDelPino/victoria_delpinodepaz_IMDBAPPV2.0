package edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0;

import android.content.Context;
import android.content.Intent;
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
                    FirestoreManager.createUser(res->{
                        FirestoreManager.getUser(fbUser.getEmail(),resultUser ->{
                            if (resultUser!=null){
                                AppPersistance.user=resultUser;
                            }
                            Intent intent=new Intent(context, MainActivity.class);
                            startActivity(intent);
                        });
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