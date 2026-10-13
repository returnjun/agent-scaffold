package daoha.top.domain.agent.service.armory.node;

import daoha.top.domain.agent.model.valobj.AiChatModelVO;

import org.springframework.ai.openai.OpenAiChatOptions;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.Map;
import java.util.Set;

/** Applies database model_options in the shared armory instead of recreating models in business code. */
public final class ChatModelOptionsResolver {
    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final Set<String> KEYS = Set.of("temperature", "topP", "maxTokens", "maxCompletionTokens",
            "timeoutSeconds", "maxRetries", "extraBody", "outputSchema",
            "purpose", "modelType", "protocol");

    private ChatModelOptionsResolver() {
    }

    public static OpenAiChatOptions resolve(AiChatModelVO model, String baseUrl, String apiKey) {
        var builder = OpenAiChatOptions.builder().baseUrl(baseUrl).apiKey(apiKey).model(model.getModel());
        if (model.getModelOptions() == null || model.getModelOptions().isBlank()) {
            return builder.build();
        }
        var options = JSON.readTree(model.getModelOptions());
        if (!options.isObject()) {
            throw new IllegalArgumentException("model_options must be an object: " + model.getChatId());
        }
        options.propertyNames().forEach(key -> {
            if (!KEYS.contains(key)) {
                throw new IllegalArgumentException("Unsupported model option: " + key);
            }
        });
        if (options.has("temperature")) {
            builder.temperature(number(options.get("temperature"), 0, 2));
        }
        if (options.has("topP")) {
            builder.topP(number(options.get("topP"), 0, 1));
        }
        if (options.has("maxTokens")) {
            builder.maxTokens(integer(options.get("maxTokens"), 1, 1000000));
        }
        if (options.has("maxCompletionTokens")) {
            builder.maxCompletionTokens(integer(options.get("maxCompletionTokens"), 1, 1000000));
        }
        if (options.has("timeoutSeconds")) {
            builder.timeout(Duration.ofSeconds(integer(options.get("timeoutSeconds"), 1, 600)));
        }
        if (options.has("maxRetries")) {
            builder.maxRetries(integer(options.get("maxRetries"), 0, 5));
        }
        if (options.has("extraBody")) {
            if (!options.get("extraBody").isObject()) {
                throw new IllegalArgumentException("extraBody must be an object");
            }
            builder.extraBody(JSON.convertValue(options.get("extraBody"),
                    new TypeReference<Map<String, Object>>() { }));
        }
        if (options.has("outputSchema")) {
            var schema = options.get("outputSchema");
            builder.outputSchema(schema.isTextual() ? schema.asText() : schema.toString());
        }
        return builder.build();
    }

    private static double number(JsonNode value, double min, double max) {
        if (!value.isNumber() || !Double.isFinite(value.asDouble()) || value.asDouble() < min || value.asDouble() > max) {
            throw new IllegalArgumentException("Invalid numeric model option");
        }
        return value.asDouble();
    }

    private static int integer(JsonNode value, int min, int max) {
        if (!value.isIntegralNumber() || !value.canConvertToInt() || value.asInt() < min || value.asInt() > max) {
            throw new IllegalArgumentException("Invalid integer model option");
        }
        return value.asInt();
    }
}
