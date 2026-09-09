package daoha.top.domain.agent.service.armory.matter.skills;


import daoha.top.domain.agent.model.valobj.AiAgentConfigTableVO;
import org.springframework.ai.tool.ToolCallback;


public interface IToolSkillsCreateService {

    ToolCallback[] buildToolCallback(AiAgentConfigTableVO.Module.ChatModel.ToolSkills toolSkills) throws Exception;

}
