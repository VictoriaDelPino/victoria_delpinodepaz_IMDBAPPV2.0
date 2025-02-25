package edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Database.Remote;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Data.EmptyCallback;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Data.User;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Data.UserCallback;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Movies.Movie;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Persistance.AppPersistance;

// Clase FirestoreManager que gestiona las operaciones remotas utilizando Firebase Firestore.
public class FirestoreManager {

    // Método que obtiene y devuelve la instancia de FirebaseFirestore.
    private static FirebaseFirestore getInstace(){
        FirebaseFirestore dbFirestore= FirebaseFirestore.getInstance();
        return dbFirestore;
    }

    // Crea un usuario en Firestore si no existe ya en la colección "users".
    public static void createUser(EmptyCallback callback){

        String email=FirebaseAuth.getInstance().getCurrentUser().getEmail();
        FirebaseFirestore db= getInstace();

        //Comprueba que el usuario no existe en la base de datos de Firestore
        db.collection("users").whereEqualTo("email",email).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                QuerySnapshot documents= task.getResult();
                if(documents.isEmpty()){
                    //Si el usuario no existe lo crea
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
                        data.put("activity_log","");
                        db.collection("users").add(data)
                                .addOnSuccessListener(documentReference -> {
                                    //Si el usuario ha sido añadido con exito a la coleccion users
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

    // Obtiene un usuario de Firestore por su email y devuelve el User en el callback.
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
                    String name= doc.get("name").toString();
                    String emailGot=doc.get("email").toString();
                    User user= new User();
                    user.setUser_id(user_id);
                    user.setName(name);
                    user.setEmail(emailGot);
                    callback.onResult(user);
                }
            }
        } );
    }

    // Agrega una película a la colección de favoritos en Firestore para el usuario actual.
    public static void addFavorite(Movie favorite, EmptyCallback callback){
        FirebaseFirestore bd= getInstace();

        //Comprueba si la pelicula ya existe en la colección
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
                            // Prepara los datos de la película para ser añadida
                            Map<String, Object> newDoc = new HashMap<>();
                            newDoc.put("id", favorite.getId());
                            newDoc.put("overview", favorite.getDescription());
                            newDoc.put("posterURL", favorite.getPhoto());
                            newDoc.put("rating", "");
                            newDoc.put("releaseDate", favorite.getReleaseDate());
                            newDoc.put("title", favorite.getTitle());
                            //Guarda la película
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

    // Elimina una película de la colección de favoritos en Firestore para el usuario actual.
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
