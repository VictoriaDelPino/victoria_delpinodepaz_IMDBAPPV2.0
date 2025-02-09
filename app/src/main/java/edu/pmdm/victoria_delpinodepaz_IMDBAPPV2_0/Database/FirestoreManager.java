package edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Database;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FirestoreManager {

    private static final ExecutorService executorService = Executors.newFixedThreadPool(5);

    private static FirebaseFirestore getInstace(){
        FirebaseFirestore dbFirestore= FirebaseFirestore.getInstance();
        return dbFirestore;
    }

    public static void createUser(){
        CountDownLatch countDownLatchFirebase= new CountDownLatch(1);
        executorService.execute(() -> {
        //implementar tambien un ejecutor
        String email=FirebaseAuth.getInstance().getCurrentUser().getEmail();
        FirebaseFirestore db= getInstace();

        //Primer paso: comprobacion de que el usuario no existe en la base de datos de Firestore
        db.collection("users").whereEqualTo("email",email).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                QuerySnapshot documents= task.getResult();
                if(documents.isEmpty()){
                    //Si el usuario no existe lo creamos
                    FirebaseAuth mAuth;
                    FirebaseUser currentUser;
                    mAuth = FirebaseAuth.getInstance();
                    currentUser = mAuth.getCurrentUser();
                    if(currentUser!=null) {
                        Map <String,String> data=new HashMap<>();
                        data.put("email", currentUser.getEmail());
                        data.put("name", currentUser.getDisplayName());
                        data.put("user_id", "");
                        data.put("address","");
                        data.put("phone", currentUser.getPhoneNumber());
                        data.put("image", currentUser.getPhotoUrl().toString());
                        data.put("activity_log","");
                        db.collection("users").add(data)
                                .addOnSuccessListener(documentReference -> {
                                    //Usuario añadido con exito a la coleccion users
                                    Map <String,Object> dataUpdate= new HashMap<>();
                                    dataUpdate.put("user_id", documentReference.getId());
                                    documentReference.update(dataUpdate).
                                            addOnSuccessListener(task2 ->{
                                                //Id de usuario actulizado con exito
                                                //creamos su lista de favoritos
                                                DocumentReference dRef= db.collection("favorites").document(documentReference.getId());
                                                Map <String,Object> dataFavorites= new HashMap<>();
                                                dataFavorites.put("movies","");
                                                dRef.set(dataFavorites).
                                                        addOnSuccessListener(task3 ->{
                                                            Log.d("FirebaseOk", "Todo correcto");
                                                        }).
                                                        addOnFailureListener(e -> {
                                                            Log.w("Error_Firebase", e.getMessage());
                                                         });
                                            }).
                                            addOnFailureListener(e -> {
                                                Log.w("Error_Firebase", e.getMessage());
                                            });

                                 })
                                .addOnFailureListener(e -> {
                                    Log.w("Error_Firebase", e.getMessage());
                                });
                    }
                }
            }
        });
            countDownLatchFirebase.countDown();
        });

        // Esperar a que la tarea asíncrona termine antes de retornar la lista
        try{
            countDownLatchFirebase.await();
        }catch (InterruptedException ei){
            ei.printStackTrace();
        }

    }
}
