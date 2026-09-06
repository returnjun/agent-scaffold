package daoha.top.domain.agent.model.valobj;

import com.google.adk.runner.InMemoryRunner;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName : AiAgentRegisterVO
 * @Description :
 * @github:
 * @Author : 24209
 * @Date: 2026/9/6  16:16
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiAgentRegisterVO {
    //智能体名称
    private String appName;
    //智能体ID
    private String agentId;
    //智能体名称
    private String agentName;
    //智能体描述
    private String agentDesc;
    //智能体执行对象
    private InMemoryRunner  runner;

}
