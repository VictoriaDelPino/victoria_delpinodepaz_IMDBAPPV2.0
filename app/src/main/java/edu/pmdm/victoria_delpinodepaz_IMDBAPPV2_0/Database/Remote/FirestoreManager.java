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

import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Data.EmptyCallback;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Data.User;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Data.UserCallback;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Movies.Movie;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Persistance.AppPersistance;

public class FirestoreManager {


    private static FirebaseFirestore getInstace(){
        FirebaseFirestore dbFirestore= FirebaseFirestore.getInstance();
        return dbFirestore;
    }

    public static void createUser(EmptyCallback callback){

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
                        if(currentUser.getDisplayName()!=null)  data.put("name", currentUser.getDisplayName());
                        else data.put("name", "");
                        data.put("user_id", "");
                        data.put("address","");
                        if( currentUser.getPhoneNumber()!= null) data.put("phone", currentUser.getPhoneNumber());
                        else data.put("phone", "");
                        if(currentUser.getPhotoUrl()!=null) data.put("image", currentUser.getPhotoUrl().toString());
                        else data.put("image", "");
                        data.put("activity_log","");
                        db.collection("users").add(data)
                                .addOnSuccessListener(documentReference -> {
                                    //Usuario añadido con exito a la coleccion users
                                    Map <String,Object> dataUpdate= new HashMap<>();
                                    dataUpdate.put("user_id", documentReference.getId());
                                    documentReference.update(dataUpdate).
                                            addOnSuccessListener(task2 ->{
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
                }else{
                    callback.onResult(false);
                }
            }

        });


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

    public static void addFavorite(Movie favorite, EmptyCallback callback){
        FirebaseFirestore bd= getInstace();

        bd.collection("favorites")
                .document(AppPersistance.user.getUser_id())
                .collection("movies")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        boolean exist = false;

                        if (!querySnapshot.isEmpty()) {
                            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                                if (doc.getId().equals(favorite.getId())) {
                                    exist = true;
                                    break;
                                }
                            }
                        }

                        if (!exist) {
                            Map<String, Object> newDoc = new HashMap<>();
                            newDoc.put("id", favorite.getId());
                            newDoc.put("overview", favorite.getDescription());
                            newDoc.put("posterURL", favorite.getPhoto());
                            newDoc.put("rating", "");
                            newDoc.put("releaseDate", favorite.getReleaseDate());
                            newDoc.put("title", favorite.getTitle());

                            bd.collection("favorites")
                                    .document(AppPersistance.user.getUser_id())
                                    .collection("movies")
                                    .document(favorite.getId())
                                    .set(newDoc)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("FirestoreFav", "Película añadida con éxito");
                                        callback.onResult(true);
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("FirestoreError", "Error al añadir película", e);
                                        callback.onResult(false);
                                    });
                        } else {
                            Log.d("FirestoreFav", "La película ya estaba en favoritos");
                            callback.onResult(false);
                        }
                    } else {
                        Log.e("FirestoreError", "Error al obtener colección de películas", task.getException());
                        callback.onResult(false);
                    }
                });

    }

    public static void removeFavorite(String movieId, EmptyCallback callback) {
        FirebaseFirestore db = getInstace();

        db.collection("favorites")
                .document(AppPersistance.user.getUser_id())
                .collection("movies")
                .document(movieId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirestoreFav", "Película eliminada con éxito");
                    callback.onResult(true);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Error al eliminar película", e);
                    callback.onResult(false);
                });
    }

}
