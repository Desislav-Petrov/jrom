package com.jrom.testdomain.broken1;

import com.jrom.api.annotation.Id;
import com.jrom.api.annotation.RedisAware;

/**
 * Example domain class
 */
@RedisAware(namespace = "sampleclass")
public class MissingGetterSampleDomainClass {
    @Id
    private String id;

    private String someStringValue;
    private Integer someIntValue;
    private Double someDoubleValue;

    public MissingGetterSampleDomainClass() {
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSomeStringValue() {
        return someStringValue;
    }

    public void setSomeStringValue(String someStringValue) {
        this.someStringValue = someStringValue;
    }

    public Integer getSomeIntValue() {
        return someIntValue;
    }

    public void setSomeIntValue(Integer someIntValue) {
        this.someIntValue = someIntValue;
    }

    public Double getSomeDoubleValue() {
        return someDoubleValue;
    }

    public void setSomeDoubleValue(Double someDoubleValue) {
        this.someDoubleValue = someDoubleValue;
    }
}
