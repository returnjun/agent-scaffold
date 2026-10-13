package daoha.top.domain.agent.service.armory.matter.skills;


import daoha.top.domain.agent.model.valobj.AiSkillVO;
import org.springframework.ai.tool.ToolCallback;


public interface IToolSkillsCreateService {

    ToolCallback[] buildToolCallback(AiSkillVO toolSkills) throws Exception;

}
