package edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Database;

import static edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Database.Local.DBManager.deleteUserFavorite;
import static edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Database.Local.DBManager.getUserFavorites;
import static edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Database.Local.DBManager.setUserFavorite;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Database.Remote.FirestoreManager;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Movies.Movie;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Persistance.AppPersistance;

public class DBSync {

    public static void syncFavoritesWithSQLite(Context context) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 🔹 Paso 1: Obtener favoritos existentes (Sincronización inicial)
        db.collection("favorites")
                .document(AppPersistance.user.getUser_id())
                .collection("movies")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        try {
                            for (DocumentSnapshot doc : task.getResult()) {
                                String movieId = doc.getId();
                                String title = doc.getString("title");
                                String description = doc.getString("overview");
                                String releaseDate = doc.getString("releaseDate");
                                String photoUrl = doc.getString("posterURL");
                                Movie movie= new Movie(description,title,photoUrl,releaseDate,movieId, "");

                                setUserFavorite(context,AppPersistance.user.getUser_id(), movie);
                            }
                        } catch (Exception e) {
                            Log.e("FirestoreSync", "Error al sincronizar favoritos iniciales", e);
                        }
                    }
                });

        // 🔹 Paso 2: Escuchar cambios en tiempo real
        db.collection("favorites")
                .document(AppPersistance.user.getUser_id())
                .collection("movies")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e("FirestoreSync", "Error al sincronizar favoritos en tiempo real", e);
                        return;
                    }

                    try {
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            DocumentSnapshot doc = dc.getDocument();
                            String movieId = doc.getId();
                            String title = doc.getString("title");
                            String description = doc.getString("overview");
                            String releaseDate = doc.getString("releaseDate");
                            String photoUrl = doc.getString("posterURL");
                            Movie movie= new Movie(description,title,photoUrl,releaseDate,movieId, "");
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
