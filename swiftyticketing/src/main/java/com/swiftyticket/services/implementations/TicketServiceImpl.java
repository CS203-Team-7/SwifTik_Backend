package com.swiftyticket.services.implementations;

import java.util.List;

import org.springframework.stereotype.Service;

import com.swiftyticket.models.Ticket;
import com.swiftyticket.repositories.TicketRepository;
import com.swiftyticket.services.TicketService;

@Service
public class TicketServiceImpl implements TicketService {
    private TicketRepository ticketRepo;

    public TicketServiceImpl(TicketRepository ticketRepo) {
        this.ticketRepo = ticketRepo;
    }

    @Override
    public List<Ticket> listTickets() {
        return ticketRepo.findAll();
    }

    @Override
    public Ticket getTicket(Integer id) {
        return ticketRepo.findById(id).map(ticket -> {
            return ticket;
        }).orElse(null);
    }

    @Override
    public Ticket addTicket(Ticket ticket) {
        return ticketRepo.save(ticket);
    }

    @Override
    public Ticket updateTicket(Integer id, Ticket newTicketInfo) {
        return ticketRepo.findById(id).map(ticket -> {
            ticket.setTicketPrice(newTicketInfo.getTicketPrice());
            return ticketRepo.save(ticket);
        }).orElse(null);
    }

    @Override
    public void deleteTicket(Integer id) {
        ticketRepo.deleteById(id);
    }
}