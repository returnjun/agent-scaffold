package daoha.top.domain.agent.service.armory.node.workflow;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.ParallelAgent;
import daoha.top.domain.agent.model.entity.ArmoryCommandEntity;
import daoha.top.domain.agent.model.valobj.AiAgentConfigTableVO;
import daoha.top.domain.agent.model.valobj.AiAgentRegisterVO;
import daoha.top.domain.agent.model.valobj.enums.AgentTypeEnum;
import daoha.top.domain.agent.service.armory.AbstractArmorySupport;
import daoha.top.domain.agent.service.armory.factory.DefaultArmoryFactory;
import daoha.top.types.design.tree.StrategyHandler;
import daoha.top.domain.agent.service.armory.node.AgentWorkFlowNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @ClassName : ParallelAgentNode
 * @Description :
 * @github:
 * @Author : 24209
 * @Date: 2026/9/6  21:12
 */
@Slf4j
@Service("parallelAgentNode")
public class ParallelAgentNode extends AbstractArmorySupport {

    @Override
    protected AiAgentRegisterVO doApply(ArmoryCommandEntity armoryCommandEntity, DefaultArmoryFactory.DynamicContext dynamicContext) throws Exception {
        log.info("agent装配的parallel并行的装配");

        AiAgentConfigTableVO.Module.AgentWorkflow currentAgentWorkflow = dynamicContext.getCurrentAgentWorkflow();

        List<String> subAgentnames = currentAgentWorkflow.getSubAgents();
        List<BaseAgent> baseAgents = dynamicContext.queryAgentList(subAgentnames);

        ParallelAgent parallelAgent =
                ParallelAgent.builder()
                        .name(currentAgentWorkflow.getName())
                        .subAgents(baseAgents)
                        .description(currentAgentWorkflow.getDescription())
                        .build();

        dynamicContext.getAgentMap().put(currentAgentWorkflow.getName(),parallelAgent);

        return router(armoryCommandEntity, dynamicContext);
    }

    @Override
    public StrategyHandler<ArmoryCommandEntity, DefaultArmoryFactory.DynamicContext, AiAgentRegisterVO> get(ArmoryCommandEntity armoryCommandEntity, DefaultArmoryFactory.DynamicContext dynamicContext) throws Exception {
        return getBean("agentWorkFlowNode");
    }
}
