package com.swiftyticket.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.swiftyticket.models.Event;

@Repository
public interface EventRepository extends JpaRepository<Event, Integer> {}
