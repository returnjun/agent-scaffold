package daoha.top.infrastructure.dao.po;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Skill 持久化对象，对应 ai_skill 表。
 */
@Data
public class AiSkillPO {

    private Long id;
    private Long appId;
    private String skillCode;
    private String resourceType;
    private String resourcePath;
    private Integer status;
    private Integer sortNo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
