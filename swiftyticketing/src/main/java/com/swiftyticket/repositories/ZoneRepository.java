package com.swiftyticket.repositories;

import org.springframework.stereotype.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.swiftyticket.models.Event;
import com.swiftyticket.models.Zones;


@Repository
public interface ZoneRepository extends JpaRepository<Zones, Integer>{
    Optional<Zones> findByZoneNameAndEvent(String zoneName, Event event);

}
