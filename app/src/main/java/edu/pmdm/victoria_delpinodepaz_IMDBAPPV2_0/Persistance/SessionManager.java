package edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Persistance;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Data.EmptyCallback;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Data.User;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Database.Local.DBhelper;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Database.Remote.FirestoreManager;

public class SessionManager {

    private static String dateLogin;
    private static String dateLogout;

    private static String getActualDate(){
        LocalDateTime date= LocalDateTime.now();
        DateTimeFormatter formatter= DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String dateFormatted= date.format(formatter);
        return dateFormatted;
    }

    public static void setDateLogin(){
        dateLogin=getActualDate();

    }

    public static void setDateLogout(){
        dateLogout=getActualDate();

    }

    public static String getDateLogin() {
        return dateLogin;
    }

    public static String getDateLogout() {
        return dateLogout;
    }

    public static void saveSession(EmptyCallback callback){
        Map<String,String> data=new HashMap<>();
        data.put("login_time",dateLogin);
        data.put("logout_time", dateLogout);

        FirebaseFirestore fb= FirebaseFirestore.getInstance();
        fb.collection("users").whereEqualTo("user_id", AppPersistance.user.getUser_id()).get().addOnCompleteListener(task->{
           if(task.isSuccessful()){
               if(!task.getResult().getDocuments().isEmpty()) {


                   DocumentReference docRef=fb.collection("users").document(AppPersistance.user.getUser_id());

                       docRef.update("activity_log", FieldValue.arrayUnion(data)).addOnSuccessListener(aVoid->
                               {Log.d("CICLO_vida","sesion guardada exitosamente");
                                   dateLogin="";
                                   dateLogout="";
                                    callback.onResult(true);
                               }


                       ).addOnFailureListener(e->{
                           Log.d("CICLO_vida","error al guardar la sesion");
                           callback.onResult(false);
                       });


               }
           }
        });
    }


}
