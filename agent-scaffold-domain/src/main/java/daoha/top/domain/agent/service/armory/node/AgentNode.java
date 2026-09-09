package daoha.top.domain.agent.service.armory.node;

import com.google.adk.agents.LlmAgent;
import com.google.adk.models.springai.SpringAI;
import com.google.adk.tools.BaseTool;
import daoha.top.domain.agent.model.entity.ArmoryCommandEntity;
import daoha.top.domain.agent.model.valobj.AiAgentConfigTableVO;
import daoha.top.domain.agent.model.valobj.AiAgentRegisterVO;
import daoha.top.domain.agent.service.armory.AbstractArmorySupport;
import daoha.top.domain.agent.service.armory.factory.DefaultArmoryFactory;
import daoha.top.domain.agent.service.armory.matter.mcp.client.TooMcpCreateService;
import daoha.top.domain.agent.service.armory.matter.mcp.client.factory.DefaultMcpClientFactory;
import daoha.top.domain.agent.service.armory.matter.skills.IToolSkillsCreateService;
import daoha.top.domain.agent.service.armory.matter.tool.SpringAiToolCallbackAdapter;
import daoha.top.types.design.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName : AgentNode
 * @Description :
 * @github:
 * @Author : 24209
 * @Date: 2026/9/6  18:11
 */
@Slf4j
@Service
public class AgentNode extends AbstractArmorySupport {

    @Resource
    private AgentWorkFlowNode agentWorkFlowNode;

    @Resource
    private DefaultMcpClientFactory mcpClientFactory;

    @Resource
    private IToolSkillsCreateService toolSkillsCreateService;

    @Override
    protected AiAgentRegisterVO doApply(ArmoryCommandEntity armoryCommandEntity, DefaultArmoryFactory.DynamicContext dynamicContext) throws Exception {
        log.info("agent节点装配");

        ChatModel chatModel = dynamicContext.getChatModel();
        AiAgentConfigTableVO aiAgentConfigTableVO = armoryCommandEntity.getAiAgentConfigTableVO();
        List<BaseTool> tools = buildTools(aiAgentConfigTableVO.getModule().getChatModel());

        List<AiAgentConfigTableVO.Module.Agent> agents = aiAgentConfigTableVO.getModule().getAgents();

        for(AiAgentConfigTableVO.Module.Agent agent : agents){
            LlmAgent llm = LlmAgent.builder()
                    .name(agent.getName())
                    .description(agent.getDescription())
                    .model(new SpringAI(chatModel))
                    .instruction(agent.getInstruction())
                    .tools(tools)
                    .outputKey(agent.getOutputKey())
                    .build();

            dynamicContext.getAgentMap().put(agent.getName(), llm);
        }


        return router(armoryCommandEntity, dynamicContext);
    }

    private List<BaseTool> buildTools(AiAgentConfigTableVO.Module.ChatModel chatModelConfig) throws Exception {
        Map<String, BaseTool> toolsByName = new LinkedHashMap<>();

        List<AiAgentConfigTableVO.Module.ChatModel.ToolMcp> toolMcpList = chatModelConfig.getToolMcpList();
        if (toolMcpList != null) {
            for (AiAgentConfigTableVO.Module.ChatModel.ToolMcp toolMcp : toolMcpList) {
                TooMcpCreateService mcpCreateService = mcpClientFactory.getLocalToolMcpCreateService(toolMcp);
                registerTools(toolsByName, mcpCreateService.buildToolCallback(toolMcp));
            }
        }

        List<AiAgentConfigTableVO.Module.ChatModel.ToolSkills> toolSkillsList = chatModelConfig.getToolSkillsList();
        if (toolSkillsList != null) {
            for (AiAgentConfigTableVO.Module.ChatModel.ToolSkills toolSkills : toolSkillsList) {
                registerTools(toolsByName, toolSkillsCreateService.buildToolCallback(toolSkills));
            }
            log.info("tool注册成功");
        }

        log.info("为 ADK Agent 注册工具: {}", toolsByName.keySet());
        return new ArrayList<>(toolsByName.values());
    }

    private void registerTools(Map<String, BaseTool> toolsByName, ToolCallback[] callbacks) {
        if (callbacks == null) {
            return;
        }

        for (ToolCallback callback : callbacks) {
            SpringAiToolCallbackAdapter tool = new SpringAiToolCallbackAdapter(callback);
            BaseTool previous = toolsByName.putIfAbsent(tool.name(), tool);
            if (previous != null) {
                throw new IllegalArgumentException("Duplicate tool name: " + tool.name());
            }
        }
    }

    @Override
    public StrategyHandler<ArmoryCommandEntity, DefaultArmoryFactory.DynamicContext, AiAgentRegisterVO> get(ArmoryCommandEntity armoryCommandEntity, DefaultArmoryFactory.DynamicContext dynamicContext) throws Exception {


        return agentWorkFlowNode;
    }
}
