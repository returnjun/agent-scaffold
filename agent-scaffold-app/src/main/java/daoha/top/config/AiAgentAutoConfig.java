package daoha.top.config;


import daoha.top.domain.agent.service.IArmoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.Resource;

@Slf4j
@Configuration
public class AiAgentAutoConfig implements ApplicationListener<ApplicationReadyEvent> {

    @Resource
    private IArmoryService armoryService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            log.info("开始从数据库加载并装配已启用的 AI Agent");
            armoryService.acceptArmoryAgents();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
