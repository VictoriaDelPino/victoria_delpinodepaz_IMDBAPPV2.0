package edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Persistance;

import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Data.EmptyCallback;

public class SessionManager {

    private static String dateLogin;
    private static String dateLogout;

    //Obtiene la fecha y hora actual en formato "dd/MM/yyyy HH:mm:ss"
    private static String getActualDate(){
        LocalDateTime date= LocalDateTime.now();
        DateTimeFormatter formatter= DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String dateFormatted= date.format(formatter);
        return dateFormatted;
    }

    //Establece la fecha y hora de inicio de sesión
    public static void setDateLogin(){
        dateLogin=getActualDate();

    }

    //Establece la fecha y hora de cierre de sesión
    public static void setDateLogout(){
        dateLogout=getActualDate();

    }

    //Devuelve la fecha y hora de inicio de sesión
    public static String getDateLogin() {
        return dateLogin;
    }

    //Devuelve la fecha y hora de cierre de sesión
    public static String getDateLogout() {
        return dateLogout;
    }

    //Guarda los datos de la sesión en Firestore
    public static void saveSession(EmptyCallback callback){
        if (AppPersistance.user == null) {
            Log.w("DBManager", "AppPersistance.user es null; se omite la actualización del logout.");
            return;
        }
        //Crea un mapa con los datos de login y logout
        Map<String,String> data=new HashMap<>();
        data.put("login_time",dateLogin);
        data.put("logout_time", dateLogout);

        FirebaseFirestore fb= FirebaseFirestore.getInstance();
        //Busca al usuario en Firestore con el user_id
        fb.collection("users").whereEqualTo("user_id", AppPersistance.user.getUser_id()).get().addOnCompleteListener(task->{
           if(task.isSuccessful()){
               if(!task.getResult().getDocuments().isEmpty()) {
                   //Obtiene la referencia al documento del usuario en Firestore
                   DocumentReference docRef=fb.collection("users").document(AppPersistance.user.getUser_id());

                   //Agrega los datos de sesión al campo "activity_log"
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
