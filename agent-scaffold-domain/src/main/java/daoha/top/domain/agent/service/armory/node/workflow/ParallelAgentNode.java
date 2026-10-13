package daoha.top.domain.agent.service.armory.node.workflow;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.ParallelAgent;
import daoha.top.domain.agent.model.entity.ArmoryCommandEntity;
import daoha.top.domain.agent.model.valobj.AiAgentRegisterVO;
import daoha.top.domain.agent.model.valobj.AiAgentWorkflowVO;
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

        AiAgentWorkflowVO currentAgentWorkflow = dynamicContext.getCurrentAgentWorkflow();

        List<Long> subAgentIds = currentAgentWorkflow.getSubAgentIds();
        List<BaseAgent> baseAgents = dynamicContext.queryAgentList(subAgentIds);

        ParallelAgent parallelAgent =
                ParallelAgent.builder()
                        .name(currentAgentWorkflow.getName())
                        .subAgents(baseAgents)
                        .description(currentAgentWorkflow.getDescription())
                        .build();

        if (dynamicContext.getWorkflowMap().putIfAbsent(currentAgentWorkflow.getId(), parallelAgent) != null) {
            throw new IllegalArgumentException("重复的 Workflow ID: " + currentAgentWorkflow.getId());
        }

        return router(armoryCommandEntity, dynamicContext);
    }

    @Override
    public StrategyHandler<ArmoryCommandEntity, DefaultArmoryFactory.DynamicContext, AiAgentRegisterVO> get(ArmoryCommandEntity armoryCommandEntity, DefaultArmoryFactory.DynamicContext dynamicContext) throws Exception {
        return getBean("agentWorkFlowNode");
    }
}
