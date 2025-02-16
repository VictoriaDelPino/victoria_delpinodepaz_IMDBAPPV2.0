package edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.ApiConnection.ApiTMDB;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Database.Local.DBManager;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Database.Remote.FirestoreManager;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Movies.Movie;

//Actividad que muestra las películas filtrada en la api TMDB
public class SearchResultActivity extends AppCompatActivity {

    private MyItemRecycleViewAdapter adapter;
    private List<Movie> movieList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search_result);
        DBManager.init(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Obtiene los datos enviados a través del Intent
        Intent intent = getIntent();
        if (intent != null) {
            int genreId = intent.getIntExtra("GENRE_ID", -1);
            String year = intent.getStringExtra("YEAR");

            // Verifica si los datos son válidos antes de realizar la búsqueda
            if (genreId != -1 && year != null) {
                // Obtiene la lista de películas según los parámetros de búsqueda
                movieList = ApiTMDB.getSearchedList(year, String.valueOf(genreId));
                // Si la API devuelve null, inicializa una lista vacía para evitar errores
                if (movieList == null) {
                    movieList = new ArrayList<>();
                }
            } else {
                movieList = new ArrayList<>();
            }
        } else {
            movieList = new ArrayList<>();
        }
        // Configura el RecyclerView con los datos obtenidos
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        // Verifica si la lista es null y la inicializa si es necesario
        if (movieList == null) {
            movieList = new ArrayList<>();
        }

        // Obtiene la referencia del RecyclerView en el layout
        RecyclerView recyclerView = findViewById(R.id.recycleViewSearchResult);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // Crea el adaptador y define el comportamiento de los clics en los elementos
        adapter = new MyItemRecycleViewAdapter(movieList, this, new MyItemRecycleViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Movie movie) {
                Toast.makeText(SearchResultActivity.this, "Click en: " + movie.getTitle(), Toast.LENGTH_SHORT).show();
                // Abre la actividad de detalles de la película
                Intent intent = new Intent(SearchResultActivity.this, MovieActivity.class);
                intent.putExtra("movie", movie);
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(Movie movie) {
                // Obtiene el usuario actual de Firebase
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

                if (currentUser != null) {
                    String userEmail = currentUser.getEmail();

                    // Intenta guardar la película en la base de datos
                    try {
                        DBManager.setUserFavorite(userEmail, movie);

                        FirestoreManager.addFavorite(movie,res->{
                            Toast.makeText(
                                    SearchResultActivity.this,
                                    "Resultado addFavoriteFirebase: "+res,
                                    Toast.LENGTH_SHORT
                            ).show();
                            Log.d("FirebaseFav","Resultado addFavoriteFirebase: "+res);
                        });


                        Toast.makeText(SearchResultActivity.this, movie.getTitle() +" "+ getString(R.string.save_as_favorite), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(
                                SearchResultActivity.this,
                                getString(R.string.error_saving_favorites),
                                Toast.LENGTH_SHORT
                        ).show();
                        Log.e("ERROR", "Error en DB", e);
                    }
                } else {
                    Toast.makeText(
                            SearchResultActivity.this,
                            getString(R.string.start_session_to_save_as_favorite),
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }
        });
        // Asigna el adaptador al RecyclerView
        recyclerView.setAdapter(adapter);
    }
}
