package daoha.top.domain.agent.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Agent 运行入口配置。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiRunnerVO {

    private Long id;
    private Long agentId;
    private Long workflowId;
    @Builder.Default
    private List<String> pluginNameList = new ArrayList<>();
}
