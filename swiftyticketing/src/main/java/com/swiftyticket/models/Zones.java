package com.swiftyticket.models;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
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
@Table(name = "zones")
public class Zones {
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "events_id", nullable = false)
    @JsonIgnore
    private Event event;

    @ManyToMany
    @JsonIgnore
    @JoinTable(name = "zones_users",
        joinColumns=
            @JoinColumn(name="zones_id"),
        inverseJoinColumns=
            @JoinColumn(name="user_id"))
    @Column(name = "preRegistered_users")
    private List<User> preRegisteredUsers4Zone = new ArrayList<>();


    public Zones(Integer zoneCapacity, String zoneName, Event event){
        this.zoneCapacity = zoneCapacity;
        this.zoneName = zoneName;
        this.event = event;
    }
}
