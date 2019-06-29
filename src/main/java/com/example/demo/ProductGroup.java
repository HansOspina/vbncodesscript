package com.example.demo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductGroup {


    @JsonProperty("_KEY")
    private String key;

    @JsonProperty("common_name")
    private String name;

}
