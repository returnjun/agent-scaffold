package daoha.top.infrastructure.dao.po;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Agent 运行入口持久化对象，对应 ai_runner 表。
 */
@Data
public class AiRunnerPO {

    private Long id;
    private Long appId;
    private Long agentId;
    private Long workflowId;
    private String pluginNames;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
