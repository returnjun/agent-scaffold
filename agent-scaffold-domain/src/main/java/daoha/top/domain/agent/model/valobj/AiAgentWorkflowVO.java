package daoha.top.domain.agent.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Agent 工作流配置。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiAgentWorkflowVO {

    private Long id;
    private String type;
    private String name;
    @Builder.Default
    private List<Long> subAgentIds = new ArrayList<>();
    private String description;
    @Builder.Default
    private Integer maxIterations = 3;
}
