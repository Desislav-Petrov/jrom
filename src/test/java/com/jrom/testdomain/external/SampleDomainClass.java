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
    @Standalone(externalNamespace = "externalDomainClassSecond", idMethodProvider = "getExternalTestVariable")
    private ExternalDomainClass externalDomainClassInstanceSecond;

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

    public ExternalDomainClass getExternalDomainClassInstanceSecond() {
        return externalDomainClassInstanceSecond;
    }

    public void setExternalDomainClassInstanceSecond(ExternalDomainClass externalDomainClassInstanceSecond) {
        this.externalDomainClassInstanceSecond = externalDomainClassInstanceSecond;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SampleDomainClass that = (SampleDomainClass) o;

        if (!id.equals(that.id)) return false;
        if (someStringValue != null ? !someStringValue.equals(that.someStringValue) : that.someStringValue != null)
            return false;
        if (someIntValue != null ? !someIntValue.equals(that.someIntValue) : that.someIntValue != null) return false;
        if (someDoubleValue != null ? !someDoubleValue.equals(that.someDoubleValue) : that.someDoubleValue != null)
            return false;
        if (externalDomainClassInstance != null ? !externalDomainClassInstance.equals(that.externalDomainClassInstance) : that.externalDomainClassInstance != null)
            return false;
        return externalDomainClassInstanceSecond != null ? externalDomainClassInstanceSecond.equals(that.externalDomainClassInstanceSecond) : that.externalDomainClassInstanceSecond == null;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + (someStringValue != null ? someStringValue.hashCode() : 0);
        result = 31 * result + (someIntValue != null ? someIntValue.hashCode() : 0);
        result = 31 * result + (someDoubleValue != null ? someDoubleValue.hashCode() : 0);
        result = 31 * result + (externalDomainClassInstance != null ? externalDomainClassInstance.hashCode() : 0);
        result = 31 * result + (externalDomainClassInstanceSecond != null ? externalDomainClassInstanceSecond.hashCode() : 0);
        return result;
    }
}
