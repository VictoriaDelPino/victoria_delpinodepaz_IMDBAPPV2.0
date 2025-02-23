package edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Persistance;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Data.EmptyCallback;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Database.Local.DBManager;


public class App extends Application {

    private int activityCount=0;

    @Override
    public void onCreate() {
        super.onCreate();
        //Log.d("CICLO_Vida","OnAppCreate" );
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
               // Log.d("CICLO_Vida","OnCreate" );
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
                if(activityCount==0){
                    FirebaseUser fbUser= FirebaseAuth.getInstance().getCurrentUser();
                    if(fbUser!=null){
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
                activityCount--;
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
