package edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Persistance;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Data.EmptyCallback;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Database.Local.DBManager;

// Clase App que extiende de Application y gestiona el ciclo de vida global de la aplicación

public class App extends Application {

    // Contador de actividades activas
    private int activityCount=0;

    @Override
    public void onCreate() {
        super.onCreate();
        // Inicializa el SDK de Facebook
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        //  Registra los callbacks del ciclo de vida de la aplicación
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
               // Log.d("CICLO_Vida","OnCreate" );
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
                if(activityCount==0){
                    //Si es la primera actividad que se inicia, verifica si el usuario está autenticado en Firebase
                    FirebaseUser fbUser= FirebaseAuth.getInstance().getCurrentUser();
                    if(fbUser!=null){
                        //Establece la fecha de inicio de sesión y actualiza el estado del usuario en la base de datos local
                        SessionManager.setDateLogin();
                        DBManager.updateUserLogin(getApplicationContext());
                        Log.d("CICLO_Vida","sessionToken - INICIO" );
                    }

                }
                activityCount++;
                //Log.d("CICLO_Vida","OnStart" );
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
               // Log.d("CICLO_Vida","OnResume" );
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {
               // Log.d("CICLO_Vida","OnPause" );
            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {
                //Decrementa el contador de actividades activas
                activityCount--;
                //Si no hay más actividades activas, establece la fecha de cierre de sesión y guarda la sesión
                if(activityCount==0){
                    SessionManager.setDateLogout();
                    DBManager.updateUserLogout(getApplicationContext());
                    SessionManager.saveSession(new EmptyCallback() {
                        @Override
                        public void onResult(Boolean b) {
                            Log.d("CICLO_Vida","sessionToken - FINAL" );
                        }
                    });

                }

              //  Log.d("CICLO_Vida","OnStop" );
            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {

            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
               // Log.d("CICLO_Vida","OnDestroy" );
            }
        });
    }
}
