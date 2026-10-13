package daoha.top.domain.agent.service.armory.node;

import daoha.top.domain.agent.model.entity.ArmoryCommandEntity;
import daoha.top.domain.agent.model.valobj.AiApiVO;
import daoha.top.domain.agent.model.valobj.AiChatModelVO;
import daoha.top.domain.agent.model.valobj.AiAgentRegisterVO;
import daoha.top.domain.agent.service.armory.AbstractArmorySupport;
import daoha.top.domain.agent.service.armory.factory.DefaultArmoryFactory;
import daoha.top.types.design.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @ClassName : ChatModelNode
 * @Description :
 * @github:
 * @Author : 24209
 * @Date: 2026/9/6  16:33
 */
@Slf4j
@Service
public class ChatModelNode extends AbstractArmorySupport {

    @Resource
    private ToolsNode  toolsNode;

    @Override
    protected AiAgentRegisterVO doApply(ArmoryCommandEntity armoryCommandEntity, DefaultArmoryFactory.DynamicContext dynamicContext) throws Exception {

        log.info("chatModel 装配");

        // 获取上下文对象
        Map<Long, AiApiVO> aiApis = dynamicContext.getAiApis();

        // 获取配置对象
        List<AiChatModelVO> chatModels = dynamicContext.getChatModelConfigs();
        if (chatModels == null) {
            throw new IllegalArgumentException("chatModels 不能为空");
        }

        for (AiChatModelVO chatModelConfig:chatModels){
            Long apiId = chatModelConfig.getApiRefId();
            String chatId = chatModelConfig.getChatId();
            if (chatId == null || chatId.isBlank()) {
                throw new IllegalArgumentException("ChatModel 的 chatId 不能为空");
            }

            AiApiVO aiApi = aiApis.get(apiId);
            if (aiApi == null) {
                throw new IllegalArgumentException("chatId=" + chatId + " 引用了不存在的 apiId=" + apiId);
            }

            ChatModel chatModel = OpenAiChatModel.builder()
                    .options(ChatModelOptionsResolver.resolve(chatModelConfig, resolveOpenAiBaseUrl(aiApi), aiApi.getApiKey()))
                    .build();

            if (dynamicContext.getChatModels().putIfAbsent(chatModelConfig.getId(), chatModel) != null) {
                throw new IllegalArgumentException("重复的 ChatModel ID: " + chatModelConfig.getId());
            }
        }

        return router(armoryCommandEntity, dynamicContext);
    }

    /**
     * Spring AI 2.x uses the official OpenAI SDK, which appends /chat/completions
     * to the configured base URL. Convert the legacy full completions path to
     * the SDK-style base URL while keeping existing YAML files compatible.
     */
    private String resolveOpenAiBaseUrl(AiApiVO aiApi) {
        String baseUrl = StringUtils.removeEnd(aiApi.getBaseUrl(), "/");
        String completionsPath = StringUtils.defaultIfBlank(aiApi.getCompletionsPath(), "/v1/chat/completions");
        completionsPath = "/" + StringUtils.removeStart(completionsPath, "/");

        String endpointSuffix = "/chat/completions";
        if (!completionsPath.endsWith(endpointSuffix)) {
            throw new IllegalArgumentException("OpenAI completionsPath must end with " + endpointSuffix);
        }

        return baseUrl + StringUtils.removeEnd(completionsPath, endpointSuffix);
    }

    @Override
    public StrategyHandler<ArmoryCommandEntity, DefaultArmoryFactory.DynamicContext, AiAgentRegisterVO> get(ArmoryCommandEntity armoryCommandEntity, DefaultArmoryFactory.DynamicContext dynamicContext) throws Exception {
        return toolsNode;
    }
}
