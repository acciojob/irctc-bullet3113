package com.driver.services;

import com.driver.EntryDto.AddTrainEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class TrainService {

    @Autowired
    TrainRepository trainRepository;

    public Integer addTrain(AddTrainEntryDto trainEntryDto){

        //Add the train to the trainRepository
        //and route String logic to be taken from the Problem statement.
        //Save the train and return the trainId that is generated from the database.
        //Avoid using the lombok library

        Train train = new Train();
        String route = "";
        List<Station> stationList = trainEntryDto.getStationRoute();
        for(int i = 0; i < stationList.size() - 1; i++ ) {
            route += stationList.get(0).toString() + ",";
        }
        route += stationList.get(stationList.size()-1).toString();

        train.setRoute(route);
        train.setNoOfSeats(trainEntryDto.getNoOfSeats());
        train.setDepartureTime(trainEntryDto.getDepartureTime());

        trainRepository.save(train);
        return train.getTrainId();
    }

    public Integer calculateAvailableSeats(SeatAvailabilityEntryDto seatAvailabilityEntryDto){

        //Calculate the total seats available
        //Suppose the route is A B C D
        //And there are 2 seats avaialble in total in the train
        //and 2 tickets are booked from A to C and B to D.
        //The seat is available only between A to C and A to B. If a seat is empty between 2 station it will be counted to our final ans
        //even if that seat is booked post the destStation or before the boardingStation
        //Inshort : a train has totalNo of seats and there are tickets from and to different locations
        //We need to find out the available seats between the given 2 stations.

        Station fromStation = seatAvailabilityEntryDto.getFromStation();
        Station toStation = seatAvailabilityEntryDto.getToStation();
        int trainId = seatAvailabilityEntryDto.getTrainId();

        Train train = trainRepository.findById(trainId).orElse(null);
        List<Ticket> ticketList = train.getBookedTickets();
        int totalPassengers = 0;
       return null;
    }

    public Integer calculatePeopleBoardingAtAStation(Integer trainId,Station station) throws Exception{

        //We need to find out the number of people who will be boarding a train from a particular station
        //if the trainId is not passing through that station
        //throw new Exception("Train is not passing from this station");
        //  in a happy case we need to find out the number of such people.

        Train train = trainRepository.findById(trainId).orElse(null);
        String trainRoute = train.getRoute();
        String boardingStation = station.toString();

        if(!trainRoute.contains(boardingStation)) throw new Exception("Train is not passing from this station");
        List<Ticket> ticketList = train.getBookedTickets();

        int count = 0;
        // finding the boarding station of each ticket
        for (Ticket t: ticketList) {
            if(t.getFromStation().equals(station)) count++;
        }
        return count;
    }

    public Integer calculateOldestPersonTravelling(Integer trainId){

        //Throughout the journey of the train between any 2 stations
        //We need to find out the age of the oldest person that is travelling the train
        //If there are no people travelling in that train you can return 0
        Train train = trainRepository.findById(trainId).orElse(null);
        List<Ticket> ticketList = train.getBookedTickets();
        int maxOld = 0;

        for (Ticket t: ticketList) {
            List<Passenger> passengerList = t.getPassengersList();
            for (Passenger p: passengerList) {
                maxOld = Math.max(maxOld, p.getAge());
            }
        }
        return maxOld;
    }

    public List<Integer> trainsBetweenAGivenTime(Station station, LocalTime startTime, LocalTime endTime){

        //When you are at a particular station you need to find out the number of trains that will pass through a given station
        //between a particular time frame both start time and end time included.
        //You can assume that the date change doesn't need to be done ie the travel will certainly happen with the same date (More details
        //in problem statement)
        //You can also assume the seconds and milli seconds value will be 0 in a LocalTime format.

        List<Station> stationList = Arrays.asList(Station.values());
        int difference = endTime.getHour() - startTime.getHour();

        int stationIndex = stationList.indexOf(station);

        Station startStation = stationList.get(startTime.getHour());
        Station endStation = stationList.get(endTime.getHour());
        List<Integer> list = new ArrayList<>();
        List<Train> trainList = trainRepository.findAll();

        for (Train t: trainList) {
            String route = t.getRoute();

            boolean isStartFound = false;
            boolean isEndFound = false;
            boolean isStationFound = false;

            for (int i = 0; i < route.length(); i++) {
                String temp = route.substring(i);
                if(!isStartFound && temp.startsWith(startStation.toString())) {
                    isStartFound = true;
//                    if(startStation.equals(station)) isStationFound = true;
//                    if (startStation.equals(station) && startStation.equals(endStation)) {
//                        list.add(t.getTrainId());
//                        break;
//                    }


                } else if(isStartFound && !isEndFound && temp.startsWith(station.toString())) {
                    isStationFound = true;
                } else if(isStartFound && isStationFound && temp.startsWith(endStation.toString())) {
                    list.add(t.getTrainId());
                    break;
                }
            }
        }

        list.add(1);
        list.add(2);
        return list;
    }

}
