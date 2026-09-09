package daoha.top.domain.agent.service.armory.factory;

import com.google.adk.agents.BaseAgent;
import daoha.top.domain.agent.model.entity.ArmoryCommandEntity;
import daoha.top.domain.agent.model.valobj.AiAgentConfigTableVO;
import daoha.top.domain.agent.model.valobj.AiAgentRegisterVO;
import daoha.top.domain.agent.service.armory.node.RootNode;
import daoha.top.types.design.tree.StrategyHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @ClassName : DefaultArmoryFactory
 * @Description :
 * @github:
 * @Author : 24209
 * @Date: 2026/9/6  16:07
 */

@Service
public class DefaultArmoryFactory {

    @Resource
    private RootNode rootNode;

    @Resource
    private ApplicationContext applicationContext;

    public StrategyHandler<ArmoryCommandEntity, DynamicContext, AiAgentRegisterVO> armoryStrategyHandler() {
        return rootNode;
    }

    public AiAgentRegisterVO getAiAgentRegisterVOById(String agentId) {
        return applicationContext.getBean(agentId,AiAgentRegisterVO.class);
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DynamicContext{

        private AiAgentConfigTableVO.Module.AiApi aiApi;
        private ChatModel chatModel;

        @Builder.Default
        private Map<String,BaseAgent> agentMap = new HashMap<>();

        @Builder.Default
        private AtomicInteger currentStepIndex = new AtomicInteger(0);
        private AiAgentConfigTableVO.Module.AgentWorkflow currentAgentWorkflow;

        @Builder.Default
        private Map<String,Object> dataObjects = new HashMap<>();

        public <T> void setValue(String key, T value){
            dataObjects.put(key,value);
        }

        public <T> T getValue(String key){
            return (T) dataObjects.get(key);
        }

        public List<BaseAgent> queryAgentList(List<String> agentNames){
            if (agentNames == null || agentNames.isEmpty() || agentMap == null) {
                return Collections.emptyList();
            }

            List<BaseAgent> agents = new ArrayList<>();
            for (String name : agentNames) {
                BaseAgent agent = agentMap.get(name);
                if (agent!=null){
                    agents.add(agent);
                }
            }

            return agents;
        }

        public void addcurrentStepIndex(){
            currentStepIndex.incrementAndGet();
        }

        public int getCurrentStepIndex(){
            return currentStepIndex.get();
        }
    }
}
