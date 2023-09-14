package com.swiftyticket.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.swiftyticket.models.Ticket;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Integer> {}
