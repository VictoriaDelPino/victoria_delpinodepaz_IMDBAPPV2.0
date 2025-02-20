package edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Data;

public class PhoneCode {
    private String name;
    private String code;
    private String phoneCode;
    private String flagEmoji;

    public PhoneCode(String code, String flagEmoji, String name, String phoneCode) {
        this.code = code;
        this.flagEmoji = flagEmoji;
        this.name = name;
        this.phoneCode = phoneCode;
    }

    public PhoneCode() {

    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getFlagEmoji() {
        return flagEmoji;
    }

    public void setFlagEmoji(String flagEmoji) {
        this.flagEmoji = flagEmoji;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneCode() {
        return phoneCode;
    }

    public void setPhoneCode(String phoneCode) {
        this.phoneCode = phoneCode;
    }
}
