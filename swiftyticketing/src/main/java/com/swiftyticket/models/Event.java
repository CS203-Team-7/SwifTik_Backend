package com.swiftyticket.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "event_id")
    private Integer eventId;

    @Column(name = "event_name")
    @NonNull
    private String eventName;

    // still unsure how to properly record multi-valued attributes, code may break
    // make a artist class? with name, surname, genre, gender, age?
    @Column(name = "artists")
    @NonNull
    private List<String> artists;
    
    // still unsure how to properly record multi-valued attributes, code may break
    // have to use LocalDate.of() method, unsure of how to input the arguements for the method right now
    // how to incorporate time? cant just make a list of time because the a specific time should be linked to specific date
    @NonNull
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

    @OneToMany(mappedBy = "event",
                cascade = CascadeType.ALL)
    private List<Zones> zoneList;

    public Event(String eventName, List<String> artists, List<Date> dates, String venue, Integer venueCapacity){
        this.eventName = eventName;
        this.artists = artists;
        this.dates = dates;
        this.venue = venue;
        this.venueCapacity = venueCapacity;

        zoneList = new ArrayList<>();
        open4Registration = true;
    }






    @JsonIgnore
    public boolean getOpenStatus(){
        return this.open4Registration;
    }


    


    // custom constructor to account for use of LocalDate.of(yyyy, mm, dd) method
    // public Event(Integer eventId, @NonNull List<String> artists,
    //   @NonNull int year, @NonNull int month, @NonNull int date, @NonNull String venue,
    //   @NonNull Integer venueCapacity) {
    //     this.eventId = eventId;
    //     this.artists = artists;
    //     this.dates = new ArrayList<>();
    //     this.dates.add(LocalDate.of(year, month, date));
    //     this.venue = venue;
    //     this.venueCapacity = venueCapacity;
    // }

    
}
