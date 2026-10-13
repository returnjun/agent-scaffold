package daoha.top.domain.agent.service.armory.node;

import com.google.adk.tools.BaseTool;
import daoha.top.domain.agent.model.entity.ArmoryCommandEntity;
import daoha.top.domain.agent.model.valobj.AiAgentRegisterVO;
import daoha.top.domain.agent.model.valobj.AiMcpToolVO;
import daoha.top.domain.agent.model.valobj.AiSkillVO;
import daoha.top.domain.agent.service.armory.AbstractArmorySupport;
import daoha.top.domain.agent.service.armory.factory.DefaultArmoryFactory;
import daoha.top.domain.agent.service.armory.matter.mcp.client.TooMcpCreateService;
import daoha.top.domain.agent.service.armory.matter.mcp.client.factory.DefaultMcpClientFactory;
import daoha.top.domain.agent.service.armory.matter.skills.IToolSkillsCreateService;
import daoha.top.domain.agent.service.armory.matter.tool.SpringAiToolCallbackAdapter;
import daoha.top.types.design.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 将 Module 中的 MCP/Skill 配置实例化，并以配置 ID 为键写入装配上下文。
 */
@Slf4j
@Service
public class ToolsNode extends AbstractArmorySupport {

    @Resource
    private AgentNode agentNode;

    @Resource
    private DefaultMcpClientFactory mcpClientFactory;

    @Resource
    private IToolSkillsCreateService toolSkillsCreateService;

    @Override
    protected AiAgentRegisterVO doApply(ArmoryCommandEntity armoryCommandEntity,
                                        DefaultArmoryFactory.DynamicContext dynamicContext) throws Exception {
        log.info("Tools 节点装配");

        List<AiMcpToolVO> mcpConfigs = dynamicContext.getMcpToolConfigs();
        if (mcpConfigs == null) {
            mcpConfigs = Collections.emptyList();
        }

        for (AiMcpToolVO mcpConfig : mcpConfigs) {
            Long toolId = requireId(mcpConfig == null ? null : mcpConfig.getId(), "MCP Tool ID");
            TooMcpCreateService createService = mcpClientFactory.getToolMcpCreateService(mcpConfig);
            registerToolGroup(dynamicContext, "mcp:" + toolId,
                    adaptCallbacks(createService.buildToolCallback(mcpConfig,
                            dynamicContext.getApiConfigs())));
        }

        List<AiSkillVO> skillConfigs = dynamicContext.getSkillConfigs();
        if (skillConfigs == null) {
            skillConfigs = Collections.emptyList();
        }

        for (AiSkillVO skillConfig : skillConfigs) {
            Long toolId = requireId(skillConfig == null ? null : skillConfig.getId(), "Skill ID");
            registerToolGroup(dynamicContext, "skill:" + toolId,
                    adaptCallbacks(toolSkillsCreateService.buildToolCallback(skillConfig)));
        }

        log.info("工具配置装配完成: {}", dynamicContext.getTools().keySet());
        return router(armoryCommandEntity, dynamicContext);
    }

    private List<BaseTool> adaptCallbacks(ToolCallback[] callbacks) {
        if (callbacks == null || callbacks.length == 0) {
            return Collections.emptyList();
        }

        Map<String, BaseTool> toolsByName = new LinkedHashMap<>();
        for (ToolCallback callback : callbacks) {
            SpringAiToolCallbackAdapter tool = new SpringAiToolCallbackAdapter(callback);
            if (toolsByName.putIfAbsent(tool.name(), tool) != null) {
                throw new IllegalArgumentException("同一工具配置中存在重复的工具名称: " + tool.name());
            }
        }
        return List.copyOf(toolsByName.values());
    }

    private void registerToolGroup(DefaultArmoryFactory.DynamicContext dynamicContext,
                                   String toolId,
                                   List<BaseTool> tools) {
        if (dynamicContext.getTools().putIfAbsent(toolId, tools) != null) {
            throw new IllegalArgumentException("重复的工具配置 ID: " + toolId);
        }
    }

    private Long requireId(Long id, String fieldName) {
        if (id == null) {
            throw new IllegalArgumentException(fieldName + " 不能为空");
        }
        return id;
    }

    @Override
    public StrategyHandler<ArmoryCommandEntity, DefaultArmoryFactory.DynamicContext, AiAgentRegisterVO> get(
            ArmoryCommandEntity armoryCommandEntity,
            DefaultArmoryFactory.DynamicContext dynamicContext) {
        return agentNode;
    }
}
