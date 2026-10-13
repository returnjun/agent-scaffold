package daoha.top.infrastructure.dao.po;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 模型 API 持久化对象，对应 ai_api 表。
 */
@Data
public class AiApiPO {

    private Long id;
    private Long appId;
    private String apiCode;
    private String baseUrl;
    private byte[] apiKeyCiphertext;
    /** 数据库查询时通过 AES_DECRYPT 生成的只读字段。 */
    private String apiKey;
    private String completionsPath;
    private String embeddingsPath;
    private Integer status;
    private Integer sortNo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
