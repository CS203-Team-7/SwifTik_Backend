package com.swiftyticket.controllers;

import java.util.List;

import com.swiftyticket.dto.ticket.TicketForUserDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.swiftyticket.exceptions.TicketNotFoundException;
import com.swiftyticket.models.Ticket;
import com.swiftyticket.repositories.TicketRepository;
import com.swiftyticket.services.TicketService;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class TicketController {
    
    private final TicketService ticketService;

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
    
    @PostMapping("/tickets/purchase/eventId={eventId},zoneId={zoneId}")
    public ResponseEntity<Ticket> addTicket(@RequestHeader("Authorization") String bearerToken, @PathVariable Integer eventId, @PathVariable Integer zoneId){
        return new ResponseEntity<Ticket>(ticketService.purchaseTicket(bearerToken, eventId, zoneId), HttpStatus.CREATED);
    }

    @GetMapping("/tickets/userid={userid}")
    public ResponseEntity<List<Ticket>> getUserTickets(@RequestBody @Valid TicketForUserDTO ticketRequest){
        return new ResponseEntity<List<Ticket>>(ticketService.getAllUserTickets(ticketRequest), HttpStatus.OK);
    }

}
