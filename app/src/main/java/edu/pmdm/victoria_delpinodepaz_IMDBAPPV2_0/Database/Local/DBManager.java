package edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Database.Local;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Data.User;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Database.Remote.FirestoreManager;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Movies.Movie;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Persistance.AppPersistance;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.SearchResultActivity;

/*Clase DBManager que gestiona las operaciones de la base de datos relacionadas con los favoritos del usuario.
 Proporciona métodos para inicializar la base de datos, obtener favoritos, agregar y eliminar películas favoritas.*/
public class DBManager {

    // Instancia del helper para manejar la base de datos
    private static DBhelper dBhelper;

    /*Inicializa la instancia de la base de datos.
    Este método se llama antes de usar cualquier otro método de DBManager.*/
    public static void init(Context context) {
        if (dBhelper == null) {
            dBhelper = DBhelper.getInstance(context);
        }
    }

    // Obtiene la lista de películas favoritas de un usuario específico.
    public static List<Movie> getUserFavorites(String user_id) {
        List<Movie> movieList = new ArrayList<>();
        SQLiteDatabase db = dBhelper.getReadableDatabase();

        // Consulta SQL para obtener las películas favoritas del usuario
        String SQL = "SELECT * FROM favorites WHERE user_id = ?";

        try (Cursor cursor = db.rawQuery(SQL, new String[]{user_id})) {
            while (cursor.moveToNext()) {
                Movie movie = new Movie();
                movie.setId(cursor.getString(cursor.getColumnIndexOrThrow("movie_id")));
                movie.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
                movie.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
                movie.setReleaseDate(cursor.getString(cursor.getColumnIndexOrThrow("release_date")));
                movie.setPhoto(cursor.getString(cursor.getColumnIndexOrThrow("url_photo")));
                movieList.add(movie);
            }
        } catch (Exception e) {
            Log.e("Error", "Error al obtener favoritos", e);
        }
        return movieList;
    }

    /*Agrega una película a la lista de favoritos de un usuario.
    Si la película ya existe en la base de datos, no se insertará de nuevo.*/
    public static void setUserFavorite(Context context, String user_id, Movie movie) {
        if (user_id == null || user_id.isEmpty() || movie == null) {
            Log.e("Database_", "Datos inválidos para favorito");
            return;
        }
        try {
            // Inserta la película en la tabla de favoritos si no existe
            String SQL = "INSERT OR IGNORE INTO favorites VALUES (?, ?, ?, ?, ?, ?)";
            SQLiteDatabase db = dBhelper.getWritableDatabase();
            db.execSQL(SQL, new Object[]{
                    user_id,
                    movie.getId(),
                    movie.getTitle(),
                    movie.getDescription(),
                    movie.getReleaseDate(),
                    movie.getPhoto()
            });

            FirestoreManager.addFavorite(movie,res->{
                Toast.makeText(
                        context,
                        "Resultado addFavoriteFirebase: "+res,
                        Toast.LENGTH_SHORT
                ).show();
                Log.d("FirebaseFav","Resultado addFavoriteFirebase: "+res);
            });
        } catch (Exception e) {
            Log.e("Error", "Error al insertar favorito: " + e.getMessage(), e);
            throw e;
        }
    }

    //Elimina una película de la lista de favoritos del usuario.
    public static void deleteUserFavorite(Context context, String user_id, String movieId) {
        SQLiteDatabase db;
        try {
            db = dBhelper.getWritableDatabase();
            // Consulta SQL para eliminar la película favorita del usuario
            String SQL = "DELETE FROM favorites WHERE user_id = ? AND movie_id = ?";
            db.execSQL(SQL, new Object[]{user_id, movieId});

            // Notifica al usuario que la película ha sido eliminada
            Toast.makeText(context, "Película eliminada de favoritos", Toast.LENGTH_SHORT).show();

            FirestoreManager.removeFavorite(movieId, success -> {
                if (success) {
                    Toast.makeText(context, "Película eliminada de favoritos firestore", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Error al eliminar película firestore", Toast.LENGTH_SHORT).show();
                }
            });


        } catch (Exception e) {
            Log.e("Error", "Error al eliminar favorito", e);
            Toast.makeText(context, "Error al eliminar la película", Toast.LENGTH_SHORT).show();
        }
    }
    public static void addUser(Context context) {
        User user= AppPersistance.user;

        SQLiteDatabase db = dBhelper.getWritableDatabase();

        String SQL = "INSERT OR IGNORE INTO users (user_id, name, email, image, address, phone, last_login, last_logout) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            db.execSQL(SQL, new Object[]{
                    user.getUser_id(),
                    user.getName(),
                    user.getEmail(),
                    user.getImage(),
                    user.getAddress(),
                    user.getPhone(),
                    "",
                    ""
            });

            Toast.makeText(context, "Usuario agregado correctamente", Toast.LENGTH_SHORT).show();
            Log.d("Database_", "Usuario agregado correctamente");

        } catch (Exception e) {
            Log.e("Error", "Error al insertar usuario: " + e.getMessage(), e);
            Toast.makeText(context, "Error al agregar usuario", Toast.LENGTH_SHORT).show();
        }
    }



}