package edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Database.Local;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Data.User;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Database.Remote.FirestoreManager;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.KeystoreManager.KeystoreManager;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Movies.Movie;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Persistance.AppPersistance;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Persistance.SessionManager;

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
            //Tambien guarda la pelicula en firestore
            FirestoreManager.addFavorite(movie,res->{
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

            //También elimina la película de la base de datos
            FirestoreManager.removeFavorite(movieId, success -> {
                Log.d("FirebaseFav","Resultado removeFavoriteFirebase: "+success);
            });

        } catch (Exception e) {
            Log.e("Error", "Error al eliminar favorito", e);
            Toast.makeText(context, "Error al eliminar la película", Toast.LENGTH_SHORT).show();
        }
    }

    // Agrega un usuario a la tabla "users" de la base de datos
    public static void addUser(Context context, User user) {
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

            Log.d("Database_", "Usuario agregado correctamente");
        } catch (Exception e) {
            Log.e("Error", "Error al insertar usuario: " + e.getMessage(), e);
        }
    }

    // Método para actualizar el usuario en la tabla "users"
    public static void updateUser(Context context) {
        User user= AppPersistance.user;
        SQLiteDatabase db = dBhelper.getWritableDatabase();
        try {
            // Encripta teléfono y dirección
            String encryptedPhone = KeystoreManager.encrypt(user.getPhone());
            String encryptedAddress = KeystoreManager.encrypt(user.getAddress());

            String SQL = "UPDATE users SET name = ?, image = ?, phone = ?, address = ? WHERE user_id = ?";
            db.execSQL(SQL, new Object[]{
                    user.getName(),
                    user.getImage(),
                    encryptedPhone,
                    encryptedAddress,
                    user.getUser_id()
            });
            Toast.makeText(context, "Usuario actualizado correctamente", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("Database_", "Error al actualizar usuario: " + e.getMessage(), e);
            Toast.makeText(context, "1Error al actualizar usuario", Toast.LENGTH_SHORT).show();
        }
    }

    // Método que busca un usuario por su ID y, si no existe, lo crea utilizando la información de FirebaseUser.
// Además, desencripta el teléfono y la dirección.
    public static User getOrCreateUser(Context context, FirebaseUser firebaseUser, String user_id) {
        SQLiteDatabase db = dBhelper.getReadableDatabase();
        User user = null;
        Cursor cursor = null;
        try {
            String query = "SELECT * FROM users WHERE user_id = ?";
            cursor = db.rawQuery(query, new String[]{user_id});
            if (cursor.moveToFirst()) {
                user = new User();
                user.setUser_id(cursor.getString(cursor.getColumnIndexOrThrow("user_id")));
                user.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
                user.setImage(cursor.getBlob(cursor.getColumnIndexOrThrow("image"))); // BLOB

                // Desencriptar address
                String encryptedAddress = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                if (encryptedAddress != null && !encryptedAddress.isEmpty()) {
                    try {
                        String decryptedAddress = KeystoreManager.decrypt(encryptedAddress);
                        user.setAddress(decryptedAddress);
                    } catch (Exception e) {
                        Log.e("DBManager", "Error desencriptando address: " + e.getMessage(), e);
                        user.setAddress("");
                    }
                } else {
                    user.setAddress("");
                }

                // Desencriptar phone
                String encryptedPhone = cursor.getString(cursor.getColumnIndexOrThrow("phone"));
                if (encryptedPhone != null && !encryptedPhone.isEmpty()) {
                    try {
                        String decryptedPhone = KeystoreManager.decrypt(encryptedPhone);
                        user.setPhone(decryptedPhone);
                    } catch (Exception e) {
                        Log.e("DBManager", "Error desencriptando phone: " + e.getMessage(), e);
                        user.setPhone("");
                    }
                } else {
                    user.setPhone("");
                }
            }
        } catch (Exception e) {
            Log.e("DBManager", "Error al buscar usuario: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        if (user == null) {
            // Si el usuario no existe, se crea con la información de FirebaseUser.
            user = new User();
            user.setUser_id(user_id);
            user.setName(firebaseUser.getDisplayName());
            user.setEmail(firebaseUser.getEmail());
            // Convertir la foto (si existe) de URL a BLOB (byte[])
            if (firebaseUser.getPhotoUrl() != null) {
                try {
                    User finalUser = user;
                    Thread downloadThread = new Thread(() -> {
                        try {
                            URL url = new URL(firebaseUser.getPhotoUrl().toString());
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            connection.setDoInput(true);
                            connection.setConnectTimeout(5000);
                            connection.setReadTimeout(5000);
                            connection.connect();
                            InputStream input = connection.getInputStream();
                            Bitmap bitmap = BitmapFactory.decodeStream(input);
                            if (bitmap != null) {
                                byte[] imageBytes = convertBitmapToByteArray(bitmap);
                                finalUser.setImage(imageBytes);
                                Log.d("DBManager", "Imagen descargada y convertida correctamente.");
                            } else {
                                Log.e("DBManager", "Bitmap nulo tras decodificar el stream.");
                                finalUser.setImage(null);
                            }
                        } catch (Exception e) {
                            Log.e("DBManager", "Error al descargar la imagen en hilo: " + e.getMessage(), e);
                            finalUser.setImage(null);
                        }
                    });
                    downloadThread.start();
                    downloadThread.join(); // Espera a que finalice la descarga
                } catch (Exception e) {
                    Log.e("DBManager", "Error al ejecutar hilo para descargar imagen: " + e.getMessage(), e);
                    user.setImage(null);
                }
            } else {
                user.setImage(null);
            }
            // Valores por defecto para address y phone
            user.setAddress("");
            user.setPhone("");
            addUser(context, user);
        }
        return user;
    }

    // Método auxiliar para convertir un Bitmap a un arreglo de bytes (BLOB)
    private static byte[] convertBitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    // Método para actualizar la fecha de último login del usuario en la tabla "users"
    public static void updateUserLogin(Context context) {

        User user = AppPersistance.user;
        SQLiteDatabase db = dBhelper.getWritableDatabase();
        try {
            String SQL = "UPDATE users SET last_login = ? WHERE user_id = ?";
            db.execSQL(SQL, new Object[]{SessionManager.getDateLogin(), user.getUser_id()});

        } catch (Exception e) {
            Log.e("Database_", "Error al actualizar login: " + e.getMessage(), e);
        }
    }

    // Método para actualizar la fecha de último logout del usuario en la tabla "users"
    public static void updateUserLogout(Context context) {
        if (AppPersistance.user == null) {
            Log.w("DBManager", "AppPersistance.user es null; se omite la actualización del logout.");
            return;
        }
        User user = AppPersistance.user;
        SQLiteDatabase db = dBhelper.getWritableDatabase();
        try {
            String SQL = "UPDATE users SET last_logout = ? WHERE user_id = ?";
            db.execSQL(SQL, new Object[]{SessionManager.getDateLogout(), user.getUser_id()});

        } catch (Exception e) {
            Log.e("Database_", "Error al actualizar login: " + e.getMessage(), e);
        }
    }
}