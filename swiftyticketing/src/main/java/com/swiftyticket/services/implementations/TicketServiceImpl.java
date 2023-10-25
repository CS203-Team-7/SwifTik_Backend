package com.swiftyticket.services.implementations;

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

    /**
     * Returns a list of all tickets in the DB irrespective of zones.
     * @return List<Ticket>
     */
    @Override
    public List<Ticket> listTickets() {
        return ticketRepo.findAll();
    }

    /**
     * Returns a single ticket based on the ticket ID.
     * @param id -> Integer ticket ID (Unique identifier)
     * @return Ticket -> Ticket object with the specified ID
     */
    @Override
    public Ticket getTicket(Integer id) {
        return ticketRepo.findById(id).map(ticket -> {
            return ticket;
        }).orElseThrow( () -> new TicketNotFoundException(id) );
    }

    /**
     * This function is to allow a user to purchase a ticket for an event.
     * @param bearerToken -> the token of the user who's trying to purchase the ticket
     * @param eventId -> the event id of the zone the user is trying to purchase the ticket for
     * @param zoneId -> the zone id of the zone the user is trying to purchase the ticket for
     * @throws PurchaseException -> if the user is not a winner for the zone
     * @throws EventNotFoundException -> if the event ID does not exist in the DB
     * @throws UserNotFoundException -> if the user does not exist in the DB
     * @throws ZoneNotFoundException -> if the zone ID does not exist in the DB
     * @return Ticket -> the ticket that was purchased
     */
    @Override
    public Ticket purchaseTicket(String bearerToken, Integer eventId, Integer zoneId) {
        //get event and corresponding zones. 
        String jwtToken = bearerToken.substring(7);
        String userEmail = jwtService.extractUserName(jwtToken);
        // get Event and user respectively.
        Event purchase4Event = eventRepository.findById(eventId).orElseThrow(() -> new EventNotFoundException(eventId));
        User purchasingUser = userRepository.findByEmail(userEmail).orElseThrow(() -> new UserNotFoundException("Invalid user / token!"));
        //we search for zone using both event and zoneid to make sure the zone is in the specified event.
        Zones purchase4Zone = zoneRepository.findByZoneIdAndEvent(zoneId, purchase4Event).orElseThrow(() -> new ZoneNotFoundException("Invalid zone for " + purchase4Event.getEventName()));

        //create the ticket object, give it to respective zone & user.
        //begin by checking if the user who's trying to buy the ticket is a winner for the zone
        if (!(purchase4Zone.getWinnerList().contains(purchasingUser))) {
            log.info("user tried to purchase ticket for zone they didn't win. get outta here!");
            //to do: throw exception!
            throw new PurchaseException();
        }
        //if reach this part of the code, means the user is a winner, we can continue with the purchase.
        Ticket purchasedTicket = new Ticket(purchase4Zone, purchasingUser);
        ticketRepo.save(purchasedTicket);
        //assign to corresponding zone and user for the relationship
        purchasingUser.getTicketsBought().add(purchasedTicket);
        purchase4Zone.getTicketList().add(purchasedTicket);

        //reduce the amount of tickets available for purchase in that zone by 1.
        purchase4Zone.setTicketsLeft(purchase4Zone.getTicketsLeft() - 1);

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

    /**
     * Deletes a ticket from the DB based on the ticket ID.
     * @param id -> Integer ticket ID (Unique identifier)
     * @throws TicketNotFoundException -> if the ticket ID does not exist in the DB
     */
    @Override
    public void deleteTicket(Integer id) {
        Optional<Ticket> t = ticketRepo.findById(id);
        if (t == null) throw new TicketNotFoundException(id);

        Ticket ticket = t.get();
        ticketRepo.deleteById(ticket.getTicketId());
    }
}