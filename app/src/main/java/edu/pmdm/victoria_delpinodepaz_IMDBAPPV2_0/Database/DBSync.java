package edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Database;

import static edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Database.Local.DBManager.deleteUserFavorite;
import static edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Database.Local.DBManager.setUserFavorite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Database.Local.DBhelper;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Movies.Movie;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Persistance.AppPersistance;

public class DBSync {

    public static void syncFavoritesWithSQLite(Context context) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = AppPersistance.user.getUser_id();

        // 🔹 Paso 1: Obtener favoritos existentes (Sincronización inicial)
        db.collection("favorites")
                .document(userId)
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
                .document(userId)
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
}
