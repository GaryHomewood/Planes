package uk.co.garyhomewood.planes.app.model;

import java.io.Serializable;

/**
 * Created by user1 on 05/03/2014.
 */
public class Plane implements Serializable {
    private final String manufacturer;
    private final String model;
    private final String airline;
    private final String registration;
    private final String imageSrc;

    public Plane(String manufacturer, String model, String airline, String registration, String imageSrc) {
        this.manufacturer = manufacturer;
        this.model = model;
        this.airline = airline;
        this.registration = registration;
        this.imageSrc = imageSrc;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getModel() {
        return model;
    }

    public String getAirline() {
        return airline;
    }

    public String getRegistration() {
        return registration;
    }

    public String getImageSrc() {
        return imageSrc;
    }

    public String getSmallImageUrl() {
        return imageSrc.replace("thumbnail", "640");
    }

    public String getMediumlImageUrl() {
        return imageSrc.replace("thumbnail", "950");
    }
}
