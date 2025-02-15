package edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Database.Remote;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Data.EmptyCallback;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Data.Favorite;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Data.User;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Data.UserCallback;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Persistance.AppPersistance;

public class FirestoreManager {

    private static final ExecutorService executorService = Executors.newFixedThreadPool(5);

    private static FirebaseFirestore getInstace(){
        FirebaseFirestore dbFirestore= FirebaseFirestore.getInstance();
        return dbFirestore;
    }

    public static void createUser(EmptyCallback callback){
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
                                    callback.onResult(true);
                                 })
                                .addOnFailureListener(e -> {
                                    Log.w("Error_Firebase", e.getMessage());
                                    callback.onResult(false);
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

    public static void getUser(String email, UserCallback callback){
        FirebaseFirestore db=getInstace();
        db.collection("users").whereEqualTo("email",email).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                QuerySnapshot documents= task.getResult();
                if(documents.isEmpty()) {
                    callback.onResult(null);
                }else{
                    DocumentSnapshot doc=documents.getDocuments().get(0);
                    String user_id=doc.get("user_id").toString();
                    String phone= doc.get("phone").toString();
                    String name= doc.get("name").toString();
                    String image=doc.get("image").toString();
                    String emailGot=doc.get("email").toString();
                    String address= doc.get("address").toString();
                    //Añadir activity_log
                    User user= new User(new ArrayList<>(),address,emailGot,image,name, phone,user_id);
                    callback.onResult(user);
                }
            }
        } );
    }

    public static void addFavorite(Favorite favorite, EmptyCallback callback){
        FirebaseFirestore bd= getInstace();

        bd.collection("favorites")
                .document(AppPersistance.user.getUser_id())
                .collection("movies")
                .get()
                .addOnCompleteListener(
                    task->{
                       if(task.isSuccessful()){
                           QuerySnapshot querySnapshot= task.getResult();
                           if(!querySnapshot.isEmpty()){
                               Boolean exist= false;
                               for (DocumentSnapshot doc: querySnapshot.getDocuments()){
                                   if(doc.getId().equals(favorite.getId())){
                                       exist=true;
                                   }
                               }
                               if(exist){
                                   callback.onResult(false);
                               }
                               else{
                                   Map<String, Object> newDoc= new HashMap<>();
                                   newDoc.put("id", favorite.getId());
                                   newDoc.put("overview",favorite.getDescription());
                                   newDoc.put("posterURL",favorite.getPhoto());
                                   newDoc.put("rating","");
                                   newDoc.put("releaseDate", favorite.getReleaseDate());
                                   newDoc.put("title", favorite.getTitle());

                                   bd.collection("favorites")
                                           .document(AppPersistance.user.getUser_id())
                                           .collection("movies")
                                           .document(favorite.getId())
                                           .set(newDoc)
                                           .addOnSuccessListener(
                                                   aVoid->{
                                                        callback.onResult(true);
                                                   });
                               }
                           }

                       }

                    }
        );
    }
}
