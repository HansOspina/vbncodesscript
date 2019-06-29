package com.example.demo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Variety {


    @JsonProperty("_KEY")
    private String key;

    @JsonProperty("variety_name")
    private String name;

    @JsonProperty("vbn_code")
    private String vbnCode;

    @JsonProperty("product_group")
    private String productGroupReference;

    private ProductGroup productGroup;

    @Override
    public String toString() {
        return (this.productGroup != null ? this.productGroup.getName() + " " : "") + name;
    }

    public Variety bindProductGroup(ProductGroup productGroup) {
        this.productGroup = productGroup;
        return this;
    }

}
