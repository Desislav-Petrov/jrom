package com.jrom.impl.metadata;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.jrom.api.annotation.Id;
import com.jrom.api.annotation.RedisAware;
import com.jrom.api.annotation.RedisIgnore;
import com.jrom.api.annotation.Standalone;
import com.jrom.api.exception.JROMCRUDException;
import com.jrom.api.exception.JROMException;
import com.jrom.api.exception.JROMMetadataException;
import com.jrom.api.metadata.MetadataExtractionStrategy;
import com.jrom.impl.JSONTranslationStrategy;
import net.sf.corn.cps.CPScanner;
import net.sf.corn.cps.ClassFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Metadata extraction from annotations
 *
 * @author des
 */
public class AnnotationMetadataExtractionStrategy implements MetadataExtractionStrategy {
    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotationMetadataExtractionStrategy.class);

    private static final String EXTERNAL_OBJECT_FIELD_NAME = "fieldName";
    private static final String EXTERNAL_OBJECT_ID = "id";

    public AnnotationMetadataExtractionStrategy() {
        //empty
    }

    @Override
    public Map<Class<?>, MetadataTableEntry> extract(List<String> packagesToScan) {
        Map<Class<?>, MetadataTableEntry> entries = new ConcurrentHashMap<>();

        for (String packageToScan : packagesToScan) {
            List<Class<?>> classes = CPScanner.scanClasses(new ClassFilter().packageName(packageToScan)
                    .annotation(RedisAware.class));
            LOGGER.info("[{}] RedisAware classes found in package [{}]", classes.size(), packageToScan);
            for (Class<?> classEntry : classes) {
                GsonBuilder builder = new GsonBuilder();

                RedisAware redisAware = classEntry.getAnnotation(RedisAware.class);
                String classEntryName = classEntry.getName();
                String namespace = redisAware.namespace();

                // Handle id extraction either from field or for method
                Optional<Function<Object, String>> idExtractor = getIdProviderFunction(classEntry);
                if (!idExtractor.isPresent()) {
                    throw new JROMMetadataException("ID strategy set to field but no field annotated with @Id");
                }
                // Handle fields with RedisIgnore
                addExcludedFields(builder);

                // Handle external fields metadata
                Map<String, MetadataTableEntry.ExternalMetadataTableEntry> externalEntries = addExternalTypeAdapters(classEntry);
                addExternalFieldAdaptors(externalEntries, builder);

                MetadataTableEntry newEntry = MetadataTableEntry.of(
                        namespace, idExtractor.get(), new JSONTranslationStrategy(builder.create()), externalEntries);
                entries.put(classEntry, newEntry);

                LOGGER.info("Added entry for class [{}]: [{}]", classEntryName, newEntry);
            }
        }
        return entries;
    }

    private Optional<Function<Object, String>> getIdProviderFunction(Class<?> classEntry) {
        Function<Object, String> idProvider = idProviderFromField(classEntry);
        if (idProvider == null) {
            idProvider = idProviderFromMethod(classEntry);
        }
        return Optional.ofNullable(idProvider);
    }

    private Map<String, MetadataTableEntry.ExternalMetadataTableEntry> addExternalTypeAdapters(Class<?> classEntry) {
        Stream<Field> allFields = Stream.of(classEntry.getDeclaredFields());

        //get all fields from all superclasses
        Class<?> superClass = classEntry.getSuperclass();
        while (superClass != Object.class) {
            allFields = Stream.concat(allFields, Stream.of(superClass.getDeclaredFields()));
            superClass = superClass.getSuperclass();
        }

        Map<String, MetadataTableEntry.ExternalMetadataTableEntry> externalEntries = allFields
                .filter(e -> e.isAnnotationPresent(Standalone.class))
                .collect(Collectors.toMap(Field::getName, e -> {
                            Standalone standaloneAnnotation = e.getAnnotation(Standalone.class);
                            String externalNamespace = standaloneAnnotation.externalNamespace();
                            String idMethodName = standaloneAnnotation.idMethodProvider();
                            return new MetadataTableEntry.ExternalMetadataTableEntry(e.getType(), externalNamespace, idMethodName);
                        }));

        return externalEntries;
    }

    private Function<Object, String> idProviderFromMethod(Class<?> classEntry) {
        Function<Object, String> idExtractor = null;

        Method idGetterMethod = Arrays.stream(classEntry.getDeclaredMethods())
                .filter( e -> e.isAnnotationPresent(Id.class))
                .findFirst()
                .orElseGet(() -> {
                    // Check each superclass for the annotation as these aren't inherited on instance vars
                    Class<?> superClass = classEntry.getSuperclass();
                    while (superClass != Object.class) {
                        Method idGetterFromSuperclass = Arrays.stream(superClass.getDeclaredMethods())
                                .filter( e -> e.isAnnotationPresent(Id.class))
                                .findFirst()
                                .orElse(null);
                        if (idGetterFromSuperclass != null) {
                            return idGetterFromSuperclass;
                        }
                        superClass = superClass.getSuperclass();
                    }

                    return null;
                });

        if (idGetterMethod != null) {
            idExtractor = e -> {
                try {
                    return idGetterMethod.invoke(e).toString();
                } catch (IllegalAccessException | InvocationTargetException ex) {
                    throw new JROMException("Exception while accessing getter for: " + idGetterMethod.getName(), ex);
                }
            };
        }

        return idExtractor;
    }

    private Function<Object, String> idProviderFromField(Class<?> classEntry) {
        Function<Object, String> idExtractor = null;

        Field idField = Arrays.stream(classEntry.getDeclaredFields())
                .filter(e -> e.isAnnotationPresent(Id.class))
                .findFirst()
                .orElseGet( () -> {
                    // Check each superclass for the annotation as these aren't inherited on instance vars
                    Class<?> superClass =classEntry.getSuperclass();
                    while (superClass != Object.class) {
                        Field idFieldFromSuperclass = Arrays.stream(superClass.getDeclaredFields())
                                .filter( e -> e.isAnnotationPresent(Id.class))
                                .findFirst()
                                .orElse(null);
                        if (idFieldFromSuperclass != null) {
                            return idFieldFromSuperclass;
                        }
                        superClass = superClass.getSuperclass();
                    }

                    return null;
                });

        if (idField != null) {
            idExtractor = e -> {
                try {
                    Method descriptor = new PropertyDescriptor(idField.getName(), classEntry).getReadMethod();
                    return descriptor.invoke(e).toString();
                } catch (Exception ex) {
                    throw new JROMException("Exception while accessing getter for: " + idField.getName(), ex);
                }
            };
        }
        return idExtractor;
    }

    private void addExcludedFields(GsonBuilder builder) {
        builder.setExclusionStrategies(new ExclusionStrategy() {
            @Override
            public boolean shouldSkipField(FieldAttributes f) {
                return f.getAnnotation(RedisIgnore.class) != null;
            }

            @Override
            public boolean shouldSkipClass(Class<?> clazz) {
                return false;
            }
        });
    }

    private void addExternalFieldAdaptors(Map<String, MetadataTableEntry.ExternalMetadataTableEntry> externalEntries, GsonBuilder builder) {
        externalEntries.forEach((k,v) -> {
            builder.registerTypeAdapter(v.getClassType(), new TypeAdapter<Object>() {

                @Override
                public void write(JsonWriter out, Object value) throws IOException {
                    out.beginObject();
                    out.name(EXTERNAL_OBJECT_FIELD_NAME).value(k);
                    try {
                        String id = String.valueOf(value.getClass().getMethod(v.idRetrievalMethod).invoke(value));
                        out.name(EXTERNAL_OBJECT_ID).value(id);
                    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                        throw new JROMCRUDException("Unable to serialise/deserialise as no method: " + v.idRetrievalMethod
                                + " on object of type: " + v.getClass(), e);
                    }
                    out.endObject();
                }

                @Override
                public Object read(JsonReader in) throws IOException {
                    //always returns null, setting externals will take place after main object si retrieved
                    in.beginObject();
                    while (in.peek() == JsonToken.NAME){
                        in.nextName();
                        in.nextString();
                    }
                    in.endObject();
                    return null;
                }
            });
        });
    }
}
