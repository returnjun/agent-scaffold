package daoha.top.infrastructure.adapter.repository;

import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import daoha.top.domain.agent.adapter.repository.IAgentRepository;
import daoha.top.domain.agent.model.valobj.AiAgentVO;
import daoha.top.domain.agent.model.valobj.AiAgentWorkflowVO;
import daoha.top.domain.agent.model.valobj.AiApiVO;
import daoha.top.domain.agent.model.valobj.AiAppVO;
import daoha.top.domain.agent.model.valobj.AiChatModelVO;
import daoha.top.domain.agent.model.valobj.AiMcpToolVO;
import daoha.top.domain.agent.model.valobj.AiRunnerVO;
import daoha.top.domain.agent.model.valobj.AiSkillVO;
import daoha.top.infrastructure.config.AgentDatabaseProperties;
import daoha.top.infrastructure.dao.IAiAgentDao;
import daoha.top.infrastructure.dao.IAiAgentWorkflowDao;
import daoha.top.infrastructure.dao.IAiApiDao;
import daoha.top.infrastructure.dao.IAiAppDao;
import daoha.top.infrastructure.dao.IAiMcpToolDao;
import daoha.top.infrastructure.dao.IAiModelDao;
import daoha.top.infrastructure.dao.IAiRunnerDao;
import daoha.top.infrastructure.dao.IAiSkillDao;
import daoha.top.infrastructure.dao.po.AiAgentPO;
import daoha.top.infrastructure.dao.po.AiAgentWorkflowPO;
import daoha.top.infrastructure.dao.po.AiApiPO;
import daoha.top.infrastructure.dao.po.AiAppPO;
import daoha.top.infrastructure.dao.po.AiMcpToolPO;
import daoha.top.infrastructure.dao.po.AiModelPO;
import daoha.top.infrastructure.dao.po.AiRunnerPO;
import daoha.top.infrastructure.dao.po.AiSkillPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 基于 MyBatis 的 Agent 配置仓储实现。
 */
@Repository
@RequiredArgsConstructor
public class AgentRepository implements IAgentRepository {

    private static final TypeReference<List<Long>> LONG_LIST_TYPE = new TypeReference<>() { };
    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() { };
    private static final TypeReference<Map<String, String>> STRING_MAP_TYPE = new TypeReference<>() { };
    private static final TypeReference<Map<String, Object>> OBJECT_MAP_TYPE = new TypeReference<>() { };

    private final IAiAppDao aiAppDao;
    private final IAiApiDao aiApiDao;
    private final IAiModelDao aiModelDao;
    private final IAiMcpToolDao aiMcpToolDao;
    private final IAiSkillDao aiSkillDao;
    private final IAiAgentDao aiAgentDao;
    private final IAiAgentWorkflowDao aiAgentWorkflowDao;
    private final IAiRunnerDao aiRunnerDao;
    private final AgentDatabaseProperties databaseProperties;
    private final ObjectMapper objectMapper;

    @Override
    public List<AiAppVO> queryEnabledApps() {
        return aiAppDao.queryAllEnabled().stream().map(this::toAppVO).toList();
    }

    @Override
    public AiAppVO queryEnabledAppByAgentId(String agentId) {
        return toAppVO(aiAppDao.queryEnabledByAgentCode(agentId));
    }

    @Override
    public List<AiApiVO> queryAiApisByAppId(Long appId) {
        String masterKey = requireMasterKey();
        return aiApiDao.queryEnabledByAppId(appId, masterKey).stream().map(this::toApiVO).toList();
    }

    @Override
    public List<AiChatModelVO> queryChatModelsByAppId(Long appId) {
        return aiModelDao.queryEnabledByAppId(appId).stream().map(this::toChatModelVO).toList();
    }

    @Override
    public List<AiMcpToolVO> queryMcpToolsByAppId(Long appId) {
        String masterKey = requireMasterKey();
        return aiMcpToolDao.queryEnabledByAppId(appId, masterKey).stream().map(this::toMcpToolVO).toList();
    }

    @Override
    public List<AiSkillVO> querySkillsByAppId(Long appId) {
        return aiSkillDao.queryEnabledByAppId(appId).stream().map(this::toSkillVO).toList();
    }

    @Override
    public List<AiAgentVO> queryAgentsByAppId(Long appId) {
        return aiAgentDao.queryEnabledByAppId(appId).stream().map(this::toAgentVO).toList();
    }

    @Override
    public List<AiAgentWorkflowVO> queryWorkflowsByAppId(Long appId) {
        return aiAgentWorkflowDao.queryEnabledByAppId(appId).stream().map(this::toWorkflowVO).toList();
    }

    @Override
    public AiRunnerVO queryRunnerByAppId(Long appId) {
        return toRunnerVO(aiRunnerDao.queryEnabledByAppId(appId));
    }

    private AiAppVO toAppVO(AiAppPO po) {
        if (po == null) {
            return null;
        }
        return AiAppVO.builder()
                .id(po.getId())
                .appCode(po.getAppCode())
                .appName(po.getAppName())
                .agentId(po.getAgentCode())
                .agentName(po.getAgentName())
                .agentDesc(po.getAgentDescription())
                .build();
    }

    private AiApiVO toApiVO(AiApiPO po) {
        if (isMissingOrPlaceholder(po.getApiKey())) {
            throw new IllegalStateException(
                    "API Key 缺失、仍为占位值或解密失败，请检查密文和 ai.agent.database.master-key，apiCode="
                            + po.getApiCode());
        }
        return AiApiVO.builder()
                .id(po.getId())
                .apiId(po.getApiCode())
                .baseUrl(po.getBaseUrl())
                .apiKey(po.getApiKey())
                .completionsPath(po.getCompletionsPath())
                .embeddingsPath(po.getEmbeddingsPath())
                .build();
    }

    private AiChatModelVO toChatModelVO(AiModelPO po) {
        return AiChatModelVO.builder()
                .id(po.getId())
                .chatId(po.getModelCode())
                .apiRefId(po.getApiId())
                .model(po.getModelName())
                .modelOptions(po.getModelOptions())
                .build();
    }

    private AiMcpToolVO toMcpToolVO(AiMcpToolPO po) {
        if (po.getAuthType() != null
                && !"NONE".equalsIgnoreCase(po.getAuthType())
                && isMissingOrPlaceholder(po.getAuthValue())) {
            throw new IllegalStateException(
                    "MCP 鉴权值缺失、仍为占位值或解密失败，请检查密文和 ai.agent.database.master-key，mcpCode="
                            + po.getMcpCode());
        }

        AiMcpToolVO.AiMcpToolVOBuilder builder = AiMcpToolVO.builder()
                .id(po.getId())
                .mcpToolId(po.getMcpCode());

        String transportType = requireText(po.getTransportType(), "transportType").toUpperCase(Locale.ROOT);
        switch (transportType) {
            case "SSE" -> builder.sse(AiMcpToolVO.SSEServerParameters.builder()
                    .name(po.getToolName())
                    .baseUri(po.getBaseUri())
                    .sseEndpoint(resolveSseEndpoint(po))
                    .requestTimeout(po.getRequestTimeoutMs())
                    .build());
            case "STDIO" -> builder.stdio(AiMcpToolVO.StdioServerParameters.builder()
                    .name(po.getToolName())
                    .requestTimeout(po.getRequestTimeoutMs())
                    .serverParameters(AiMcpToolVO.StdioServerParameters.ServerParameters.builder()
                            .command(po.getCommandText())
                            .args(parseStringList(po.getArgsJson(), "args_json"))
                            .env(parseStringMap(po.getEnvJson(), "env_json"))
                            .build())
                    .build());
            case "LOCAL" -> builder.local(AiMcpToolVO.LocalParameters.builder()
                    .name(requireText(po.getLocalBeanName(), "localBeanName"))
                    .toolName(po.getToolName())
                    .baseUri(po.getBaseUri())
                    .endpointPath(po.getEndpointPath())
                    .requestTimeout(po.getRequestTimeoutMs())
                    .options(parseObjectMap(po.getArgsJson(), "args_json"))
                    .environment(parseStringMap(po.getEnvJson(), "env_json"))
                    .build());
            default -> throw new IllegalArgumentException("不支持的 MCP transportType: " + transportType);
        }
        return builder.build();
    }

    private AiSkillVO toSkillVO(AiSkillPO po) {
        return AiSkillVO.builder()
                .id(po.getId())
                .skillId(po.getSkillCode())
                .type(po.getResourceType())
                .path(po.getResourcePath())
                .build();
    }

    private AiAgentVO toAgentVO(AiAgentPO po) {
        return AiAgentVO.builder()
                .id(po.getId())
                .name(po.getAgentCode())
                .chatModelId(po.getModelId())
                .description(po.getDescription())
                .instruction(po.getInstructionText())
                .outputKey(po.getOutputKey())
                .mcpToolIds(parseLongList(po.getMcpToolIds(), "mcp_tool_ids"))
                .skillIds(parseLongList(po.getSkillIds(), "skill_ids"))
                .toolIds(parseLongList(po.getToolIds(), "tool_ids"))
                .build();
    }

    private AiAgentWorkflowVO toWorkflowVO(AiAgentWorkflowPO po) {
        return AiAgentWorkflowVO.builder()
                .id(po.getId())
                .type(po.getWorkflowType())
                .name(po.getWorkflowCode())
                .description(po.getDescription())
                .subAgentIds(parseLongList(po.getSubAgentIds(), "sub_agent_ids"))
                .maxIterations(po.getMaxIterations())
                .build();
    }

    private AiRunnerVO toRunnerVO(AiRunnerPO po) {
        if (po == null) {
            return null;
        }
        return AiRunnerVO.builder()
                .id(po.getId())
                .agentId(po.getAgentId())
                .workflowId(po.getWorkflowId())
                .pluginNameList(parseStringList(po.getPluginNames(), "plugin_names"))
                .build();
    }

    private String resolveSseEndpoint(AiMcpToolPO po) {
        String endpoint = po.getEndpointPath();
        if (!"QUERY".equalsIgnoreCase(po.getAuthType()) || po.getAuthValue() == null) {
            return endpoint;
        }
        String separator = endpoint != null && endpoint.contains("?") ? "&" : "?";
        return (endpoint == null ? "" : endpoint)
                + separator
                + URLEncoder.encode(requireText(po.getAuthKey(), "authKey"), StandardCharsets.UTF_8)
                + "="
                + URLEncoder.encode(po.getAuthValue(), StandardCharsets.UTF_8);
    }

    private List<Long> parseLongList(String json, String fieldName) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, LONG_LIST_TYPE);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("JSON 字段解析失败: " + fieldName, e);
        }
    }

    private List<String> parseStringList(String json, String fieldName) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, STRING_LIST_TYPE);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("JSON 字段解析失败: " + fieldName, e);
        }
    }

    private Map<String, String> parseStringMap(String json, String fieldName) {
        if (json == null || json.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, STRING_MAP_TYPE);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("JSON 字段解析失败: " + fieldName, e);
        }
    }

    private Map<String, Object> parseObjectMap(String json, String fieldName) {
        if (json == null || json.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, OBJECT_MAP_TYPE);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("JSON 字段解析失败: " + fieldName, e);
        }
    }

    private String requireMasterKey() {
        return requireText(databaseProperties.getMasterKey(), "ai.agent.database.master-key");
    }

    private boolean isMissingOrPlaceholder(String value) {
        return value == null || value.isBlank() || value.startsWith("REPLACE_WITH_");
    }

    private String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " 不能为空");
        }
        return value;
    }
}
