package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.geom.Arc2D.Double;
import java.util.Date;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    private static ParkingService parkingService;

    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;

    @BeforeEach
    private void setUpPerTest() {
        try {
            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }

    @Test
    public void processExitingVehicleTest() throws Exception {
      //Given
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        when(ticketDAO.getNbTicket("ABCDEF")).thenReturn(1D);
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        Ticket ticket = new Ticket();
        ticket.setInTime(new Date());
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");
        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
  
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
        
        //When
        parkingService.processExitingVehicle();

        //Then
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, Mockito.times(1)).getNbTicket(anyString());
    }

    @Test
     void testProcessIncomingVehicle()throws Exception {
        //Given
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
        when(ticketDAO.getNbTicket("ABCDEF")).thenReturn(1D);
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        Ticket ticket = new Ticket();
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");
        ticket.setPrice(0);
        ticket.setInTime(new Date());
        ticket.setOutTime(null);
        when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);

        //When
        parkingService.processIncomingVehicle();

        //Then
        verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class));
    }

    @Test
    public  void processExitingVehicleTestUnableUpdate () throws Exception{
        //Given
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        Ticket ticket = new Ticket();
        ticket.setInTime(new Date());
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");
        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);

        //When
        parkingService.processExitingVehicle();

        //Then
        verify(parkingSpotDAO, Mockito.times(0)).updateParking(any(ParkingSpot.class));

    }

    @Test
    public void testGetNextParkingNumberIfAvailable()  throws Exception{
      //Given
       when(inputReaderUtil.readSelection()).thenReturn(1);
       when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);

       //When
       ParkingSpot result =  parkingService.getNextParkingNumberIfAvailable();
        
      //Then

    assertEquals(result.getId(), 1 );
    assertEquals(result.isAvailable(), true );



    }

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberNotFound() throws Exception{
      //Given
       when(inputReaderUtil.readSelection()).thenReturn(1);
       when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(0);

       //When
       ParkingSpot result =  parkingService.getNextParkingNumberIfAvailable();
        
      //Then

   
    assertEquals(result, null );

    }

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() throws Exception{
      //Given
        when(inputReaderUtil.readSelection()).thenReturn(3);
      //When
       ParkingSpot result =  parkingService.getNextParkingNumberIfAvailable();
      
      //Then
     assertEquals(result, null );
    }


}
