package daoha.top.domain.agent.model.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArmoryCommandEntity {

    /** 对外智能体 ID，对应 ai_app.agent_code。 */
    private String agentId;

}
