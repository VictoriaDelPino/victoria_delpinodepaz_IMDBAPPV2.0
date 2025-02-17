package edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Database.Local.DBhelper;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Persistance.AppPersistance;

public class DBSync {

    public static void syncFavoritesWithSQLite(DBhelper dbHelper) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = AppPersistance.user.getUser_id();

        // 🔹 Paso 1: Obtener favoritos existentes (Sincronización inicial)
        db.collection("favorites")
                .document(userId)
                .collection("movies")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        SQLiteDatabase database = dbHelper.getWritableDatabase();
                        database.beginTransaction();
                        try {
                            for (DocumentSnapshot doc : task.getResult()) {
                                String movieId = doc.getId();
                                String title = doc.getString("title");
                                String description = doc.getString("overview");
                                String releaseDate = doc.getString("releaseDate");
                                String photoUrl = doc.getString("posterURL");

                                database.execSQL("INSERT OR REPLACE INTO favorites (user_id, movie_id, title, description, release_date, poster_url) VALUES (?, ?, ?, ?, ?, ?)",
                                        new Object[]{userId, movieId, title, description, releaseDate, photoUrl});
                            }
                            database.setTransactionSuccessful();
                        } catch (Exception e) {
                            Log.e("FirestoreSync", "Error al sincronizar favoritos iniciales", e);
                        } finally {
                            database.endTransaction();
                            database.close();
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

                    SQLiteDatabase database = dbHelper.getWritableDatabase();
                    try {
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            DocumentSnapshot doc = dc.getDocument();
                            String movieId = doc.getId();
                            String title = doc.getString("title");
                            String description = doc.getString("overview");
                            String releaseDate = doc.getString("releaseDate");
                            String photoUrl = doc.getString("posterURL");

                            switch (dc.getType()) {
                                case ADDED:
                                case MODIFIED:
                                    database.execSQL("INSERT OR REPLACE INTO favorites (user_id, movie_id, title, description, release_date, poster_url) VALUES (?, ?, ?, ?, ?, ?)",
                                            new Object[]{userId, movieId, title, description, releaseDate, photoUrl});
                                    break;
                                case REMOVED:
                                    database.execSQL("DELETE FROM favorites WHERE user_id = ? AND movie_id = ?",
                                            new Object[]{userId, movieId});
                                    break;
                            }
                        }
                    } catch (Exception ex) {
                        Log.e("FirestoreSync", "Error al sincronizar la base de datos SQLite en tiempo real", ex);
                    } finally {
                        database.close();
                    }
                });
    }
}
