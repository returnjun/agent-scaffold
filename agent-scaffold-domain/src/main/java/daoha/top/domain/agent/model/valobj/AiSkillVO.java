package daoha.top.domain.agent.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Agent Skill 资源配置。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiSkillVO {

    private Long id;
    private String skillId;
    @Builder.Default
    private String type = "directory";
    private String path;
}
