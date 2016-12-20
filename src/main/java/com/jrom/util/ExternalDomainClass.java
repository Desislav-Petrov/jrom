package com.jrom.util;

/**
 * Created by des on 12/10/16.
 */
public class ExternalDomainClass {
    private String externalTestVariable;

    public ExternalDomainClass() {
    }

    public String getExternalTestVariable() {
        return externalTestVariable;
    }

    public void setExternalTestVariable(String externalTestVariable) {
        this.externalTestVariable = externalTestVariable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExternalDomainClass that = (ExternalDomainClass) o;

        return externalTestVariable != null ? externalTestVariable.equals(that.externalTestVariable) : that.externalTestVariable == null;
    }

    @Override
    public int hashCode() {
        return externalTestVariable != null ? externalTestVariable.hashCode() : 0;
    }
}
