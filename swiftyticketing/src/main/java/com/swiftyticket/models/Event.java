package com.swiftyticket.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "events")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Slf4j
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "event_id")
    private Integer eventId;

    @Column(name = "event_name")
    @NonNull
    private String eventName;

    @Column(name = "artists")
    @NonNull
    private List<String> artists;

    @NonNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "event_dates")
    private List<Date> dates;

    @NonNull
    @Column(name = "venue")
    private String venue;

    @NonNull
    @Column(name = "venue_capacity")
    private Integer venueCapacity;

    @Column(name = "open")
    private boolean open4Registration;
    
    @Column(name = "user_count")
    private int user_count;

    @OneToMany(mappedBy = "event",
                fetch = FetchType.EAGER,
                cascade = CascadeType.ALL)
    private List<Zones> zoneList;

    @ManyToMany(fetch = FetchType.EAGER)
    @JsonIgnore
    @JoinTable(name = "event_users",
        joinColumns=
            @JoinColumn(name="event_id"),
        inverseJoinColumns=
            @JoinColumn(name="user_id"))
    @Column(name = "users_preRegistered")
    private List<User> preRegisteredUsers4Event;

    private Integer raffleRound = 0;

    @JsonIgnore
    public boolean getOpenStatus(){
        return this.open4Registration;
    }


        //currently eventController uses requestBody, which doesnt require this constructor. If you wish to change it in the future can use this.
        @JsonIgnore
    public Event(String eventName, List<String> artists, List<Date> dates, String venue, Integer venueCapacity){

        this.eventName = eventName;
        this.artists = artists;
        this.dates = dates;
        this.venue = venue;
        this.venueCapacity = venueCapacity;

        this.zoneList = new ArrayList<>();
        this.preRegisteredUsers4Event = new ArrayList<>();
        

        log.info("Event successfully created!");

        }

        @Override
        @JsonIgnore
        public boolean equals(Object other){
            if( !(other instanceof Event) ){
                return false;
            }
    
            return this.eventId == ((Event)other).eventId;
        }
}
