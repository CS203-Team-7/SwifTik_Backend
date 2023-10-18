package com.swiftyticket.dto.zone;

import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class ZoneRequest {
    //to pass the information of a zone without its event in order to make the zone
    Integer zoneCapacity;
    String zoneName;
    Date zoneDate;
}
