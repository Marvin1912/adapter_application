package com.marvin.consul.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.marvin.consul.config.ConsulKeyValueValueDeserializer;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ConsulKeyValueDTO(String key, String value) {

    public ConsulKeyValueDTO(
            @JsonProperty("Key") String key,
            @JsonProperty("Value") @JsonDeserialize(using = ConsulKeyValueValueDeserializer.class) String value
    ) {
        this.key = key;
        this.value = value;
    }
}
