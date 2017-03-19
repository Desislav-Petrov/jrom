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
import java.util.stream.Collectors;

import static com.jrom.impl.metadata.ExternalMetadataTableEntry.SingleExternalMetadataTableEntry;

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
        switch (entry.getExternalEntryType()) {
            case SINGLE:
                SingleExternalMetadataTableEntry singleEntry = (SingleExternalMetadataTableEntry) entry;
                String objectId = null;
                objectId = getObjectId(externalObject, singleEntry);
                Map<String, String> idWithValues = new HashMap<>();
                idWithValues.put(objectId, BASE_GSON.toJson(externalObject));
                currentPipeline.hmset(singleEntry.getNamespace(), idWithValues);
                break;

            case LIST:
                List<?> externalList = (List<?>) externalObject;
                if (externalList != null && !externalList.isEmpty()) {
                    currentPipeline.del(entry.getNamespace());
                    externalList.forEach(e -> currentPipeline.lpush(entry.getNamespace(), BASE_GSON.toJson(e)));
                }
                break;

            case MAP:
                Map<?, ?> externalMap = (Map<?, ?>) externalObject;
                if (externalMap != null && !externalMap.isEmpty()) {
                    currentPipeline.del(entry.getNamespace());
                    Map<String, String> serialisedMap = externalMap.entrySet().stream()
                            .collect(Collectors.toMap(BASE_GSON::toJson, BASE_GSON::toJson));
                    currentPipeline.hmset(entry.getNamespace(), serialisedMap);
                }
                break;

            case SET:
                Set<?> externalSet = (Set<?>) externalObject;
                if (externalSet != null && !externalSet.isEmpty()) {
                    currentPipeline.del(entry.getNamespace());
                    externalSet.forEach(e -> currentPipeline.sadd(entry.getNamespace(), BASE_GSON.toJson(e)));
                }
                break;

            default:
                throw new IllegalArgumentException("Unknown external type: " + entry.getExternalEntryType());
        }
    }

    private String getObjectId(Object externalObject, SingleExternalMetadataTableEntry genericEntry) {
        try {
            return String.valueOf(externalObject.getClass()
                             .getMethod(genericEntry.getIdRetrievalMethod())
                             .invoke(externalObject));
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            LOG.error("Couldn't retrieve id during translation for external object [{}]", externalObject);
            throw new JROMTranslationException(e);
        }
    }

    @Override
    public Object deserialiseStandalone(ExternalEntry externalEntry, MetadataTableEntry entry, Jedis jedis) {
        String fieldName = externalEntry.getFieldName();
        ExternalMetadataTableEntry externalFieldMetadata = entry.getExternalEntries().get(fieldName);

        switch (externalFieldMetadata.getExternalEntryType()) {
            case SINGLE:
                SingleExternalMetadataTableEntry singleEntry = (SingleExternalMetadataTableEntry) externalFieldMetadata;
                String externalObjectId = externalEntry.getId();
                String externalObjectAsString = jedis.hget(externalFieldMetadata.getNamespace(), externalObjectId);
                return BASE_GSON.fromJson(externalObjectAsString, singleEntry.getClassType());

            case LIST:
                ExternalMetadataTableEntry.SetOrListExternalMetadataTableEntry setOrListEntry =
                        (ExternalMetadataTableEntry.SetOrListExternalMetadataTableEntry) externalFieldMetadata;
                List<String> listEntriesFromRedis = jedis.lrange(entry.getClassNamespace(), 0, -1);
                List<Object> objectList = new ArrayList<>();
                listEntriesFromRedis.forEach(e -> objectList.add(BASE_GSON.fromJson(e, setOrListEntry.getClassType())));
                return objectList;

            case MAP:
                ExternalMetadataTableEntry.MapExternalMetadataTableEntry mapEntry =
                        (ExternalMetadataTableEntry.MapExternalMetadataTableEntry) externalFieldMetadata;
                Map<String, String> mapFromRedis = jedis.hgetAll(mapEntry.getNamespace());
                return mapFromRedis.entrySet()
                        .stream()
                        .collect(Collectors
                        .toMap(k -> BASE_GSON.toJson(k, mapEntry.getKeyType()), v -> BASE_GSON.toJson(v, mapEntry.getValueType())));

            case SET:
                setOrListEntry = (ExternalMetadataTableEntry.SetOrListExternalMetadataTableEntry) externalFieldMetadata;
                Set<String> setEntriesFromRedis = jedis.smembers(entry.getClassNamespace());
                Set<Object> objectSet = new HashSet<>();
                setEntriesFromRedis.forEach(e -> objectSet.add(BASE_GSON.fromJson(e, setOrListEntry.getClassType())));
                return objectSet;

            default:
                throw new IllegalArgumentException("Unknown external type: " + externalFieldMetadata.getExternalEntryType());
        }
    }

    @Override
    public String serialise(Object object) {
        return gson.toJson(object);
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