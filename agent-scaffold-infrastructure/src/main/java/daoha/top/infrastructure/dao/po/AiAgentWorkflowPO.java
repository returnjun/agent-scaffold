package daoha.top.infrastructure.dao.po;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Agent 工作流持久化对象，对应 ai_agent_workflow 表。
 */
@Data
public class AiAgentWorkflowPO {

    private Long id;
    private Long appId;
    private String workflowCode;
    private String workflowType;
    private String description;
    private String subAgentIds;
    private Integer maxIterations;
    private Integer status;
    private Integer sortNo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
