package daoha.top.domain.agent.service.armory.matter.tool;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.adk.tools.BaseTool;
import com.google.adk.tools.ToolContext;
import com.google.genai.types.FunctionDeclaration;
import io.reactivex.rxjava3.core.Single;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Exposes a Spring AI {@link ToolCallback} as a Google ADK {@link BaseTool}.
 *
 * <p>The ADK agent remains responsible for executing function calls, while
 * the existing Spring AI MCP/local/skills integrations remain reusable.</p>
 */
public final class SpringAiToolCallbackAdapter extends BaseTool {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final ToolCallback delegate;
    private final FunctionDeclaration functionDeclaration;

    public SpringAiToolCallbackAdapter(ToolCallback delegate) {
        super(toolDefinition(delegate).name(), description(toolDefinition(delegate)));
        this.delegate = delegate;
        this.functionDeclaration = buildFunctionDeclaration(delegate.getToolDefinition());
    }

    @Override
    public Optional<FunctionDeclaration> declaration() {
        return Optional.of(functionDeclaration);
    }

    @Override
    public Single<Map<String, Object>> runAsync(Map<String, Object> arguments, ToolContext toolContext) {
        return Single.fromCallable(() -> {
            String requestJson = OBJECT_MAPPER.writeValueAsString(arguments);
            String response = delegate.call(requestJson);
            return toResultMap(response);
        });
    }

    private static ToolDefinition toolDefinition(ToolCallback delegate) {
        if (delegate == null || delegate.getToolDefinition() == null) {
            throw new IllegalArgumentException("ToolCallback and its ToolDefinition must not be null");
        }
        return delegate.getToolDefinition();
    }

    private static String description(ToolDefinition definition) {
        return definition.description() == null ? "" : definition.description();
    }

    private static FunctionDeclaration buildFunctionDeclaration(ToolDefinition definition) {
        try {
            Object inputSchema = OBJECT_MAPPER.readValue(definition.inputSchema(), Object.class);
            return FunctionDeclaration.builder()
                    .name(definition.name())
                    .description(description(definition))
                    .parametersJsonSchema(inputSchema)
                    .build();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid input schema for tool: " + definition.name(), e);
        }
    }

    private static Map<String, Object> toResultMap(String response) {
        if (response == null || response.isBlank()) {
            return Map.of("result", "");
        }

        try {
            Object parsed = OBJECT_MAPPER.readValue(response, Object.class);
            if (parsed instanceof Map<?, ?>) {
                return OBJECT_MAPPER.convertValue(parsed, MAP_TYPE);
            }
            return Map.of("result", parsed == null ? "" : parsed);
        } catch (Exception ignored) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("result", response);
            return result;
        }
    }
}
