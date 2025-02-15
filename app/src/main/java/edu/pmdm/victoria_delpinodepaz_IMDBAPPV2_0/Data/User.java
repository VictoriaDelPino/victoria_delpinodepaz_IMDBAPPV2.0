package edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Data;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class User implements Parcelable {
    private String user_id;
    private String name;
    private String image;
    private String email;
    private String address;
    private ArrayList<Activity_log> activityLogs;
    private String phone;

    public User(ArrayList<Activity_log> activityLogs, String address, String email, String image, String name, String phone, String user_id) {
        this.activityLogs = activityLogs;
        this.address = address;
        this.email = email;
        this.image = image;
        this.name = name;
        this.phone = phone;
        this.user_id = user_id;
    }
    public User() {
    }

    protected User(Parcel in) {
        user_id = in.readString();
        name = in.readString();
        image = in.readString();
        email = in.readString();
        address = in.readString();
        phone = in.readString();
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

    public ArrayList<Activity_log> getActivityLogs() {
        return activityLogs;
    }

    public void setActivityLogs(ArrayList<Activity_log> activityLogs) {
        this.activityLogs = activityLogs;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    @Override
    public String toString() {
        return "User{" +
                "activityLogs=" + activityLogs +
                ", user_id='" + user_id + '\'' +
                ", name='" + name + '\'' +
                ", image='" + image + '\'' +
                ", email='" + email + '\'' +
                ", address='" + address + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(user_id, user.user_id) && Objects.equals(name, user.name) && Objects.equals(image, user.image) && Objects.equals(email, user.email) && Objects.equals(address, user.address) && Objects.equals(activityLogs, user.activityLogs) && Objects.equals(phone, user.phone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user_id, name, image, email, address, activityLogs, phone);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(user_id);
        parcel.writeString(name);
        parcel.writeString(image);
        parcel.writeString(email);
        parcel.writeString(address);
        parcel.writeString(phone);
    }
}
