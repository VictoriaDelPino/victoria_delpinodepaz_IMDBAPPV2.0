package edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.ApiConnection;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Movies.Genre;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Movies.Movie;

public class ApiTMDB {
    // URL de la API para obtener la lista de géneros en español
    private static final String API_URL = "https://api.themoviedb.org/3/genre/movie/list?language=es";
    private static  String API_URL_2;

    // Clave de autenticación para la API
    private static final String API_KEY = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJjNDg2M2FmMGIxNTA3MzVjYjMyYjQwOTNiY2E0YTBiZCIsIm5iZiI6MTczODQzNjcxOC40NzMsInN1YiI6IjY3OWU3MDZlYTFlMzNjNDA4YTI2MWNhYyIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.LqxvWm0e_oI1DS7NAM1djVEWHw89rD_p7TXhbE8FSI0"; // Reemplázalo con tu token válido

    // Pool de hilos para ejecutar tareas en segundo plano
    private static final ExecutorService executorServiceTMDB = Executors.newFixedThreadPool(5);

    // Lista de géneros obtenidos de la API
    private static final List<Genre> genreList = new ArrayList<>();

    // Lista de películas obtenidas según los filtros de búsqueda
    private static List<Movie> movieList;

    /*Obtiene la lista de géneros de la API de TMDB.
      devuelve la  Lista de géneros disponibles en la API.*/
    public static ArrayList<Genre> getGenre() {

        // Permite la sincronización entre hilos para esperar la finalización de tareas
        CountDownLatch countDownLatchTMDB= new CountDownLatch(1);
        executorServiceTMDB.execute(() -> {

            HttpURLConnection connection = null;
            try {
                // Establece la conexión con la API
                URL url = new URL(API_URL);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("Authorization", API_KEY);
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                // Validación del código de respuesta HTTP
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Lee la respuesta de la API
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    // Convierte la respuesta a un JSONObject
                    JSONObject jsonObject = new JSONObject(response.toString());
                    JSONArray genresArray = jsonObject.getJSONArray("genres");

                    Genre genre;
                    // Obtiene los generos de la api y lo mete en la lista
                    for (int i = 0; i < genresArray.length(); i++) {
                        genre=new Genre();
                        JSONObject genreObject = genresArray.getJSONObject(i);
                        int genreId=genreObject.getInt("id");
                        String genreName = genreObject.getString("name");
                        genre.setId(genreId);
                        genre.setGenreName(genreName);
                        genreList.add(genre);
                    }
                } else {
                    Log.e("ERROR", "Error en la API: Código " + responseCode);
                }

            } catch (Exception e) {
                Log.e("ERROR", "Error en la API: " + e.getMessage(), e);

            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }

            // Contador de finalización de la tarea
            countDownLatchTMDB.countDown();
        });

        // Esperar a que la tarea termine antes de continuar
        try{
            countDownLatchTMDB.await();
        }catch (InterruptedException ei){
            ei.printStackTrace();
        }
        return (ArrayList<Genre>) genreList;
    }

    /*Busca películas por año y género en la API de TMDB.
     Devuelve la Lista de películas filtradas por año y género.*/
    public static ArrayList<Movie> getSearchedList(String year, String genre) {

        // Permite la sincronización entre hilos para esperar la finalización de tareas
        CountDownLatch countDownLatchTMDB= new CountDownLatch(1);
        executorServiceTMDB.execute(() -> {
            movieList=new ArrayList<>();

            // Construye la URL de búsqueda con los filtros de año y género
            API_URL_2= "https://api.themoviedb.org/3/discover/movie?"
                    + "primary_release_year=" + year
                    + "&with_genres=" + genre
                    + "&include_adult=false"
                    + "&language=es-ES"
                    + "&page=1";

            HttpURLConnection connection = null;
            // Establece la conexión con la API
            try {
                URL url = new URL(API_URL_2);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("Authorization", API_KEY);
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                // Valida el código de respuesta HTTP
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    // Convierte la respuesta a un JSONObject
                    JSONObject jsonObject = new JSONObject(response.toString());
                    JSONArray moviesArray = jsonObject.getJSONArray("results");

                    Movie movie;
                    for (int i = 0; i < moviesArray.length(); i++) {
                        movie=new Movie();
                        JSONObject movieObject = moviesArray.getJSONObject(i);

                        // Extrae los datos de cada película
                        int idMovieInt= movieObject.getInt(("id"));
                        String idMovie=""+idMovieInt;
                        movie.setId(idMovie);

                        String titleMovie= movieObject.getString(("title"));
                        movie.setTitle(titleMovie);

                        String releaseDate= movieObject.getString(("release_date"));
                        movie.setReleaseDate(releaseDate);

                        String posterPath= movieObject.getString(("poster_path"));
                        String photoURL="https://image.tmdb.org/t/p/w500"+posterPath;
                        movie.setPhoto(photoURL);

                        String overview= movieObject.getString(("overview"));
                        movie.setDescription(overview);

                        movie.setRating(""); // El rating no está siendo extraído, queda vacío
                        movieList.add(movie);
                    }
                } else {
                    Log.e("Error", "Error en la API TMDB: Código " + responseCode);
                }

            } catch (Exception e) {
                Log.e("ERROR", "Error en la API TMDB: " + e.getMessage(), e);

            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            // Contador de finalización de la tarea
            countDownLatchTMDB.countDown();
        });

        // Esperar a que la tarea termine antes de continuar
        try{
            countDownLatchTMDB.await();
        }catch (InterruptedException ei){
            ei.printStackTrace();
        }
        return (ArrayList<Movie>) movieList;
    }
}