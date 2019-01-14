package cyclone;

import java.util.ArrayList;

public class ModelInput {

    private float latitude;
    private float longitude;
    private float nextLatitude;
    private float nextLongitude;
    private float maxWindSpeed;
    private float maxWindRadius;
    private float speed;

    private float course;

    private ArrayList<Float> bounds;


    public ModelInput(){
        bounds = new ArrayList<>();
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public float getNextLatitude() {
        return nextLatitude;
    }

    public void setNextLatitude(float nextLatitude) {
        this.nextLatitude = nextLatitude;
    }

    public float getNextLongitude() {
        return nextLongitude;
    }

    public void setNextLongitude(float nextLongitude) {
        this.nextLongitude = nextLongitude;
    }

    public float getMaxWindSpeed() {
        return maxWindSpeed;
    }

    public void setMaxWindSpeed(float maxWindSpeed) {
        this.maxWindSpeed = maxWindSpeed;
    }

    public float getMaxWindRadius() {
        return maxWindRadius;
    }

    public void setMaxWindRadius(float maxWindRadius) {
        this.maxWindRadius = maxWindRadius;
    }

    public float getCourse() {
        return course;
    }

    public void setCourse(float course) {
        this.course = course;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public ArrayList<Float> getBounds() {
        return bounds;
    }

    public void setBounds(ArrayList<Float> bounds) {
        this.bounds = bounds;
    }
}
