package daoha.top.infrastructure.dao.po;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 智能体应用持久化对象，对应 ai_app 表。
 */
@Data
public class AiAppPO {

    private Long id;
    private String appCode;
    private String appName;
    private String agentCode;
    private String agentName;
    private String agentDescription;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
