package edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

//Clase que representa un fragmento de diálogo para mostrar la lista de películas favoritas en formato json.
public class FavoritesFragment extends DialogFragment {
    private static final String ARG_JSON = "json_arg";
    private String jsonContent;

    //Crea una nueva instancia del fragmento con los datos proporcionados.
    public static FavoritesFragment newInstance(String jsonContent) {
        FavoritesFragment fragment = new FavoritesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_JSON, jsonContent);
        fragment.setArguments(args);
        return fragment;
    }

    //Recupera los argumentos y desactiva la cancelación al tocar fuera del diálogo.
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false); // Evita que se cierre tocando fuera o con el botón atrás
        if (getArguments() != null) {
            jsonContent = getArguments().getString(ARG_JSON);
        }
    }

    //Método que infla el diseño del fragmento y configura sus elementos.
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);

        // Obtiene la referencia del TextView donde se mostrarán las películas favoritas
        TextView jsonMoviesFav = view.findViewById(R.id.txtMoviesList);
        // Asigna el contenido JSON al TextView si está disponible, de lo contrario, muestra un mensaje de error
        if (jsonContent != null) {
            jsonMoviesFav.setText(jsonContent);
        } else {
            jsonMoviesFav.setText(getString(R.string.no_data_available));
        }

        // Obtiene la referencia del botón de cierre y asigna su funcionalidad
        Button btnClose = view.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> dismiss());

        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle("Favoritos");
        dialog.setCanceledOnTouchOutside(false); // Evita cierre al tocar fuera
        return dialog;
    }
}
