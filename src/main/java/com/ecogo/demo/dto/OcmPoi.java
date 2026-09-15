package com.ecogo.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record OcmPoi(
    @JsonProperty("ID") Integer id,
    @JsonProperty("AddressInfo") AddressInfo addressInfo,
    @JsonProperty("Connections") List<Connection> connections,
    @JsonProperty("StatusType") StatusType statusType
) {
    public record AddressInfo(
        @JsonProperty("Title") String title,
        @JsonProperty("AddressLine1") String addressLine1,
        @JsonProperty("Town") String town,
        @JsonProperty("StateOrProvince") String stateOrProvince,
        @JsonProperty("Latitude") Double latitude,
        @JsonProperty("Longitude") Double longitude
    ) {}

    public record Connection(
        @JsonProperty("ConnectionTypeID") Integer connectionTypeId,
        @JsonProperty("PowerKW") Double powerKw
    ) {}

    public record StatusType(
        @JsonProperty("IsOperational") Boolean isOperational
    ) {}
}