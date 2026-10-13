package daoha.top.infrastructure.dao.po;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 模型持久化对象，对应 ai_model 表。
 */
@Data
public class AiModelPO {

    private Long id;
    private Long appId;
    private String modelCode;
    private Long apiId;
    private String modelName;
    private String modelOptions;
    private Integer status;
    private Integer sortNo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
