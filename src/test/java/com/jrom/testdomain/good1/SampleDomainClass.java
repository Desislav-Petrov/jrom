package com.jrom.testdomain.good1;

import com.jrom.api.annotation.Id;
import com.jrom.api.annotation.RedisAware;

/**
 * Example domain class
 */
@RedisAware(namespace = "sampleclass")
public class SampleDomainClass {
    @Id
    private String id;

    private String someStringValue;
    private Integer someIntValue;
    private Double someDoubleValue;

    public SampleDomainClass() {
    }

    public String getId() {
        return id;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SampleDomainClass that = (SampleDomainClass) o;

        if (!id.equals(that.id)) return false;
        if (!someStringValue.equals(that.someStringValue)) return false;
        if (!someIntValue.equals(that.someIntValue)) return false;
        return someDoubleValue.equals(that.someDoubleValue);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + someStringValue.hashCode();
        result = 31 * result + someIntValue.hashCode();
        result = 31 * result + someDoubleValue.hashCode();
        return result;
    }
}
