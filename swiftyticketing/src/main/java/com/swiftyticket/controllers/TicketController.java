package com.swiftyticket.controllers;

import java.util.List;

import com.swiftyticket.dto.ticket.PurchaseTicketDTO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.swiftyticket.exceptions.TicketNotFoundException;
import com.swiftyticket.models.Ticket;
import com.swiftyticket.repositories.TicketRepository;
import com.swiftyticket.services.TicketService;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@Slf4j
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
    
    @PostMapping("/tickets/purchase/eventId={eventId}/zoneId={zoneId}")
    public ResponseEntity<Ticket> addTicket(@RequestBody @Valid PurchaseTicketDTO purchaseTicketRequest, @PathVariable Integer eventId, @PathVariable Integer zoneId){
        log.info("Purchase ticket request: {}", purchaseTicketRequest);
        return new ResponseEntity<Ticket>(ticketService.purchaseTicket(purchaseTicketRequest, eventId, zoneId), HttpStatus.CREATED);
    }

    @GetMapping("/tickets/user/{email}")
    public ResponseEntity<List<Ticket>> getUserTickets(@PathVariable String email){
        return new ResponseEntity<List<Ticket>>(ticketService.getAllUserTickets(email), HttpStatus.OK);
    }

}
