package com.example.demo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Data
@NoArgsConstructor
public class Query {
    private Integer domain;
    private Integer page = 1;
    private Integer size = 100;

    @JsonProperty("row_model")
    private String rowModel;


    @JsonProperty("fetch")
    private String[] fetch;


    private Query(Integer domain, Integer page, Integer size, String rowModel, List<String> fetch) {
        this.domain = domain;
        this.page = page;
        this.size = size;
        this.rowModel = rowModel;
        this.fetch = fetch.toArray(String[]::new);
    }



    static public class Builder {
        private Integer domain;
        private Integer page;
        private Integer size;
        private String rowModel;
        private List<String> fetchModels = new ArrayList<>();


        public Builder setDomain(Integer domain) {
            this.domain = domain;
            return this;
        }

        public Builder setPage(Integer page) {
            this.page = page;
            return this;
        }

        public Builder setSize(Integer size) {
            this.size = size;
            return this;
        }

        public Builder setRowModel(String rowModel) {
            this.rowModel = rowModel;
            return this;
        }

        public Builder addFetch(String rowModel) {
            this.fetchModels.add(rowModel);
            return this;
        }

        public static Builder build() {
            return new Builder();
        }

        public Query createQuery() {
            return new Query(domain, page, size, rowModel, fetchModels);
        }
    }
}
