package com.jrom.impl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.jrom.api.TranslationStrategy;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    private static final Pattern STANDALONE_ATTRIBUTE = Pattern.compile("\\{\"fieldName[\\S&&[^\\}]]+\\}{1}");
    private final Gson gson;

    public JSONTranslationStrategy(Gson gson) {
        this.gson = gson;
    }

    public JSONTranslationStrategy() {
        this(new Gson());
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
            JsonObject jsonObject = gson.fromJson(entry, JsonObject.class);
            LOG.info("Standalone entry [{}] for class [{}]", entry, classTypeAsString);
            // Handles null/unset objects
            if (!NULL_EXTERNAL_OBJECT_ID.equals(jsonObject.get("id").getAsString())) {
                externalEntries.add(new ExternalEntry(
                        jsonObject.get("id").getAsString(),
                        jsonObject.get("fieldName").getAsString()));
            }
        }

        return Optional.of(new ImmutablePair<>(gson.fromJson(objectString, classType), externalEntries));
    }
}