package com.jrom.testdomain.good1;

import com.jrom.api.annotation.RedisIgnore;

/**
 * Created by des on 12/10/16.
 */
public class SampleDomainClassWithIgnore extends SampleDomainClass {
    @RedisIgnore
    private String ignoredString;

    public String getIgnoredString() {
        return ignoredString;
    }

    public void setIgnoredString(String ignoredString) {
        this.ignoredString = ignoredString;
    }
}
