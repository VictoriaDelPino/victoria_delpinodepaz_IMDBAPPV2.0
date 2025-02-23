package edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Data;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.Objects;

public class User implements Parcelable {
    private String user_id;
    private String name;
    private byte[] image; // Se almacena la imagen como BLOB
    private String email;
    private String address;
    private String login;
    private String logout;
    private String phone;

    public User(String login, String logout, String address, String email, byte[] image, String name, String phone, String user_id) {
        this.login = login;
        this.logout = logout;
        this.address = address;
        this.email = email;
        this.image = image;
        this.name = name;
        this.phone = phone;
        this.user_id = user_id;
    }

    public User() { }

    protected User(Parcel in) {
        user_id = in.readString();
        name = in.readString();
        image = in.createByteArray();
        email = in.readString();
        address = in.readString();
        phone = in.readString();
        login = in.readString();
        logout = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }
        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    // Getters y setters
    public byte[] getImage() {
        return image;
    }
    public void setImage(byte[] image) {
        this.image = image;
    }
    public String getUser_id() {
        return user_id;
    }
    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public String getLogin() {
        return login;
    }
    public void setLogin(String login) {
        this.login = login;
    }
    public String getLogout() {
        return logout;
    }
    public void setLogout(String logout) {
        this.logout = logout;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int flags) {
        parcel.writeString(user_id);
        parcel.writeString(name);
        parcel.writeByteArray(image);
        parcel.writeString(email);
        parcel.writeString(address);
        parcel.writeString(phone);
        parcel.writeString(login);
        parcel.writeString(logout);
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(user_id, user.user_id) &&
                Objects.equals(name, user.name) &&
                Objects.equals(image, user.image) &&
                Objects.equals(email, user.email) &&
                Objects.equals(address, user.address) &&
                Objects.equals(phone, user.phone) &&
                Objects.equals(login, user.login) &&
                Objects.equals(logout, user.logout);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user_id, name, image, email, address, phone, login, logout);
    }
}
