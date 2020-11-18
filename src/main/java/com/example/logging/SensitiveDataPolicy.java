package com.example.logging;

import software.amazon.awssdk.services.comprehend.ComprehendClient;
import software.amazon.awssdk.services.comprehend.model.DetectPiiEntitiesRequest;
import software.amazon.awssdk.services.comprehend.model.DetectPiiEntitiesResponse;
import software.amazon.awssdk.services.comprehend.model.PiiEntity;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.rewrite.RewritePolicy;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.SimpleMessage;

@Plugin(name = "SensitiveDataPolicy", category = Core.CATEGORY_NAME, elementType = "rewritePolicy", printObject = true)
public final class SensitiveDataPolicy implements RewritePolicy {

    private final String maskMode;
    private final String mask;
    private final float minScore;
    private final List<String> entityTypes = new ArrayList<>();
    private final ComprehendClient comprehendClient;

    public SensitiveDataPolicy(String minScoreString, String maskMode, String mask, String entitiesToReplace) {
        this.minScore = Float.parseFloat(minScoreString);
        this.maskMode = maskMode;
        this.mask = mask;

        String[] entityAsArray = entitiesToReplace.split(",");
        for (String entityType: entityAsArray) {
            entityTypes.add(entityType.trim());
        }

        comprehendClient = ComprehendClient.builder()
                .build();
    }

    @Override
    public LogEvent rewrite(final LogEvent event) {
        final Message msg = event.getMessage();
        Log4jLogEvent.Builder builder = new Log4jLogEvent.Builder();

        DetectPiiEntitiesRequest piiEntitiesRequest =
                DetectPiiEntitiesRequest.builder()
                        .languageCode("en")
                        .text(msg.getFormattedMessage())
                        .build();

        boolean changed = false;
        DetectPiiEntitiesResponse piiEntitiesResponse = comprehendClient.detectPiiEntities(piiEntitiesRequest);

        if (piiEntitiesResponse.hasEntities()) {
            final List<PiiEntity> entities = piiEntitiesResponse.entities();
            String formattedMessage = msg.getFormattedMessage();
            StringBuilder newString = new StringBuilder(formattedMessage);

            for (PiiEntity piiEntity: entities) {
                if (!entityTypes.contains(piiEntity.typeAsString())) {
                    continue;
                }

                if (piiEntity.score() > minScore) {
                    String sub = formattedMessage.substring(piiEntity.beginOffset(), piiEntity.endOffset());
                    int index = newString.indexOf(sub);
                    int subLength = sub.length();
                    String stringToReplace = "";
                    if (maskMode.equals("MASK")) {
                        stringToReplace = new String(new char[subLength]).replace("\0", mask);
                    } else if (maskMode.equals("REPLACE")){
                        stringToReplace = "[" + piiEntity.typeAsString() + "]";
                    }
                    newString.replace(index, index + subLength, stringToReplace);
                    changed = true;
                }
            }
            if (changed)  {
                builder.setMessage(new SimpleMessage(newString.toString()));
            } else {
                builder.setMessage(msg);
            }
        }

        builder.setLoggerName(event.getLoggerName());
        builder.setMarker(event.getMarker());
        builder.setLoggerFqcn(event.getLoggerFqcn());
        builder.setLevel(event.getLevel());
        builder.setThrown(event.getThrown());
        builder.setContextStack(event.getContextStack());
        builder.setThreadName(event.getThreadName());
        builder.setSource(event.getSource());
        builder.setTimeMillis(event.getTimeMillis());
        return builder.build();
    }

    @PluginFactory
    public static SensitiveDataPolicy createPolicy(
            @PluginAttribute("minScore") String minScoreString,
            @PluginAttribute("maskMode") String maskMode,
            @PluginAttribute("mask") String mask,
            @PluginAttribute("entitiesToReplace") String entitiesToReplace
            )
    {
        // setting some defaults
        if (maskMode == null || maskMode.length() == 0) {
            maskMode = "REPLACE";
        }
        if (!maskMode.equals("MASK") && !maskMode.equals("REPLACE")) {
            throw new RuntimeException("Unidentified maskMode");
        }
        if (entitiesToReplace == null || entitiesToReplace.length() == 0) {
            entitiesToReplace = "SSN";
        }
        if (minScoreString == null || minScoreString.length() == 0) {
            minScoreString = "0.9";
        }

        return new SensitiveDataPolicy(minScoreString, maskMode, mask, entitiesToReplace);
    }
}