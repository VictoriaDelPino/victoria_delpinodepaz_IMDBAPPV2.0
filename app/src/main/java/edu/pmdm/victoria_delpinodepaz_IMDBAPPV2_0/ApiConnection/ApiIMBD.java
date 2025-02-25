package edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.ApiConnection;

import android.util.Log;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.*;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Movies.Movie;
import java.util.concurrent.CountDownLatch;

public class ApiIMBD {
    // Claves y valores para la autenticación en la API de RapidAPI
    private static final String HEADER_KEY = "x-rapidapi-key";
    private static final String HEADER_HOST = "x-rapidapi-host";

    //Api keys cargadas que puede usar la aplicación
    private static final String API_KEY_1 = "c6fe9d2717msh7eeaffed3f84584p1c3319jsn676081a570f9";
    private static final String API_KEY_2 = "586f49b130msh99155cbad4559f5p12735bjsn219e7f19e09e";
    private static final String API_KEY_3 = "5f871d3eeemsh5a94169685bb269p1e3fd8jsn031f2b0f5978";
    private static final List<String> API_KEYS = Arrays.asList(
            API_KEY_1, API_KEY_2, API_KEY_3);
    private static int apiKeyIndex = 0;
    private static String apiKey = API_KEYS.get(apiKeyIndex);
    private static final String HEADER_VALUE = "imdb-com.p.rapidapi.com";
    // Tiempo de espera para las solicitudes en milisegundos
    private static final int TIMEOUT = 5000;
    private static int attempts=0;
    // Servicio de ejecución en paralelo con un pool de 5 hilos
    private static final ExecutorService executorService = Executors.newFixedThreadPool(5);
    // Lista compartida de películas obtenidas de la API
    private static final List<Movie> moviesList = new ArrayList<>();

    // Interfaz para manejar la recepción de la sinopsis de una película
    public interface MovieOverviewCallback {
        void onOverviewReceived(String overview);
    }

    //Actualiza la api key a la siguiente de la lista
    private static synchronized void switchApiKey() {
        apiKeyIndex = (apiKeyIndex + 1) % API_KEYS.size();
        apiKey = API_KEYS.get(apiKeyIndex);
        Log.d("API_cambio", "Cambiando API Key a: " + apiKeyIndex);
    }

    /* Método para obtener el top 10 de películas desde la API de IMDb.
      Se ejecuta en un hilo separado usando un ExecutorService.
      Devuelve una Lista de las 10 mejores películas.*/
    public static ArrayList<Movie> getTop10Movie() {
        // Permite la sincronización entre hilos para esperar la finalización de tareas
        CountDownLatch countDownLatch= new CountDownLatch(1);

        executorService.execute(() -> {
            boolean success = false;
            while (!success) {
            try {
                // URL de la API para obtener el ranking de películas
                String URLstring = "https://imdb-com.p.rapidapi.com/title/get-top-meter?topMeterTitlesType=ALL";
                URL url = new URL(URLstring);

                // Configuración de la conexión HTTP
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty(HEADER_KEY, apiKey);
                connection.setRequestProperty(HEADER_HOST, HEADER_VALUE);
                connection.setConnectTimeout(TIMEOUT);
                connection.setReadTimeout(TIMEOUT);
                Log.d("API_cambio", "Cambiando API Key a: " + apiKey);
                // Validación del código de respuesta HTTP
                int responseCode = connection.getResponseCode();
                if (responseCode == 429 || responseCode != 200) {
                    //si hay error cambia de api key
                    switchApiKey();
                    continue;
                }

                // Lee la respuesta JSON de la API
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Convierte de la respuesta JSON
                JSONArray filmsArray = new JSONObject(response.toString())
                        .getJSONObject("data")
                        .getJSONObject("topMeterTitles")
                        .getJSONArray("edges");

                // Lista temporal para almacenar las películas obtenidas
                List<Movie> tempMoviesList = new ArrayList<>();
                for (int i = 0; i < Math.min(filmsArray.length(), 10); i++) {
                    JSONObject movieObject = filmsArray.getJSONObject(i).getJSONObject("node");
                    Movie movie = new Movie();
                    movie.setId(movieObject.getString("id"));
                    movie.setTitle(movieObject.getJSONObject("titleText").getString("text"));
                    movie.setPhoto(movieObject.getJSONObject("primaryImage").getString("url"));
                    JSONObject releaseDateObject = movieObject.optJSONObject("releaseDate");
                    if (releaseDateObject != null) {
                        String releaseDate = releaseDateObject.optInt("day", 1) + "/" +
                                releaseDateObject.optInt("month", 1) + "/" +
                                releaseDateObject.optInt("year", 2000);
                        movie.setReleaseDate(releaseDate);
                    }
                    movie.setRating(String.valueOf(movieObject.getJSONObject("meterRanking").optInt("currentRank", 0)));
                    tempMoviesList.add(movie);
                }

                // Actualiza la lista global de películas
                moviesList.clear();
                moviesList.addAll(tempMoviesList);

                // Sincroniza la recuperación de descripciones de películas
                CountDownLatch countDown = new CountDownLatch(moviesList.size());
                for (int i = 0; i < moviesList.size(); i++) {
                    Movie movie = moviesList.get(i);
                    int finalI = i;
                    // Llama a la API para obtener la sinopsis de cada película
                    getMovieOverview(movie.getId(), overview -> {
                        movie.setDescription(overview);
                        Log.d("Victoria__Overview", "Descripción recibida: " + overview);
                        Log.d("Victoria__movie_" + finalI, movie.toString());
                        countDown.countDown();
                    });
                }
                // Espera a que todas las sinopsis sean recuperadas antes de continuar
                try {
                    countDown.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                Log.d("Victoria__Error", "Error: " + e.getMessage());
            }
            // Contador de finalización de la tarea principal
            countDownLatch.countDown();
            success = true;
        }
        });
        // Espera a que la tarea asíncrona termine antes de retornar la lista
        try{
            countDownLatch.await();
        }catch (InterruptedException ei){
            ei.printStackTrace();
        }
        return (ArrayList<Movie>) moviesList;
    }

    // Método para obtener la sinopsis de una película en función de su ID.
    public static void getMovieOverview(String movieID, MovieOverviewCallback callback) {
        executorService.submit(() -> {
            boolean success = false;
            while (!success) {
            try {
                // URL de la API para obtener la sinopsis de la película
                String URLstring = "https://imdb-com.p.rapidapi.com/title/get-overview?tconst=" + movieID;
                URL url = new URL(URLstring);

                // Configuración de la conexión HTTP
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty(HEADER_KEY, apiKey);
                connection.setRequestProperty(HEADER_HOST, HEADER_VALUE);
                connection.setConnectTimeout(TIMEOUT);
                connection.setReadTimeout(TIMEOUT);

                // Manejo de error 429 (demasiadas solicitudes): espera y reintenta
                int responseCode = connection.getResponseCode();
                if (responseCode == 429) {
                    if (attempts<10){
                        attempts++;
                        Thread.sleep(3000);
                        getMovieOverview(movieID, callback);
                        return;
                    }else {
                        //si el error continua cambia de api key
                        attempts=0;
                        switchApiKey();
                        continue;
                    }
                }
                if (responseCode != 200) {
                    Log.d("Error", "API Response Code: " + responseCode);
                    callback.onOverviewReceived("");
                    return;
                }

                // Leee la respuesta JSON
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Convierte de la sinopsis desde el JSON
                JSONObject jsonObject = new JSONObject(response.toString());
                String plot = jsonObject.getJSONObject("data")
                        .getJSONObject("title")
                        .getJSONObject("plot")
                        .getJSONObject("plotText")
                        .getString("plainText");

                // Devuelve la sinopsis a través del callback
                callback.onOverviewReceived(plot);

            } catch (Exception e) {
                Log.d("Error", "Error: " + e.getMessage());
                callback.onOverviewReceived("");
            }
            success = true;
        }

        });
    }
}

