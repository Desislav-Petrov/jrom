package com.jrom.testdomain.external;

import com.jrom.api.annotation.Id;
import com.jrom.api.annotation.RedisAware;
import com.jrom.api.annotation.Standalone;
import com.jrom.util.ExternalDomainClass;

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
    @Standalone(externalNamespace = "externalDomainClass", idMethodProvider = "getExternalTestVariable")
    private ExternalDomainClass externalDomainClassInstance;

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

    public ExternalDomainClass getExternalDomainClassInstance() {
        return externalDomainClassInstance;
    }

    public void setExternalDomainClassInstance(ExternalDomainClass externalDomainClassInstance) {
        this.externalDomainClassInstance = externalDomainClassInstance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SampleDomainClass that = (SampleDomainClass) o;

        return id.equals(that.id);

    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        return result;
    }
}
