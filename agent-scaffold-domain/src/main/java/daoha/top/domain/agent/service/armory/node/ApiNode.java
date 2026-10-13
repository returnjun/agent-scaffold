package daoha.top.domain.agent.service.armory.node;

import daoha.top.domain.agent.model.entity.ArmoryCommandEntity;
import daoha.top.domain.agent.model.valobj.AiApiVO;
import daoha.top.domain.agent.model.valobj.AiAgentRegisterVO;
import daoha.top.domain.agent.service.armory.AbstractArmorySupport;
import daoha.top.domain.agent.service.armory.factory.DefaultArmoryFactory;
import daoha.top.types.design.tree.StrategyHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

import java.util.Collections;
import java.util.List;

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
        List<AiApiVO> aiApis = dynamicContext.getApiConfigs();
        if (aiApis == null) {
            aiApis = Collections.emptyList();
        }

        for (AiApiVO aiApi:aiApis){
            if (aiApi == null || aiApi.getId() == null) {
                throw new IllegalArgumentException("AI API 的数据库 ID 不能为空");
            }
            if (dynamicContext.getAiApis().putIfAbsent(aiApi.getId(), aiApi) != null) {
                throw new IllegalArgumentException("重复的 AI API ID: " + aiApi.getId());
            }
        }


        return router(requestCommandEntity, dynamicContext);
    }

    @Override
    public StrategyHandler<ArmoryCommandEntity, DefaultArmoryFactory.DynamicContext, AiAgentRegisterVO> get(ArmoryCommandEntity armoryCommandEntity, DefaultArmoryFactory.DynamicContext dynamicContext) throws Exception {
        return chatModelNode;
    }
}
