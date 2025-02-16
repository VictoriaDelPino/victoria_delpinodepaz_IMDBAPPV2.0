package edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Database.Local.DBManager;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.MovieActivity;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Movies.Movie;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.MyItemRecycleViewAdapter;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Persistance.AppPersistance;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.R;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.databinding.FragmentHomeBinding;

//Fragmento que muestra el top 10 de películas.
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private MyItemRecycleViewAdapter adapter;
    private List<Movie> movieList;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        // Inicializa la base de datos con el contexto actual
        DBManager.init(getContext());

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Configura el RecyclerView con una lista de películas
        setupRecyclerView();

        return root;
    }

    private void setupRecyclerView() {
        //Llama a la API para cargar el top 10 de películas
        movieList=new ArrayList<>();
        //movieList = ApiIMBD.getTop10Movie();

        // Configura RecyclerView con GridLayoutManager para 2 columnas
        binding.recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // Inicializa el adaptador del RecyclerView y define los eventos de click
        adapter = new MyItemRecycleViewAdapter(movieList, getContext(), new MyItemRecycleViewAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(Movie movie) {
                Toast.makeText(getContext(), "Click en: " + movie.getTitle(), Toast.LENGTH_SHORT).show();
                // Inicia una nueva actividad para mostrar detalles de la película
                Intent intent = new Intent(getActivity(), MovieActivity.class);
                intent.putExtra("movie", movie);
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(Movie movie) {
                // Obtiene el usuario actual autenticado en Firebase
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

                if (currentUser != null) {
                    String userEmail = currentUser.getEmail();
                    // Intenta guardar la película en la lista de favoritos del usuario
                    try {
                        DBManager.setUserFavorite(AppPersistance.user.getUser_id(), movie);
                        Toast.makeText(
                                getContext(),
                                movie.getTitle() + " "+getString(R.string.save_as_favorite),
                                Toast.LENGTH_SHORT
                        ).show();

                    } catch (Exception e) {
                        // Muestra un mensaje de error si ocurre un problema con la base de datos
                        Toast.makeText(
                                getContext(),
                                getString(R.string.error_saving_favorites),
                                Toast.LENGTH_SHORT
                        ).show();
                        Log.e("Error", "Error en DB", e);
                    }
                } else {
                    Toast.makeText(
                            getContext(),
                            getString(R.string.start_session_to_save_as_favorite),
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }
        });
        // Asigna el adaptador al RecyclerView
        binding.recyclerView.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

