package com.swiftyticket.services.implementations;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.swiftyticket.dto.zone.PreRegisterRequest;
import com.swiftyticket.dto.zone.ZoneRequest;
import com.swiftyticket.models.Event;
import com.swiftyticket.models.User;
import com.swiftyticket.models.Zones;
import com.swiftyticket.repositories.EventRepository;
import com.swiftyticket.repositories.UserRepository;
import com.swiftyticket.repositories.ZoneRepository;
import com.swiftyticket.services.ZoneService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.swiftyticket.exceptions.AlreadyPreRegisteredException;
import com.swiftyticket.exceptions.EventClosedException;
import com.swiftyticket.exceptions.EventNotFoundException;
import com.swiftyticket.exceptions.UserNotFoundException;
import com.swiftyticket.exceptions.WrongZoneDateException;
import com.swiftyticket.exceptions.ZoneNotFoundException;


@Service
@RequiredArgsConstructor
@Slf4j
public class ZoneServiceImpl implements ZoneService {

    private final ZoneRepository zoneRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final SmsServiceImpl smsServ;

    /**
     * Adds a new zone to a specified event.
     * @param zoneReq -> ZoneRequest object containing the zone details
     * @param event -> Event object that the zone should be assigned to
     * @throws EventNotFoundException -> if the event ID does not exist in the DB
     * @return Zone -> Zone object that was added to the DB
     */
    public Zones addZone(ZoneRequest zoneReq, Event event){
        // check if event has a date for the zone's date. If event does not have a date for that zone, throw an error
        if( !(event.getDates().contains(zoneReq.getZoneDate())) ){
            log.info("admin tried to create a zone with a date it's corresponding event does not have!");
            throw new WrongZoneDateException(event, zoneReq.getZoneDate());
        }

        Zones newZone = new Zones(zoneReq.getZoneCapacity(), zoneReq.getZoneName(), zoneReq.getZoneDate(), zoneReq.getTicketPrice(), event);
        newZone = zoneRepository.save(newZone);

        // Add this zone to the event it belongs to
        event.getZoneList().add(newZone);
        eventRepository.save(event);
        return newZone;
    }

    /**
     * Returns a list of all zones in the DB corresponding to the specified event.
     * @param event -> Event object that the zones should be retrieved from
     * @throws EventNotFoundException -> if the event ID does not exist in the DB
     * @return List<Zones> -> List of Zone objects that belong to the specified event
     */
    public List<Zones> listZones(Event event){
        return event.getZoneList();
    }

    /**
     * Allows a user to join the raffle for a specified zone.
     * @param registerRequest -> PreRegisterRequest object containing the user's email
     * @param eventId -> the event id of the zone the user is trying to join the raffle for
     * @param zoneID -> the zone id of the zone the user is trying to join the raffle for
     * @throws EventNotFoundException -> if the event ID does not exist in the DB
     * @throws UserNotFoundException -> if the user does not exist in the DB
     * @throws ZoneNotFoundException -> if the zone ID does not exist in the DB
     * @throws AlreadyPreRegisteredException -> if the user has already pre-registered for the event
     * @throws EventClosedException -> if the event is not open for pre-registration
     * @return String message to indicate success or failure
     */
    public String joinRaffle(PreRegisterRequest registerRequest, Integer eventId, Integer zoneID){
        // get Event and user respectively.
        Event joinEvent = eventRepository.findById(eventId).orElseThrow(() -> new EventNotFoundException(eventId));
        // we search for zone using both event and zoneid to make sure the zone is in the specified event.
        Zones joinZone = zoneRepository.findByZoneIdAndEvent(zoneID, joinEvent).orElseThrow(() -> new ZoneNotFoundException("Invalid zone for " + joinEvent.getEventName()));

        if(!joinEvent.getOpenStatus()){
            log.info("User tried to join when pre-registration was closed, Denied.");
            throw new EventClosedException();
        }

        log.info(registerRequest.getEmail());
        User joiningUser = userRepository.findByEmail(registerRequest.getEmail()).orElseThrow(() -> new UserNotFoundException());

        if(joinEvent.getPreRegisteredUsers4Event().contains(joiningUser)){
            log.info("User tried to join when already pre-registered, Denied.");
            throw new AlreadyPreRegisteredException(joinEvent);
            
        }

        joinZone.getPreRegisteredUsers4Zone().add(joiningUser);
        joiningUser.getPreRegisteredZones().add(joinZone);
        joiningUser.getPreRegisteredEvents().add(joinEvent);
        // also add user to event they joined so they cant join other zones with the same event
        joinEvent.getPreRegisteredUsers4Event().add(joiningUser);
        joinZone.setUser_count(joinZone.getPreRegisteredUsers4Zone().size());
        joinEvent.setUser_count(joinEvent.getPreRegisteredUsers4Event().size());
        eventRepository.save(joinEvent);
        userRepository.save(joiningUser);
        zoneRepository.save(joinZone);
        return "Successfully joined the raffle for: " + joinZone.getZoneName() + " on " + joinZone.getZoneDate() + " for " + joinEvent.getEventName();
        
    }

    /**
     * Below is our main ticketing algorithm to assign tickets to users who have won the raffle.
     * This function is to perform the raffling of each zone in the event once pre-registration is closed.
     * @param zone -> Zone object that the raffle should be performed on
     *             -> This function is called by the EventService class
     * @throws EventNotFoundException -> if the event ID does not exist in the DB
     */
    public void raffle(Zones zone){
        // get associated event with zone
        Event event = zone.getEvent();

        // if raffle round >= 1, means we have to clear the previous round winners.
        if(event.getRaffleRound() >= 1){
            // get previous list of users, clear this zone from their list.
            List<User> previousWinners = zone.getWinnerList();  
            for(User previousWin : previousWinners){
                previousWin.getZonesWon().remove(zone);
            }
            zone.setWinnerList(new ArrayList<User>());
        }

        // get list of users who have pre registered for this zone
        // record size of the list of users.
        List<User> toRaffle = zone.getPreRegisteredUsers4Zone();
        List<User> userWinners = new ArrayList<>();

        // get number of tickets/seats available (this is how many winners we will be selecting)
        int ticketsLeft = zone.getTicketsLeft();

        // if ticketsLeft > toRaffleSize (means no need to raffle, everyone wins!)
        Random rand = new Random();

        // count how many people have won so far, once == zoneCap stop. (no more tickets left to raffle!)
        // select winners until there are no tickets left (until ticketsLeft <= count)
        while(ticketsLeft > userWinners.size() && toRaffle.size()>0){
            // this will roll a number between 0 -> (size of arraylist-1)
            int luckyNumber = rand.nextInt(toRaffle.size());
            User winner = toRaffle.get(luckyNumber);
            userWinners.add(winner);
            // remove winner from next draw, which will in turn update the size. 
            toRaffle.remove(winner);
            log.info("raffled! " + userWinners.size());
            }
        // update zone preRegistered user list.
        zone.setPreRegisteredUsers4Zone(toRaffle);
        // update zone winnerlist
        zone.setWinnerList(userWinners);
        // update count for registered user for zone
        zone.setUser_count(toRaffle.size());
        zoneRepository.save(zone);

        // for all winners, update their user class (and event class) accordingly.
        // event class -> preRegisteredUsers4Event needs to be updated (remove winners)
        // user class -> remove their zone and event pre-registration once they have won. 
        // to do: add a field for users to check which zones they won (store zone id)
        for(int i=0; i<userWinners.size(); i++){
            User u = userWinners.get(i);
            //remove their pre-registration once they won
            u.getPreRegisteredEvents().remove(event);
            u.getPreRegisteredZones().remove(zone);
            // add the zone they won to zoneswon. (to facilitate ticket purchasing later)
            u.getZonesWon().add(zone);
            userRepository.save(u);

            // remove accordingly from event pre-registration list.
            event.getPreRegisteredUsers4Event().remove(u);
            // update count for pre_registered users for an event.
            event.setUser_count(event.getPreRegisteredUsers4Event().size());
            eventRepository.save(event);
            log.info("winner no." + i + " zones won" + u.getZonesWon());
            log.info("winner no." + i + " preRegisteredZones" + u.getPreRegisteredZones());
            log.info("winner no." + i + " preRegisteredEvent" + u.getPreRegisteredEvents());

        // now we want to message the winners who won
        String congratz = "Congratulations " + u.getEmail() + "! You have won the raffle for the event: " + event.getEventName() + ", for the zone: " + zone.getZoneName();
        smsServ.sendCongratz(congratz, u.getPhoneNumber());
        }

        // check everything is updated accordingly.
        log.info("event preResigtered list: " + event.getPreRegisteredUsers4Event());
        log.info("zone preRegistered list: " + zone.getPreRegisteredUsers4Zone());
        log.info("zone winner list:" + zone.getWinnerList());
        return;
    }

    /**
     * Returns a list of all zones that a user has pre-registered for.
     * @param email -> the email of the user
     * @throws UserNotFoundException -> if the user does not exist in the DB
     */
    public List<Zones> userJoinedZones(String email){
        User uzer = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException());
        return uzer.getPreRegisteredZones();
    }

    /**
     * Returns the corresponding event for a specified zone.
     * @param zoneId -> the unique identifier of the zone
     * @throws ZoneNotFoundException -> if the zone ID does not exist in the DB
     */
    public Event getCorrespondingEvent(Integer zoneID){
        Zones zone = zoneRepository.findById(zoneID).orElseThrow( () -> new ZoneNotFoundException("invalid zone") );
        return zone.getEvent();
    }
}
