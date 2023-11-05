package com.swiftyticket.services.implementations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.swiftyticket.exceptions.EventNotFoundException;
import com.swiftyticket.exceptions.PurchaseException;
import com.swiftyticket.exceptions.TicketNotFoundException;
import com.swiftyticket.exceptions.UserNotFoundException;
import com.swiftyticket.exceptions.ZoneNotFoundException;
import com.swiftyticket.models.Event;
import com.swiftyticket.models.Ticket;
import com.swiftyticket.models.User;
import com.swiftyticket.models.Zones;
import com.swiftyticket.repositories.EventRepository;
import com.swiftyticket.repositories.TicketRepository;
import com.swiftyticket.repositories.UserRepository;
import com.swiftyticket.repositories.ZoneRepository;
import com.swiftyticket.services.TicketService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Service
@AllArgsConstructor
@Slf4j
public class TicketServiceImpl implements TicketService {
    private final TicketRepository ticketRepo;
    private final JwtServiceImpl jwtService;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final ZoneRepository zoneRepository;

    @Override
    public List<Ticket> listTickets() {
        return ticketRepo.findAll();
    }

    @Override
    public Ticket getTicket(Integer id) {
        return ticketRepo.findById(id).map(ticket -> {
            return ticket;
        }).orElseThrow( () -> new TicketNotFoundException(id) );
    }

    @Override
    public Ticket purchaseTicket(String bearerToken, Integer eventId, Integer zoneId) {
        //get event and corresponding zones. 
        String jwtToken = bearerToken.substring(7);
        String userEmail = jwtService.extractUserName(jwtToken);
        // get Event and user respectively.
        Event purchase4Event = eventRepository.findById(eventId).orElseThrow(() -> new EventNotFoundException(eventId));
        User purchasingUser = userRepository.findByEmail(userEmail).orElseThrow(() -> new UserNotFoundException());
        //we search for zone using both event and zoneid to make sure the zone is in the specified event.
        Zones purchase4Zone = zoneRepository.findByZoneIdAndEvent(zoneId, purchase4Event).orElseThrow(() -> new ZoneNotFoundException("Invalid zone for " + purchase4Event.getEventName()));

        //create the ticket object, give it to respective zone & user.
        //begin by checking if the user who's trying to buy the ticket is a winner for the zone
        log.info(purchase4Zone.getWinnerList().toString());
        log.info(""+purchase4Zone.getWinnerList().get(0).getUserId());
        log.info("" + purchasingUser);
        log.info(""+purchasingUser.getUserId());
        if( !( purchase4Zone.getWinnerList().contains(purchasingUser) ) ){
            log.info("user tried to purchase ticket for zone they didn't win. get outta here!");
            
            throw new PurchaseException();
        }
        //if reach this part of the code, means the user is a winner, we can continue with the purchase.
        Ticket purchasedTicket = new Ticket(purchase4Zone, purchasingUser);
        ticketRepo.save(purchasedTicket);
        //assign to corresponding zone and user for the relationship
        purchasingUser.getTicketsBought().add(purchasedTicket);
        purchase4Zone.getTicketList().add(purchasedTicket);

        //reduce the amount of tickets available for purchase in that zone by 1.
        purchase4Zone.setTicketsLeft(purchase4Zone.getTicketsLeft()-1);

        //now that they have purchased a ticket, remove them from the winnerList for zone and zonesWon for user.
        //this is to prevent them from purchasing another ticket again.
        purchase4Zone.getWinnerList().remove(purchasingUser);
        purchasingUser.getZonesWon().remove(purchase4Zone);

        //now save em in the repo and we gucci
        userRepository.save(purchasingUser);
        zoneRepository.save(purchase4Zone);

        //return the purchased ticket
        return purchasedTicket;
    }

    @Override
    public List<Ticket> getAllUserTickets(Integer userId){
        return new ArrayList<>();
    }

}