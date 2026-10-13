package daoha.top.domain.agent.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * MCP 或本地 ToolCallbackProvider 配置。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiMcpToolVO {

    private Long id;
    private String mcpToolId;
    private SSEServerParameters sse;
    private StdioServerParameters stdio;
    private LocalParameters local;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SSEServerParameters {
        private String name;
        private String baseUri;
        private String sseEndpoint;
        @Builder.Default
        private Integer requestTimeout = 3000;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StdioServerParameters {
        private String name;
        @Builder.Default
        private Integer requestTimeout = 3000;
        private ServerParameters serverParameters;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ServerParameters {
            private String command;
            private List<String> args;
            private Map<String, String> env;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocalParameters {
        private String name;
        private String toolName;
        private String baseUri;
        private String endpointPath;
        @Builder.Default
        private Integer requestTimeout = 3000;
        @Builder.Default
        private Map<String, Object> options = Map.of();
        @Builder.Default
        private Map<String, String> environment = Map.of();
    }
}
