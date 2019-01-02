package com.inc.bb.smartcampus;

/**
 * Created by s163310 on 11-11-2017.
 */

public class User {
    public String studentnumber;
    public String password;
    public String latitude;
    public String longitude;
    public String salt;

    public User(String studentnumber, String password, String latitude, String longitude){
        this.studentnumber = studentnumber;
        this.password = password;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
