package edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Database.Remote;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

   /* public static void updateUser(String userId, String name, String address, String phone, Uri imgUri, Context context, EmptyCallback callback) {
        FirebaseFirestore db = getInstace();
       // db.collection("users").whereEqualTo("user_id", userId).get().addOnCompleteListener()

        try {
            // Convertir URI en Bitmap
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imgUri);

            // Verificar que el Bitmap no sea null
            if (bitmap == null) {
                Log.e("UpdateUser", "Error: Bitmap es NULL");
                callback.onResult(false);
                return;
            }

            // Convertir Bitmap a byte[]
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageData = baos.toByteArray();

            Log.d("UpdateUser", "Iniciando subida de imagen para el usuario: " + userId);

            // Subir imagen a Firebase Storage
            storageRef.putBytes(imageData)
                    .addOnSuccessListener(taskSnapshot -> {
                        Log.d("UpdateUser", "Imagen subida exitosamente.");
                        storageRef.getDownloadUrl()
                                .addOnSuccessListener(uri -> {
                                    String imageUrl = uri.toString();
                                    Log.d("UpdateUser", "URL de descarga obtenida: " + imageUrl);
                                    updateUserData(db, userId, name, address, phone, imageUrl, callback);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("UpdateUser", "Error al obtener URL de descarga", e);
                                    callback.onResult(false);
                                });
                    })
                    .addOnFailureListener(e -> {
                        Log.e("UpdateUser", "Error al subir la imagen", e);
                        callback.onResult(false);
                    });
        } catch (IOException e) {
            Log.e("UpdateUser", "Error al convertir URI a Bitmap", e);
            callback.onResult(false);
        }
    }




    private static void updateUserData(FirebaseFirestore db, String userId, String name, String address, String phone, String imageUrl, EmptyCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("address", address);
        updates.put("phone", phone);
        if (imageUrl != null) {
            updates.put("image", imageUrl);
        }

        db.collection("users").document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> callback.onResult(true))
                .addOnFailureListener(e -> callback.onResult(false));
    }*/



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
