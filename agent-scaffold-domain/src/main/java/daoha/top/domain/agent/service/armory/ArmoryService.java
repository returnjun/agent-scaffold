package daoha.top.domain.agent.service.armory;

import daoha.top.domain.agent.model.entity.ArmoryCommandEntity;
import daoha.top.domain.agent.model.valobj.AiAgentConfigTableVO;
import daoha.top.domain.agent.model.valobj.AiAgentRegisterVO;
import daoha.top.domain.agent.service.IArmoryService;
import daoha.top.domain.agent.service.armory.factory.DefaultArmoryFactory;
import daoha.top.types.design.tree.StrategyHandler;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * @ClassName : ArmoryService
 * @Description :
 * @github:
 * @Author : 24209
 * @Date: 2026/9/6  16:01
 */
@Service
public class ArmoryService implements IArmoryService {
    @Resource
    private DefaultArmoryFactory defaultArmoryFactory;

    @Override
    public void acceptArmoryAgents(List<AiAgentConfigTableVO> tables) throws Exception {
        for (AiAgentConfigTableVO table : tables) {
            StrategyHandler<ArmoryCommandEntity, DefaultArmoryFactory.DynamicContext, AiAgentRegisterVO> handler = defaultArmoryFactory.armoryStrategyHandler();
            handler.apply(ArmoryCommandEntity.builder()
                            .aiAgentConfigTableVO(table)
                            .build(),
                    new DefaultArmoryFactory.DynamicContext());
        }
    }
}
