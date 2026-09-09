package daoha.top.domain.agent.service.armory.node;

import daoha.top.domain.agent.model.entity.ArmoryCommandEntity;
import daoha.top.domain.agent.model.valobj.AiAgentConfigTableVO;
import daoha.top.domain.agent.model.valobj.AiAgentRegisterVO;
import daoha.top.domain.agent.service.armory.AbstractArmorySupport;
import daoha.top.domain.agent.service.armory.factory.DefaultArmoryFactory;
import daoha.top.types.design.tree.StrategyHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

/**
 * @ClassName : ApiNode
 * @Description :
 * @github:
 * @Author : 24209
 * @Date: 2026/9/6  16:19
 */
@Slf4j
@Service
public class ApiNode extends AbstractArmorySupport {

    @Resource
    private ChatModelNode chatModelNode;

    @Override
    protected AiAgentRegisterVO doApply(ArmoryCommandEntity requestCommandEntity, DefaultArmoryFactory.DynamicContext dynamicContext) throws Exception {
        //api实例化
        log.info("Api节点装配");
        AiAgentConfigTableVO aiAgentConfigTableVO = requestCommandEntity.getAiAgentConfigTableVO();
        AiAgentConfigTableVO.Module.AiApi aiApiConfig = aiAgentConfigTableVO.getModule().getAiApi();

        dynamicContext.setAiApi(aiApiConfig);

        return router(requestCommandEntity, dynamicContext);
    }

    @Override
    public StrategyHandler<ArmoryCommandEntity, DefaultArmoryFactory.DynamicContext, AiAgentRegisterVO> get(ArmoryCommandEntity armoryCommandEntity, DefaultArmoryFactory.DynamicContext dynamicContext) throws Exception {
        return chatModelNode;
    }
}
