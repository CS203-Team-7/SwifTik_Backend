package com.swiftyticket.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tickets")
@Transactional
public class Ticket {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ticket_id")
    private Integer ticketId;

    @Column(name = "zone_name")
    private String zonename;

/*
    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "zonet_id", nullable = false)
    private Zones forZone;
*/
    private Integer userId;

/*
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usert_id", nullable = false)
    private User forUser;
*/
    private Integer zoneId;

    private String userEmail;

    @JsonIgnore
    public Ticket(Zones zone, User user){

        //this.forZone = zone;
        //this.forUser = user;
        this.zoneId = zone.getZoneId();
        this.userId = user.getUserId();


        
        this.zonename = zone.getZoneName();
        this.userEmail = user.getEmail();
    }
    
}
