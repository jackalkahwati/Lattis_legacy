package com.lattis.ellipse.presentation.model;

/**
 * Created by raverat on 4/19/17.
 */

public class PersonModel {

    public static class Name {
        public String title;
        public String first;
        public String last;
    }

    public static class Location {
        public String street;
        public String city;
        public String state;
        public String postcode;
    }

    public static class Picture {
        public static String large;
        public static String medium;
        public static String thumbnail;
    }

    public String name;
    public Location location;
    public String email;
    public String phone;
    public String cell;
    public String contactId;
    public Picture picture;
    public boolean isSelected = false;

    public PersonModel() {
    }


}
