package daoha.top.domain.agent.service.armory.factory;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.agents.SequentialAgent;
import com.google.adk.runner.InMemoryRunner;
import daoha.top.domain.agent.model.valobj.AiAgentConfigTableVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.expression.spel.ast.OpNE;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @ClassName : DefaultArmoryFactory
 * @Description :
 * @github:
 * @Author : 24209
 * @Date: 2026/9/6  16:07
 */

@Service
public class DefaultArmoryFactory {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DynamicContext{

        private OpenAiApi openAiApi;
        private ChatModel chatModel;
        private SequentialAgent sequentialAgent;

        private Map<String,BaseAgent> agentMap;
        private List<AiAgentConfigTableVO.Module.AgentWorkflow> agentWorkflows = new ArrayList<>();


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
    }
}
