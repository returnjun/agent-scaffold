package daoha.top.infrastructure.dao.po;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * LLM Agent 持久化对象，对应 ai_agent 表。
 */
@Data
public class AiAgentPO {

    private Long id;
    private Long appId;
    private String agentCode;
    private Long modelId;
    private String description;
    private String instructionText;
    private String outputKey;
    private String mcpToolIds;
    private String skillIds;
    private String toolIds;
    private Integer status;
    private Integer sortNo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
