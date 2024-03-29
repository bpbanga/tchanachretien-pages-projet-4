package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket , boolean discount){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        // FIX timestamp tronqué par la BDD
        //long inHour = Math.round(ticket.getInTime().getTime()/1000)*1000;
        //long outHour = Math.round(ticket.getOutTime().getTime()/1000)*1000;
        long inHour = ticket.getInTime().getTime();
        long outHour = ticket.getOutTime().getTime();

        //TODO: Some tests are failing here. Need to check if this logic is correct
        double duration = (outHour - inHour)/ (1000 * 3600D);
         if ( duration <= 0.5)   {
             ticket.setPrice(0);                        
         } else{

                 switch (ticket.getParkingSpot().getParkingType()){
                case CAR: {
                
                    ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR);
                    break;
                }
                case BIKE: {
                    ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR);
                    break;
                }
                default: throw new IllegalArgumentException("Unkown Parking Type");
                        
             }
             
         }
         if (discount == true){
                 ticket.setPrice(ticket.getPrice() * 0.95);
             } 
       
           
        }
     public void calculateFare(Ticket ticket ){
         calculateFare(ticket , false);

         }
         
    }
