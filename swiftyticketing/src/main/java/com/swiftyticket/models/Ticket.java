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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tickets")
public class Ticket {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ticket_id")
    private Integer ticketId;

    @Column(name = "zone_name")
    private String zonename;


    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "zonet_id", nullable = false)
    private Zones forZone;

    //private Integer zoneId;


    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usert_id", nullable = false)
    private User forUser;

    //private Integer userId;
    

    private String userEmail;

    @JsonIgnore
    public Ticket(Zones zone, User user){

        this.forZone = zone;
        this.forUser = user;
        //this.zoneId = zone.getZoneId();
        //this.userId = user.getUserId();


        
        this.zonename = zone.getZoneName();
        this.userEmail = user.getEmail();
    }

    @Override
    @JsonIgnore
    public boolean equals(Object other){
        if( !(other instanceof Ticket) ){
            return false;
        }

        return this.ticketId == ((Ticket)other).ticketId;
    }
    
}
