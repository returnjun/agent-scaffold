package daoha.top.domain.agent.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Agent 使用的聊天模型配置。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatModelVO {

    private Long id;
    private String chatId;
    private Long apiRefId;
    private String model;
    private String modelOptions;
}
