package com.example.demo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.HashMap;
import java.util.List;

@Data
public class Response<T> {
    private Boolean success;
    private String error;
    private String message;

    @JsonProperty("total_pages")
    Integer totalPages;


    List<T> results;

    @JsonProperty("fetched_results")
    private HashMap<String, HashMap<String, HashMap<String, Object>>> fetchModels;

    @Override
    public String toString() {
        return success+error+results.size();
    }

}
