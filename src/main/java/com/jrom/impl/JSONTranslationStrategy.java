package com.jrom.impl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.jrom.api.TranslationStrategy;
import com.jrom.api.exception.JROMTranslationException;
import com.jrom.impl.metadata.AnnotationMetadataExtractionStrategy;
import com.jrom.impl.metadata.ExternalMetadataTableEntry;
import com.jrom.impl.metadata.MetadataTableEntry;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Translation strategy based on JSON
 *
 * @author des
 */
public class JSONTranslationStrategy implements TranslationStrategy {
    public static final String NULL_EXTERNAL_OBJECT_ID = "null";
    private static final Logger LOG = LoggerFactory.getLogger(JSONTranslationStrategy.class);
    private static final Pattern STANDALONE_ATTRIBUTE = Pattern.compile("\"[\\S+&&[^\"]]+\":\\{\"jromExternalObjectId[\\S&&[^\\}]]+\\}{1}");
    private static final Gson BASE_GSON = new Gson();

    private final Gson gson;

    public JSONTranslationStrategy(Gson gson) {
        this.gson = gson;
    }

    public JSONTranslationStrategy() {
        this(new Gson());
    }

    @Override
    public void serialiseStandalone(ExternalMetadataTableEntry entry, Object externalObject, Pipeline currentPipeline)
            throws JROMTranslationException {
        ExternalMetadataTableEntry.GenericExternalMetadataTableEntry genericEntry = (ExternalMetadataTableEntry.GenericExternalMetadataTableEntry) entry;
        String objectId = null;
        try {
            objectId = String.valueOf(externalObject.getClass()
                    .getMethod(genericEntry.getIdRetrievalMethod())
                    .invoke(externalObject));
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            LOG.error("Exception during translation, ");
            throw new JROMTranslationException("Exception during translation", e);
        }
        Map<String, String> idWithValues = new HashMap<>();
        idWithValues.put(objectId, BASE_GSON.toJson(externalObject));
        currentPipeline.hmset(genericEntry.getNamespace(), idWithValues);
    }

    @Override
    public Object deserialiseStandaloneV2(ExternalEntry externalEntry, MetadataTableEntry entry, Jedis jedis) {
        String externalObjectId = externalEntry.getId();
        String fieldName = externalEntry.getFieldName();
        ExternalMetadataTableEntry.GenericExternalMetadataTableEntry externalFieldMetadata =
                (ExternalMetadataTableEntry.GenericExternalMetadataTableEntry) entry.getExternalEntries().get(fieldName);
        String externalObjectAsString = jedis.hget(externalFieldMetadata.getNamespace(), externalObjectId);
        return BASE_GSON.fromJson(externalObjectAsString, externalFieldMetadata.getClassType());
    }

    @Override
    public String serialise(Object object) {
        return gson.toJson(object);
    }

    @Override
    public <T> T deserialiseStandalone(String externalObjectAsString, Class<T> classType) {
        return BASE_GSON.fromJson(externalObjectAsString, classType);
    }

    @Override
    public <T> Optional<Pair<T, List<ExternalEntry>>> deserialise(String objectString, Class<T> classType) {
        if (StringUtils.isEmpty(objectString)) {
            return Optional.empty();
        }

        List<ExternalEntry> externalEntries = new ArrayList<>();
        Matcher standaloneMatches = STANDALONE_ATTRIBUTE.matcher(objectString);
        String classTypeAsString = classType.getName();
        while (standaloneMatches.find()) {
            String entry = standaloneMatches.group();
            JsonObject jsonObject = gson.fromJson("{" + entry + "}", JsonObject.class);
            LOG.info("Standalone entry [{}] for class [{}]", entry, classTypeAsString);
            String fieldId = jsonObject.entrySet().stream().map(Map.Entry::getKey).findFirst().orElse(null);
            String externalObjectId = jsonObject.getAsJsonObject(fieldId)
                                                .get(AnnotationMetadataExtractionStrategy.EXTERNAL_OBJECT_ID)
                                                .getAsString();
            // Handles null/unset objects
            if (!NULL_EXTERNAL_OBJECT_ID.equals(externalObjectId)) {
                externalEntries.add(new ExternalEntry(externalObjectId, fieldId));
            }
        }

        return Optional.of(new ImmutablePair<>(gson.fromJson(objectString, classType), externalEntries));
    }
}