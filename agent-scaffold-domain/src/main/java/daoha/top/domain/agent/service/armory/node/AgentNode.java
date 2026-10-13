package daoha.top.domain.agent.service.armory.node;

import com.google.adk.agents.LlmAgent;
import com.google.adk.models.springai.SpringAI;
import com.google.adk.tools.BaseTool;
import daoha.top.domain.agent.model.entity.ArmoryCommandEntity;
import daoha.top.domain.agent.model.valobj.AiAgentRegisterVO;
import daoha.top.domain.agent.model.valobj.AiAgentVO;
import daoha.top.domain.agent.service.armory.AbstractArmorySupport;
import daoha.top.domain.agent.service.armory.factory.DefaultArmoryFactory;
import daoha.top.types.design.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 根据 Agent 上声明的 chatId、mcpToolId 和 skillId 完成 LLM Agent 装配。
 */
@Slf4j
@Service
public class AgentNode extends AbstractArmorySupport {

    @Resource
    private AgentWorkFlowNode agentWorkFlowNode;

    @Override
    protected AiAgentRegisterVO doApply(ArmoryCommandEntity armoryCommandEntity,
                                        DefaultArmoryFactory.DynamicContext dynamicContext) throws Exception {
        log.info("Agent 节点装配");

        List<AiAgentVO> agents = dynamicContext.getAgentConfigs();
        if (agents == null) {
            agents = Collections.emptyList();
        }

        for (AiAgentVO agent : agents) {
            Long agentId = requireId(agent.getId(), "Agent.id");
            String agentName = requireText(agent.getName(), "Agent.name");
            Long chatModelId = requireId(agent.getChatModelId(), "Agent " + agentName + " 的 chatModelId");
            ChatModel chatModel = dynamicContext.getChatModels().get(chatModelId);
            if (chatModel == null) {
                throw new IllegalArgumentException("Agent " + agentName + " 引用了不存在的 chatModelId=" + chatModelId);
            }

            List<BaseTool> tools = resolveTools(agent, dynamicContext);
            LlmAgent llmAgent = LlmAgent.builder()
                    .name(agentName)
                    .description(agent.getDescription())
                    .model(new SpringAI(chatModel))
                    .instruction(agent.getInstruction())
                    .tools(tools)
                    .outputKey(agent.getOutputKey())
                    .build();

            if (dynamicContext.getAgentMap().putIfAbsent(agentId, llmAgent) != null) {
                throw new IllegalArgumentException("重复的 Agent ID: " + agentId);
            }
            log.info("Agent {} 装配完成，chatModelId={}，tools={}",
                    agentName, chatModelId, tools.stream().map(BaseTool::name).toList());
        }

        return router(armoryCommandEntity, dynamicContext);
    }

    private List<BaseTool> resolveTools(AiAgentVO agent,
                                        DefaultArmoryFactory.DynamicContext dynamicContext) {
        Set<String> toolIds = new LinkedHashSet<>();
        addIds(toolIds, agent.getMcpToolIds(), "mcp:");
        addIds(toolIds, agent.getSkillIds(), "skill:");
        addIds(toolIds, agent.getToolIds(), "tool:");

        Map<String, BaseTool> toolsByName = new LinkedHashMap<>();
        for (String toolId : toolIds) {
            List<BaseTool> toolGroup = dynamicContext.getTools().get(toolId);
            if (toolGroup == null) {
                throw new IllegalArgumentException("Agent " + agent.getName()
                        + " 引用了不存在的工具配置 ID=" + toolId);
            }
            for (BaseTool tool : toolGroup) {
                if (toolsByName.putIfAbsent(tool.name(), tool) != null) {
                    throw new IllegalArgumentException("Agent " + agent.getName()
                            + " 装配出重复的工具名称=" + tool.name());
                }
            }
        }
        return new ArrayList<>(toolsByName.values());
    }

    private void addIds(Set<String> destination, List<Long> ids, String prefix) {
        if (ids == null) {
            return;
        }
        for (Long id : ids) {
            destination.add(prefix + requireId(id, "工具配置 ID"));
        }
    }

    private Long requireId(Long value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " 不能为空");
        }
        return value;
    }

    private String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " 不能为空");
        }
        return value;
    }

    @Override
    public StrategyHandler<ArmoryCommandEntity, DefaultArmoryFactory.DynamicContext, AiAgentRegisterVO> get(
            ArmoryCommandEntity armoryCommandEntity,
            DefaultArmoryFactory.DynamicContext dynamicContext) {
        return agentWorkFlowNode;
    }
}
