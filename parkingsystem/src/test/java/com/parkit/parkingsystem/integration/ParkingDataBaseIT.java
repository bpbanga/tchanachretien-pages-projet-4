package com.parkit.parkingsystem.integration;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;
import com.parkit.parkingsystem.service.FareCalculatorService;
import java.util.Date;
import com.parkit.parkingsystem.constants.Fare;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Formatter;
import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import java.sql.ResultSet;
import java.sql.PreparedStatement;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown(){

    }

    @Test
    public void testParkingACar(){
        //Given
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        
        //When

        parkingService.processIncomingVehicle();
        
        // Then
      Ticket ticket = ticketDAO.getTicket("ABCDEF");
      
      assertEquals("ABCDEF", ticket.getVehicleRegNumber()) ;
      //assertEquals(Fare.CAR_RATE_PER_HOUR,ticket.getPrice());
      assertEquals(2 , parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR));
      //TODO: check that a ticket is actualy saved in DB and Parking table is updated with availability
    }

    @Test
    public void testParkingLotExit(){
       // testParkingACar();
        //Given
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        
        parkingService.processIncomingVehicle();
        Ticket ticketIn = ticketDAO.getTicket("ABCDEF");
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (   60 * 60 * 1000) ); 
        ticketIn.setInTime(inTime);
        ticketDAO.saveTicket(ticketIn);
        //When
        parkingService.processExitingVehicle();

        //Then
       Ticket ticket = ticketDAO.getTicket("ABCDEF");
       assertNotEquals( null ,ticket.getOutTime());
       assertEquals(1 , parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR));

        //TODO: check that the fare generated and out time are populated correctly in the database
    }

    @Test
    public void testParkingLotExitRecurringUser(){

      //Given
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        
        parkingService.processIncomingVehicle();
        Ticket ticketIn = ticketDAO.getTicket("ABCDEF");
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (   60 * 60 * 1000) ); 
        ticketIn.setInTime(inTime);
        ticketDAO.saveTicket(ticketIn);
        //When
        parkingService.processExitingVehicle();

        //Then
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        // FIX timestamp tronqué par la BDD
        //long inHour = Math.round(ticket.getInTime().getTime()/1000)*1000;
        //long outHour = Math.round(ticket.getOutTime().getTime()/1000)*1000;
        long inHour = ticket.getInTime().getTime();
        long outHour = ticket.getOutTime().getTime();        
        double duration = (outHour - inHour)/ (1000 * 3600D);
        //float carParkingPrice = (float)(0.95 * Fare.CAR_RATE_PER_HOUR  * duration);
        //float price = (float)(ticket.getPrice());
        NumberFormat nf= NumberFormat.getInstance();
        nf.setMaximumFractionDigits(3);

       
        //System.out.println( "affichage du carParkingPrice " + carParkingPrice);
        //System.out.println("affichage du prix " + price);

        
       assertNotEquals( null ,ticket.getOutTime());
       assertTrue(ticketDAO.getNbTicket("ABCDEF") > 1);
       assertEquals(nf.format(0.95 * Fare.CAR_RATE_PER_HOUR  * duration) , nf.format(ticket.getPrice()) );
       assertEquals(1 , parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR));


    }

}
