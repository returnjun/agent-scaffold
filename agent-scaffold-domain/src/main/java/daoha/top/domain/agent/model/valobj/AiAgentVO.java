package daoha.top.domain.agent.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 单个 LLM Agent 节点配置。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiAgentVO {

    private Long id;
    private String name;
    private Long chatModelId;
    @Builder.Default
    private List<Long> mcpToolIds = new ArrayList<>();
    @Builder.Default
    private List<Long> skillIds = new ArrayList<>();
    @Builder.Default
    private List<Long> toolIds = new ArrayList<>();
    private String instruction;
    private String description;
    private String outputKey;
}
