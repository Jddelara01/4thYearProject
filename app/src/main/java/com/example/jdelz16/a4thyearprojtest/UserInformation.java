package com.example.jdelz16.a4thyearprojtest;

/**
 * Created by jdelz16 on 23/01/2018.
 */

public class UserInformation {

    public String userName;
    public String availability;
    public String exerciseType;
    public double latitude;
    public double longtitude;
    public String uniqueID;
    public double ratingAvg;

   /*public UserInformation() {

    }

    public UserInformation(String availability, String exerciseType) {
        this.availability = availability;
        this.exerciseType = exerciseType;
    }*/

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public String getExerciseType() {
        return exerciseType;
    }

    public void setExerciseType(String exerciseType) {
        this.exerciseType = exerciseType;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongtitude() {
        return longtitude;
    }

    public void setLongtitude(double longtitude) {
        this.longtitude = longtitude;
    }

    public String getUniqueID() {
        return uniqueID;
    }

    public void setUniqueID(String uniqueID) {
        this.uniqueID = uniqueID;
    }

    public double getRatingAvg() {
        return ratingAvg;
    }

    public void setRatingAvg(double ratingAvg) {
        this.ratingAvg = ratingAvg;
    }
}
