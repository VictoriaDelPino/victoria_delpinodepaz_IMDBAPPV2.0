package edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Data;

import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Movies.Movie;

public class Favorite extends Movie {
    public Favorite(String description, String title, String photo, String releaseDate, String id){
        super(description,title, photo,releaseDate,id,"");
    }

    public Favorite(){
        super();
    }
}
