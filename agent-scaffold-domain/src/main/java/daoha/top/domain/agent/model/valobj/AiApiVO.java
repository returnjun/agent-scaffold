package daoha.top.domain.agent.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 模型服务 API 配置。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiApiVO {

    private Long id;
    private String apiId;
    private String baseUrl;
    private String apiKey;
    @Builder.Default
    private String completionsPath = "/v1/chat/completions";
    @Builder.Default
    private String embeddingsPath = "/v1/embeddings";
}
