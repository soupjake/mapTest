package com.asuper.maptest;

/**
 * Created by super on 09/06/2017.
 */

public class Weather {

    private String stationName;
    private String condition;
    private String description;
    private String date;

    private double temp;
    private double pressure;
    private double humidity;
    private double windSpeed;
    private double windDeg;
    private double cloudPercentage;
    private double rainPercentage;
    private double snowPercentage;

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getPressure() {
        return pressure;
    }

    public void setPressure(double pressure) {
        this.pressure = pressure;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public double getTemp() {
        return temp;
    }

    public void setTemp(double temp) {
        this.temp = temp;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public double getWindDeg() {
        return windDeg;
    }

    public void setWindDeg(double windDeg) {
        this.windDeg = windDeg;
    }

    public double getCloudPercentage(){
        return cloudPercentage;
    }

    public void setCloudPercentage(double cloudPercentage) {
        this.cloudPercentage = cloudPercentage;
    }

    public double getRainPercentage(){
        return rainPercentage;
    }

    public void setRainPercentage(double rainPercentage) {
        this.rainPercentage = rainPercentage;
    }

    public double getSnowPercentage(){
        return snowPercentage;
    }

    public void setSnowPercentage(double snowPercentage) {
        this.snowPercentage = snowPercentage;
    }

}

