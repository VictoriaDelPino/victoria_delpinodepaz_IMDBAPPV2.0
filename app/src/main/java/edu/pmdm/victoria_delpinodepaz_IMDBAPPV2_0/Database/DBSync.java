package edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Database;

import static edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Database.Local.DBManager.deleteUserFavorite;
import static edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Database.Local.DBManager.getUserFavorites;
import static edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Database.Local.DBManager.setUserFavorite;

import android.content.Context;
import android.util.Log;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Database.Remote.FirestoreManager;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Movies.Movie;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Persistance.AppPersistance;

public class DBSync {

    //Método que sincroniza la base de datos local con lo que pasa en firestore
    public static void syncFavoritesWithSQLite(Context context) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Obtiene los favoritos existentes (Sincronización inicial)
        db.collection("favorites")
                .document(AppPersistance.user.getUser_id())
                .collection("movies")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        try {
                            //Recorre todos los documentos obtenidos y extrae los datos de la película
                            for (DocumentSnapshot doc : task.getResult()) {
                                String movieId = doc.getId();
                                String title = doc.getString("title");
                                String description = doc.getString("overview");
                                String releaseDate = doc.getString("releaseDate");
                                String photoUrl = doc.getString("posterURL");
                                Movie movie= new Movie(description,title,photoUrl,releaseDate,movieId, "");

                                //Guardar la película como favorita en SQLite
                                setUserFavorite(context,AppPersistance.user.getUser_id(), movie);
                            }
                        } catch (Exception e) {
                            Log.e("FirestoreSync", "Error al sincronizar favoritos iniciales", e);
                        }
                    }
                });

        // Escucha cambios en tiempo real
        db.collection("favorites")
                .document(AppPersistance.user.getUser_id())
                .collection("movies")
                .addSnapshotListener((snapshots, e) -> {
                    // Manejo de errores en el listener de cambios en tiempo real
                    if (e != null) {
                        Log.e("FirestoreSync", "Error al sincronizar favoritos en tiempo real", e);
                        return;
                    }

                    try {
                        // Recorre los cambios en los documentos de la colección
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            //Extrae información de la película del documento modificado
                            DocumentSnapshot doc = dc.getDocument();
                            String movieId = doc.getId();
                            String title = doc.getString("title");
                            String description = doc.getString("overview");
                            String releaseDate = doc.getString("releaseDate");
                            String photoUrl = doc.getString("posterURL");
                            Movie movie= new Movie(description,title,photoUrl,releaseDate,movieId, "");
                            //Dependiendo del tipo de cambio, agregar o eliminar el favorito en SQLite
                            switch (dc.getType()) {
                                case ADDED:
                                case MODIFIED:
                                    setUserFavorite(context,AppPersistance.user.getUser_id(), movie);
                                    break;
                                case REMOVED:
                                    deleteUserFavorite(context,AppPersistance.user.getUser_id(), movieId);
                                    break;
                            }
                        }
                    } catch (Exception ex) {
                        Log.e("FirestoreSync", "Error al sincronizar la base de datos SQLite en tiempo real", ex);
                    }
                });
    }

    //Método para sincronizar firestore con la informacion de la base de datos local
    public static void syncFavoritesWithFirestore(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<Movie> movieList =getUserFavorites(AppPersistance.user.getUser_id());
        for(Movie movie : movieList){
            FirestoreManager.addFavorite(movie, res->{
                Log.d("FirebaseFav","Resultado addFavoriteFirebase: "+res);
            });
        }
    }
}
