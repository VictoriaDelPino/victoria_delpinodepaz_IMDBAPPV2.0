package edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Data;

import java.util.Objects;

public class Activity_log {

    private String login_time;
    private String logout_time;

    public Activity_log(String login_time, String logout_time) {
        this.login_time = login_time;
        this.logout_time = logout_time;
    }

    public String getLogin_time() {
        return login_time;
    }

    public void setLogin_time(String login_time) {
        this.login_time = login_time;
    }

    public String getLogout_time() {
        return logout_time;
    }

    public void setLogout_time(String logout_time) {
        this.logout_time = logout_time;
    }

    @Override
    public String toString() {
        return "Activity_log{" +
                "login_time='" + login_time + '\'' +
                ", logout_time='" + logout_time + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Activity_log that = (Activity_log) o;
        return Objects.equals(login_time, that.login_time) && Objects.equals(logout_time, that.logout_time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(login_time, logout_time);
    }
}
