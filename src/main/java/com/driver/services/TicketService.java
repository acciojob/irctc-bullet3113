package com.driver.services;


import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TrainRepository trainRepository;

    @Autowired
    PassengerRepository passengerRepository;


    public Integer bookTicket(BookTicketEntryDto bookTicketEntryDto)throws Exception{

        //Check for validity
        //Use bookedTickets List from the TrainRepository to get bookings done against that train
        // Incase the there are insufficient tickets
        // throw new Exception("Less tickets are available");
        //otherwise book the ticket, calculate the price and other details
        //Save the information in corresponding DB Tables

        Ticket ticket = new Ticket();

        Train train = trainRepository.findById(bookTicketEntryDto.getTrainId()).orElse(null);
        List<Ticket> ticketList = train.getBookedTickets();
        int noOfSeats = bookTicketEntryDto.getNoOfSeats();
        int totalTrainSeats = train.getNoOfSeats();

        if((totalTrainSeats - ticketList.size()) < noOfSeats) throw new Exception("Less tickets are available");


        //Fare System : Check problem statement
        //Incase the train doesn't pass through the requested stations
        //throw new Exception("Invalid stations");

        Station fromStation = bookTicketEntryDto.getFromStation();
        Station toStation = bookTicketEntryDto.getToStation();

        // checking train route first
        String trainRoute = train.getRoute();

        // creating route for our booking
        String customRoute = "";
        List<Station> stationList = Arrays.asList(Station.values());

        // searching for respective station in the list
        boolean isFromStationFound = false;
        boolean isToStationFound = false;
        for (Station s: stationList) {
            if (s.equals(fromStation) && !isFromStationFound) isFromStationFound = true;
            else if(isFromStationFound && s.equals(toStation)) isToStationFound = true;
        }

        if(!isFromStationFound || !isToStationFound) throw new Exception("Invalid stations");

        // calculating fare
        int indexFromStation = stationList.indexOf(fromStation);
        int indexToStation = stationList.indexOf(toStation);

        int distance = indexToStation - indexFromStation;
        int fare = distance * 300;

        ticket.setFromStation(fromStation);
        ticket.setToStation(toStation);
        ticket.setTrain(train);


        // creating passenger list
        List<Passenger> passengerList = new ArrayList<>();
        List<Integer> passengerIds = bookTicketEntryDto.getPassengerIds();

        // filling passengers according to IDs in passengerList
        for (int id: passengerIds) {
            Passenger p = passengerRepository.findById(id).orElse(null);
            passengerList.add(p);
        }

        ticket.setPassengersList(passengerList);
        int totalFare = passengerIds.size() * fare;
        ticket.setTotalFare(totalFare);

        ticketRepository.save(ticket);

        //Save the bookedTickets in the train Object
        ticketList.add(ticket);
        train.setBookedTickets(ticketList);
        trainRepository.save(train);

        //Also in the passenger Entity change the attribute bookedTickets by using the attribute bookingPersonId.
        Passenger bookingPerson = passengerRepository.findById(bookTicketEntryDto.getBookingPersonId()).orElse(null);
        List<Ticket> passengerTickets = bookingPerson.getBookedTickets();
        passengerTickets.add(ticket);
        bookingPerson.setBookedTickets(passengerTickets);
        passengerRepository.save(bookingPerson);

        //And the end return the ticketId that has come from db

       return ticket.getTicketId();

    }
}
