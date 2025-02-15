package edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Persistance;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Data.User;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Database.DBManager;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Database.FirestoreManager;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.MainActivity;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

    }
}
