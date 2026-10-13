package daoha.top.domain.agent.service.armory.matter.skills.impl;


import daoha.top.domain.agent.model.valobj.AiSkillVO;
import daoha.top.domain.agent.service.armory.matter.skills.IToolSkillsCreateService;
import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.agent.tools.SkillsTool;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@Service
public class DefaultIToolSkillsCreateService implements IToolSkillsCreateService {

    @Override
    public ToolCallback[] buildToolCallback(AiSkillVO toolSkills) throws Exception {

        String type = toolSkills.getType();
        String path = toolSkills.getPath();
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("Skill " + toolSkills.getSkillId() + " 的 path 不能为空");
        }

        List<ToolCallback> toolCallbackList = new ArrayList<>();

        if ("directory".equals(type)){
            ToolCallback toolCallback = SkillsTool.builder()
                    .addSkillsDirectory(path)
                    .build();
            toolCallbackList.add(toolCallback);
        }

        if ("resource".equals(type)){
            ToolCallback toolCallback = SkillsTool.builder()
                    .addSkillsResource(new ClassPathResource(path))
                    .build();
            toolCallbackList.add(toolCallback);
        }

        if (toolCallbackList.isEmpty()) {
            throw new IllegalArgumentException("Skill " + toolSkills.getSkillId()
                    + " 的 type 仅支持 directory 或 resource，当前值: " + type);
        }

        return toolCallbackList.toArray(new ToolCallback[0]);
    }

}
