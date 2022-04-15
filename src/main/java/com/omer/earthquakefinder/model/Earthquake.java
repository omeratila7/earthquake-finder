package com.omer.earthquakefinder.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import java.util.Date;
import java.util.List;

public class Earthquake {

    @Autowired
    Environment environment;
    private String place;
    private Double mag;
    private Date time;
    private String country;

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public Double getMag() {
        return mag;
    }

    public void setMag(Double mag) {
        this.mag = mag;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}
