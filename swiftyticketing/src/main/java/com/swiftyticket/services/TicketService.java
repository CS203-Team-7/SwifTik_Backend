package com.swiftyticket.services;

import java.util.List;

import com.swiftyticket.models.Ticket;

public interface TicketService {
    List<Ticket> listTickets();
    Ticket getTicket(Integer id);
    Ticket addTicket(Ticket ticket);
    void deleteTicket(Integer id);
}
