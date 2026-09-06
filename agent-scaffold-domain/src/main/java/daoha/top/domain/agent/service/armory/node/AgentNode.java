package daoha.top.domain.agent.service.armory.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.google.adk.agents.LlmAgent;
import com.google.adk.models.springai.SpringAI;
import daoha.top.domain.agent.model.entity.ArmoryCommandEntity;
import daoha.top.domain.agent.model.valobj.AiAgentConfigTableVO;
import daoha.top.domain.agent.model.valobj.AiAgentRegisterVO;
import daoha.top.domain.agent.service.armory.AbstractArmorySupport;
import daoha.top.domain.agent.service.armory.factory.DefaultArmoryFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.util.List;

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


    @Override
    protected AiAgentRegisterVO doApply(ArmoryCommandEntity armoryCommandEntity, DefaultArmoryFactory.DynamicContext dynamicContext) throws Exception {
        log.info("agent节点装配");

        ChatModel chatModel = dynamicContext.getChatModel();
        AiAgentConfigTableVO aiAgentConfigTableVO = armoryCommandEntity.getAiAgentConfigTableVO();
        List<AiAgentConfigTableVO.Module.Agent> agents = aiAgentConfigTableVO.getModule().getAgents();

        for(AiAgentConfigTableVO.Module.Agent agent : agents){
            LlmAgent llm = LlmAgent.builder()
                    .name(agent.getName())
                    .description(agent.getDescription())
                    .model(new SpringAI(chatModel))
                    .instruction(agent.getInstruction())
                    .outputKey(agent.getOutputKey())
                    .build();

            dynamicContext.getAgentMap().put(agent.getName(), llm);
        }


        return router(armoryCommandEntity, dynamicContext);
    }

    @Override
    public StrategyHandler<ArmoryCommandEntity, DefaultArmoryFactory.DynamicContext, AiAgentRegisterVO> get(ArmoryCommandEntity armoryCommandEntity, DefaultArmoryFactory.DynamicContext dynamicContext) throws Exception {
        return defaultStrategyHandler;
    }
}
