package uk.ac.rgu.ecodrive.models;

public class LocationData {
    private double latitude;
    private double longitude;
    private float speed;

    public LocationData(double latitude, double longitude, float speed) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
    }

    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public float getSpeed() { return speed; }

    @Override
    public String toString() {
        return "Lat: " + latitude + ", Lng: " + longitude + ", Speed: " + speed + " km/h";
    }
}