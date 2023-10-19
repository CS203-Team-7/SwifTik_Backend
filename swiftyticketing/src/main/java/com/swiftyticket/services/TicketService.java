package com.swiftyticket.services;

import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;

import com.swiftyticket.models.Ticket;

public interface TicketService {
    List<Ticket> listTickets();
    Ticket getTicket(Integer id);
    Ticket purchaseTicket(String bearerToken, Integer eventId, Integer zoneId);
    void deleteTicket(Integer id);
}
