package com.swiftyticket.services;

import java.util.List;


import com.swiftyticket.dto.ticket.PurchaseTicketDTO;
import com.swiftyticket.dto.ticket.TicketForUserDTO;
import com.swiftyticket.models.Ticket;

public interface TicketService {
    List<Ticket> listTickets();
    Ticket getTicket(Integer id);
    Ticket purchaseTicket(PurchaseTicketDTO purchaseTicketRequest, Integer eventId, Integer zoneId);
    List<Ticket> getAllUserTickets(String email);
}
