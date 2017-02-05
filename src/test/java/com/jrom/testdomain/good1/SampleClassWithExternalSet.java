package com.jrom.testdomain.good1;

import com.jrom.api.annotation.Id;
import com.jrom.api.annotation.RedisAware;
import com.jrom.api.annotation.Standalone;

import java.util.*;

/**
 * Created by des on 2/5/17.
 */
@RedisAware(namespace = "sampleclasstest")
public class SampleClassWithExternalSet {
    @Id
    private int id;
    @Standalone(externalNamespace = "testSet", idMethodProvider = "toString")
    private Set<String> setValues = new HashSet<>();
    @Standalone(externalNamespace = "testList", idMethodProvider = "toString")
    private List<String> listValues = new ArrayList<>();
    @Standalone(externalNamespace = "testMap", idMethodProvider = "toString")
    private Map<String, String> mapValues = new HashMap<>();

    public List<String> getListValues() {
        return listValues;
    }

    public void setListValues(List<String> listValues) {
        this.listValues = listValues;
    }

    public Map<String, String> getMapValues() {
        return mapValues;
    }

    public void setMapValues(Map<String, String> mapValues) {
        this.mapValues = mapValues;
    }

    public Set<String> getSetValues() {
        return setValues;
    }

    public void setSetValues(Set<String> setValues) {
        this.setValues = setValues;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
