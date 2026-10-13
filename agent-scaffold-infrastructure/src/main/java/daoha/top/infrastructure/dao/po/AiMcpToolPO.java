package daoha.top.infrastructure.dao.po;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * MCP/本地工具持久化对象，对应 ai_mcp_tool 表。
 */
@Data
public class AiMcpToolPO {

    private Long id;
    private Long appId;
    private String mcpCode;
    private String transportType;
    private String toolName;
    private String baseUri;
    private String endpointPath;
    private Integer requestTimeoutMs;
    private String commandText;
    private String argsJson;
    private String envJson;
    private String localBeanName;
    private String authType;
    private String authKey;
    private byte[] authValueCiphertext;
    /** 数据库查询时通过 AES_DECRYPT 生成的只读字段。 */
    private String authValue;
    private Integer status;
    private Integer sortNo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
