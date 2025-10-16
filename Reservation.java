/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Network_Project;

import java.time.LocalDate;


/**
 *
 * @author hetaf
 */
public class Reservation {

   
    private String username;      
    private String carId;          // rented car
    private LocalDate startDate;   // start date
    private int days;              // duration (1–7 days)

    // ===== Constructor =====
    public Reservation( String username, String carId, LocalDate startDate, int days) {
      
        this.username = username;
        this.carId = carId;
        this.startDate = startDate;
        this.days = days;
    }

    // ===== Getters / Setters =====
 

    public String getUsername()
    { return username; }
    public void setUsername(String username)
    { this.username = username; }

    public String getCarId() 
    { return carId; }
    public void setCarId(String carId)
    { this.carId = carId; }

    public LocalDate getStartDate() 
    { return startDate; }
    public void setStartDate(LocalDate startDate)
    { this.startDate = startDate; }

    public int getDays() 
    { return days; }
    public void setDays(int days)
    { this.days = days; }

    // ===== Helper: end date (exclusive) =====
    public LocalDate getEndDateExclusive() {
        return startDate.plusDays(days);
    }


}//end of class 

