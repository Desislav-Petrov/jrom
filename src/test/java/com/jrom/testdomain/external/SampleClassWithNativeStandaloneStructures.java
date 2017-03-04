package com.jrom.testdomain.external;

import com.jrom.api.annotation.Id;
import com.jrom.api.annotation.RedisAware;
import com.jrom.api.annotation.Standalone;

import java.util.*;

/**
 * Created by des on 2/5/17.
 */
@RedisAware(namespace = "sampleclasstest")
public class SampleClassWithNativeStandaloneStructures {
    @Id
    private String id;
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

        SampleClassWithNativeStandaloneStructures that = (SampleClassWithNativeStandaloneStructures) o;

        if (!id.equals(that.id)) return false;
        if (setValues != null ? !setValues.equals(that.setValues) : that.setValues != null) return false;
        if (listValues != null ? !listValues.equals(that.listValues) : that.listValues != null) return false;
        return mapValues != null ? mapValues.equals(that.mapValues) : that.mapValues == null;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + (setValues != null ? setValues.hashCode() : 0);
        result = 31 * result + (listValues != null ? listValues.hashCode() : 0);
        result = 31 * result + (mapValues != null ? mapValues.hashCode() : 0);
        return result;
    }
}
