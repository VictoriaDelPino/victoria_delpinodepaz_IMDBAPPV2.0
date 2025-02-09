package edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.ui.slideshow;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.ApiConnection.ApiTMDB;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Movies.Genre;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.R;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.SearchResultActivity;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.databinding.FragmentSlideshowBinding;

//Fragmento que permite filtar una busqueda de películas por genero y año
public class SlideshowFragment extends Fragment {

    private FragmentSlideshowBinding binding;
    private List<Genre> genreObjectList;
    private List<String> genreList;
    private Spinner genreSpinner;
    private Button btnSearch;
    private Genre selectedGenre;
    private EditText txtYear;
    private String year;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SlideshowViewModel slideshowViewModel =
                new ViewModelProvider(this).get(SlideshowViewModel.class);

        binding = FragmentSlideshowBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Asigna los elementos de la interfaz a variables
        btnSearch = binding.btnSearch;
        genreSpinner = binding.spinnerGenre;
        txtYear = binding.eTxtYear;

        // Obtiene la lista de géneros desde la API
        genreObjectList = ApiTMDB.getGenre();
        genreList = new ArrayList<>();

        // Agrega un elemento por defecto como primer elemento de la lista de géneros
        genreList.add(0, getString(R.string.choose_gerne));
        for (Genre genre : genreObjectList) {
            genreList.add(genre.getGenreName());
        }

        // Configura el adaptador para el Spinner con la lista de géneros
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                genreList
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genreSpinner.setAdapter(adapter);

        // Configura el listener para detectar la selección de un género en el Spinner
        genreSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Si el usuario selecciona la opción por defecto, se muestra en color gris
                if (position == 0) {
                    ((TextView) parent.getChildAt(0)).setTextColor(Color.GRAY);
                    selectedGenre = null;
                } else {
                    // Busca el objeto Genre correspondiente al nombre seleccionado
                    String selectedGenreName = genreList.get(position);
                    for (Genre g : genreObjectList) {
                        if (g.getGenreName().equals(selectedGenreName)) {
                            selectedGenre = g;
                            break;
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Configura el botón para iniciar la búsqueda de películas
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Verifica si el usuario ha seleccionado un género válido
                if (selectedGenre != null) {
                    try {
                        // Obtiene y procesa el año ingresado por el usuario
                        year = txtYear.getText().toString().trim();
                        int yearInt = Integer.parseInt(year);
                        int currentYear = java.time.Year.now().getValue();

                        // Verifica si el año ingresado es válido dentro del rango permitido
                        if (yearInt >= 1888 && yearInt <= currentYear) {
                            // Crea un Intent para iniciar la actividad de búsqueda con los datos ingresados
                            Intent intent = new Intent(requireContext(), SearchResultActivity.class);
                            intent.putExtra("GENRE_ID", selectedGenre.getId());
                            intent.putExtra("YEAR", year);
                            startActivity(intent);
                        } else {
                            Toast.makeText(requireContext(), getString(R.string.year_not_available)+" " + currentYear, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(requireContext(), getString(R.string.year_too_long), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(requireContext(), getString(R.string.invalid_genre), Toast.LENGTH_SHORT).show();
                }
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}