package com.jrom.testdomain.good1;

import com.jrom.api.annotation.Id;
import com.jrom.api.annotation.RedisAware;

/**
 * Created by des on 12/17/16.
 */
@RedisAware(namespace = "samplenamespace")
public class SampleDomainClassWithMethodId {
    private String id;

    @Id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SampleDomainClassWithMethodId that = (SampleDomainClassWithMethodId) o;

        return id.equals(that.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
