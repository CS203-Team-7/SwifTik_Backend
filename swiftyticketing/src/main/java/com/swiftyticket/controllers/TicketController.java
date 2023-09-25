package com.swiftyticket.controllers;

import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.swiftyticket.exceptions.TicketNotFoundException;
import com.swiftyticket.models.Ticket;
import com.swiftyticket.repositories.TicketRepository;
import com.swiftyticket.services.TicketService;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class TicketController {
    
    private TicketService ticketService;

    public TicketController(TicketRepository ticketRepository, TicketService ticketService) {
        this.ticketService = ticketService;
    }
    
    @GetMapping ("/tickets")
    public List<Ticket> getTickets() {
        return ticketService.listTickets();
    }
    
    @GetMapping ("/tickets/{id}")
    public Ticket findTicket(@PathVariable Integer id) {
        Ticket ticket = ticketService.getTicket(id);

        if (ticket == null) throw new TicketNotFoundException(id);
        return ticket;
    }
    
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/tickets")
    public Ticket addTicket(@RequestBody Ticket ticket){
        return ticketService.addTicket(ticket);
    }
    
    @PutMapping("/tickets/{id}")
    public Ticket updateTicket(@PathVariable Integer id, @RequestBody Ticket newTicket) throws TicketNotFoundException {
        Ticket ticket = ticketService.updateTicket(id, newTicket);
        if (ticket == null) throw new TicketNotFoundException(id);
        return ticket;
    }

    @DeleteMapping("/tickets/{id}")
    public String deleteTicket(@PathVariable Integer id) {
        try {
            ticketService.deleteTicket(id);
        } catch(EmptyResultDataAccessException e) {
            throw new TicketNotFoundException(id);
        }
        return "Ticket "+ id + " deleted.";
    }
}