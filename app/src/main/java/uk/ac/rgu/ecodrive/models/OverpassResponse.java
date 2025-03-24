package uk.ac.rgu.ecodrive.models;

import java.util.List;

public class OverpassResponse {
    public List<Element> elements;

    public static class Element {
        public Tags tags;
    }

    public static class Tags {
        public String maxspeed;
    }
}