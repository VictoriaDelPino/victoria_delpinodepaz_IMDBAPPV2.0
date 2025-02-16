package edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.ui.gallery;

import static edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Database.Local.DBManager.deleteUserFavorite;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Database.Local.DBManager;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.FavoritesFragment;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.MovieActivity;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Movies.Movie;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.MyItemRecycleViewAdapter;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.R;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.databinding.FragmentGalleryBinding;

//Fragmento que muestra la galería de películas favoritas del usuario.
public class GalleryFragment extends Fragment {

    private static final int REQUEST_BLUETOOTH_PERMISSION = 1;
    private MyItemRecycleViewAdapter adapter;
    private List<Movie> movieList;
    private FragmentGalleryBinding binding;
    private String userEmail;
    private Button btnShare;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        GalleryViewModel galleryViewModel =
                new ViewModelProvider(this).get(GalleryViewModel.class);

        // Obtiene usuario autenticado desde Firebase
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        userEmail = currentUser.getEmail();

        // Infla el layout con ViewBinding
        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        btnShare = binding.btnShare;
        View root = binding.getRoot();

        // Obtiene la lista de películas favoritas del usuario desde la base de datos
        try {
            movieList = DBManager.getUserFavorites(userEmail);
        } catch (Exception e) {
            Log.e("Error", "Error en DB", e);
        }

        // Configura el botón para compartir la lista de películas mediante Bluetooth
        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestBluetoothPermission();
            }
        });

        // Configura el RecyclerView con las películas favoritas
        setupRecyclerView();
        return root;
    }

    //Método para solicitar permisos de Bluetooth en función de la versión de Android.
    private void requestBluetoothPermission() {
        // Para Android 12+ se usa el permiso BLUETOOTH_CONNECT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_BLUETOOTH_PERMISSION);
                return;
            }
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.BLUETOOTH}, REQUEST_BLUETOOTH_PERMISSION);
                return;
            }
        }
        // Si ya se tiene los permisos, se muestra el JSON en el fragmento FavoritesFragment
        Toast.makeText(getContext(), getString(R.string.bt_permission_granted), Toast.LENGTH_SHORT).show();
        showMovieListJson();
    }

    //Maneja el resultado de la solicitud de permisos.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_BLUETOOTH_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showMovieListJson();
                Toast.makeText(getContext(), getString(R.string.bt_permission_granted), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), getString(R.string.bt_permission_denied), Toast.LENGTH_SHORT).show();
            }
        }
    }

    //Convierte la lista de películas en formato JSON y la muestra en un diálogo.
    private void showMovieListJson() {
        if (movieList == null || movieList.isEmpty()) {
            Toast.makeText(getContext(), getString(R.string.no_favorite_movies), Toast.LENGTH_SHORT).show();
            return;
        }
        JSONArray jsonArray = new JSONArray();
        try {
            // Convierte cada película en un objeto JSON y añadirlo al array
            for (Movie movie : movieList) {
                JSONObject movieObj = new JSONObject();
                movieObj.put("id", movie.getId());
                movieObj.put("photo", movie.getPhoto());
                movieObj.put("title", movie.getTitle());
                movieObj.put("description", movie.getDescription());
                movieObj.put("releaseDate", movie.getReleaseDate());
                movieObj.put("rating", movie.getRating());
                jsonArray.put(movieObj);
            }
        } catch (JSONException e) {
            Log.e("ERROR", "Error al convertir la lista de películas a JSON", e);
            Toast.makeText(getContext(), getString(R.string.error_json), Toast.LENGTH_SHORT).show();
            return;
        }
        // Convierte el array a una cadena y lo muestra en un fragmento de favoritos
        String jsonMovies = jsonArray.toString();
        FavoritesFragment favoritesFragment = FavoritesFragment.newInstance(jsonMovies);
        favoritesFragment.show(getParentFragmentManager(), "FavoritesDialog");
    }

    //Configura el RecyclerView para mostrar la lista de películas favoritas.
    private void setupRecyclerView() {
        // Inicializar lista si es null
        if (movieList == null) {
            movieList = new ArrayList<>();
        }

        RecyclerView recyclerView = binding.recyclerViewFav;
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // Configura el adaptador del RecyclerView con eventos de click y onlongclick
        adapter = new MyItemRecycleViewAdapter(movieList, getContext(), new MyItemRecycleViewAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(Movie movie) {
                movie.setRating("");
                Toast.makeText(getContext(), "Click en: " + movie.getTitle(), Toast.LENGTH_SHORT).show();
                // Abre la actividad de detalles de la película
                Intent intent = new Intent(getActivity(), MovieActivity.class);
                intent.putExtra("movie", movie);
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(Movie movie) {
                // Elimina la película de la lista de favoritos en la base de datos
                deleteUserFavorite(getContext(), userEmail, movie.getId());
                // Elimina la película de la lista
                movieList.remove(movie);
                // Notifica al adaptador del cambio
                adapter.notifyDataSetChanged();
            }
        });

        recyclerView.setAdapter(adapter);
    }

    //Método llamado cuando la vista del fragmento es destruida para liberar recursos.
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
