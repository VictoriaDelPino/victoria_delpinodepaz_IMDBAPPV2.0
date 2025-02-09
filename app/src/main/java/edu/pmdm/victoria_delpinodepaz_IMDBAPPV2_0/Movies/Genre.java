package edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Movies;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

/* Clase Genre que representa un género de película.
 Implementa Parcelable para facilitar el paso de objetos entre Activities o Fragments.*/
public class Genre implements Parcelable {
    private int id;
    private String genreName;

    public Genre(String genreName, int id) {
        this.genreName = genreName;
        this.id = id;
    }

    public Genre() {
    }

    protected Genre(Parcel in) {
        id = in.readInt();
        genreName = in.readString();
    }

    public static final Creator<Genre> CREATOR = new Creator<Genre>() {
        @Override
        public Genre createFromParcel(Parcel in) {
            return new Genre(in);
        }

        @Override
        public Genre[] newArray(int size) {
            return new Genre[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(id);
        parcel.writeString(genreName);
    }

    public String getGenreName() {
        return genreName;
    }

    public void setGenreName(String genreName) {
        this.genreName = genreName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    @Override
    public String toString() {
        return "Genre{" +
                "genreName='" + genreName + '\'' +
                ", id=" + id +
                '}';
    }
}
