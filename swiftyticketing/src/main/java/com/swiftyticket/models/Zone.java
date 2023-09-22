package com.swiftyticket.models;

import java.util.ArrayList;
import java.util.List;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "zone")
public class Zone {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "zone_id")
    private Integer zoneId;

    @NonNull
    @Column(name = "zone_capacity")
    private Integer zoneCapacity;

    @NonNull
    @Column(name = "zone_name")
    private String zoneName;
    
    /*
    @Column(name = "preRegistered_users")
    private List<User> preRegisteredUsers = new ArrayList<>();
    */

    @ManyToOne
    @JoinColumn(name = "events_id")
    private Event event;


    public Zone(Integer zoneCapacity, String zoneName, Event event){
        this.zoneCapacity = zoneCapacity;
        this.zoneName = zoneName;
        this.event = event;
    }
}
