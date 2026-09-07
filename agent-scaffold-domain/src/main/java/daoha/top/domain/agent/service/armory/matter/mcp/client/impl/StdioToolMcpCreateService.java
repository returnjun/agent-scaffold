package daoha.top.domain.agent.service.armory.matter.mcp.client.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import daoha.top.domain.agent.model.valobj.AiAgentConfigTableVO;
import daoha.top.domain.agent.service.armory.matter.mcp.client.TooMcpCreateService;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.json.jackson.JacksonMcpJsonMapper;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * @ClassName : StdioToolMcpCreateService
 * @Description :
 * @github:
 * @Author : 24209
 * @Date: 2026/9/7  15:10
 */
@Slf4j
@Service
public class StdioToolMcpCreateService implements TooMcpCreateService {
    @Override
    public ToolCallback[] buildToolCallback(AiAgentConfigTableVO.Module.ChatModel.ToolMcp toolMcp) throws Exception {

        AiAgentConfigTableVO.Module.ChatModel.ToolMcp.StdioServerParameters stdio = toolMcp.getStdio();

        AiAgentConfigTableVO.Module.ChatModel.ToolMcp.StdioServerParameters.ServerParameters serverParameters = stdio.getServerParameters();

        ServerParameters stdioParams = ServerParameters.builder(serverParameters.getCommand())
                .args(serverParameters.getArgs())
                .env(serverParameters.getEnv())
                .build();

        McpSyncClient mcpSyncClient = McpClient.sync(new StdioClientTransport(stdioParams, new JacksonMcpJsonMapper(new ObjectMapper())))
                .requestTimeout(Duration.ofSeconds(stdio.getRequestTimeout())).build();

        McpSchema.InitializeResult initialize = mcpSyncClient.initialize();

        return SyncMcpToolCallbackProvider.builder()
                .mcpClients(mcpSyncClient).build()
                .getToolCallbacks();
    }
}
