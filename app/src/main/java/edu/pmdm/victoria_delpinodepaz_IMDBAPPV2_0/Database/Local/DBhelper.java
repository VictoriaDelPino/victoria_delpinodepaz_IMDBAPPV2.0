package edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Database.Local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

 /* Clase DBhelper que gestiona la base de datos SQLite de la aplicación.
  Se utiliza para almacenar las películas favoritas de los usuarios. */
public class DBhelper extends SQLiteOpenHelper {
     // Nombre del archivo de la base de datos
    private static final String DATABASE_NAME = "favoriteMovies.db";
     // Versión de la base de datos
     private static final int DATABASE_VERSION = 2;
     // Instancia única de DBhelper (patrón Singleton)
     private static DBhelper instance;

     // Consulta SQL para crear la tabla de favoritos
    private static final String SQL_CREATE_FAVORITES =
            "CREATE TABLE favorites (" +
                    "user_id TEXT NOT NULL," +
                    "movie_id TEXT NOT NULL," +
                    "title TEXT NOT NULL," +
                    "description TEXT," +
                    "release_date TEXT," +
                    "url_photo TEXT," +
                    "PRIMARY KEY (user_id, movie_id))";// Clave primaria compuesta (un usuario no puede guardar la misma película dos veces)

     private static final String SQL_CREATE_USERS =
             "CREATE TABLE users (" +
                     "user_id TEXT PRIMARY KEY, " +
                     "name TEXT, " +
                     "email TEXT, " +
                     "image TEXT, " +
                     "address TEXT, " +
                     "phone TEXT, " +
                     "last_login TEXT, " +
                     "last_logout TEXT)";

     /*Constructor privado para evitar instancias directas.
      Se usa el patrón Singleton para asegurar que solo haya una instancia de la base de datos.
      context Contexto de la aplicación.*/
     public DBhelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

     /*Método para obtener la instancia única de DBhelper.
      Si la instancia no existe, se crea una nueva.
      context Contexto de la aplicación.
      devuelve la Instancia única de DBhelper.*/
    public static synchronized DBhelper getInstance(Context context) {
        if (instance == null) {
            instance = new DBhelper(context.getApplicationContext());
        }
        return instance;
    }

     /*Método llamado cuando se crea la base de datos por primera vez.
      Se ejecuta la consulta para crear la tabla de favoritos.*/
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_FAVORITES);
        db.execSQL(SQL_CREATE_USERS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS favorites");
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }
}