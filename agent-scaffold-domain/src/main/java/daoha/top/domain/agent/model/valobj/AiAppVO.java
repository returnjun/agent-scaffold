package daoha.top.domain.agent.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 可装配的智能体应用。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiAppVO {

    private Long id;
    private String appCode;
    private String appName;
    private String agentId;
    private String agentName;
    private String agentDesc;
}
