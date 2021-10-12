package io.kgu.chatservice.socket.custom;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedCaseInsensitiveMap;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketExtension;

import java.io.Serializable;
import java.util.*;

@Data
@NoArgsConstructor
public class CustomWebSocketExtension implements Serializable {

    private String name;

    private Map<String, String> parameters;

    public CustomWebSocketExtension(String name) {
        this(name, null);
    }

    public CustomWebSocketExtension(String name, @Nullable Map<String, String> parameters) {
        Assert.hasLength(name, "Extension name must not be empty");
        this.name = name;
        if (!CollectionUtils.isEmpty(parameters)) {
            Map<String, String> map = new LinkedCaseInsensitiveMap<>(parameters.size(), Locale.ENGLISH);
            map.putAll(parameters);
            this.parameters = Collections.unmodifiableMap(map);
        }
        else {
            this.parameters = Collections.emptyMap();
        }
    }

    public static List<WebSocketExtension> parseExtensions(String extensions) {
        if (StringUtils.hasText(extensions)) {
            String[] tokens = StringUtils.tokenizeToStringArray(extensions, ",");
            List<WebSocketExtension> result = new ArrayList<>(tokens.length);
            for (String token : tokens) {
                result.add(parseExtension(token));
            }
            return result;
        }
        else {
            return Collections.emptyList();
        }
    }

    private static WebSocketExtension parseExtension(String extension) {
        if (extension.contains(",")) {
            throw new IllegalArgumentException("Expected single extension value: [" + extension + "]");
        }
        String[] parts = StringUtils.tokenizeToStringArray(extension, ";");
        String name = parts[0].trim();

        Map<String, String> parameters = null;
        if (parts.length > 1) {
            parameters = CollectionUtils.newLinkedHashMap(parts.length - 1);
            for (int i = 1; i < parts.length; i++) {
                String parameter = parts[i];
                int eqIndex = parameter.indexOf('=');
                if (eqIndex != -1) {
                    String attribute = parameter.substring(0, eqIndex);
                    String value = parameter.substring(eqIndex + 1);
                    parameters.put(attribute, value);
                }
            }
        }

        return new WebSocketExtension(name, parameters);
    }

}
