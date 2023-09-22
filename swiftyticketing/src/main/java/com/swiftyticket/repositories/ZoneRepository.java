package com.swiftyticket.repositories;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.swiftyticket.models.Zones;


@Repository
public interface ZoneRepository extends JpaRepository<Zones, Integer>{
    
}
