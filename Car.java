/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Network_Project;

/**
 * CAR CLASS (Phase 1)
 * -------------------
 * Represents a single car in the rental system.
 * Author: Hetaf — simplified student version
 */

/**
 * Car model (Phase 1)
 */
public class Car {

    private String carId;        
    private String type;         
    private int numOfSeates;    

    public Car(String id, String type, int seats) {
        this.carId = id;
        this.type = type;
        this.numOfSeates = seats;
    }

    public String getCarId() { return carId; }
    public void setCarId(String carId) { this.carId = carId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getNumOfSeates() { return numOfSeates; }
    public void setNumOfSeates(int numOfSeates) { this.numOfSeates = numOfSeates; }

    @Override
    public String toString() {
        return "Car{" + "carId='" + carId + "', type='" + type + "', seats=" + numOfSeates + '}';
    }
}
