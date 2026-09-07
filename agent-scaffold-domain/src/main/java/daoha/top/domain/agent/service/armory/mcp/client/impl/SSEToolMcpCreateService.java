package daoha.top.domain.agent.service.armory.mcp.client.impl;

import daoha.top.domain.agent.model.valobj.AiAgentConfigTableVO;
import daoha.top.domain.agent.service.armory.mcp.client.TooMcpCreateService;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.time.Duration;

/**
 * @ClassName : SSEToolMcpCreateService
 * @Description :
 * @github:
 * @Author : 24209
 * @Date: 2026/9/7  15:09
 */
@Slf4j
@Service
public class SSEToolMcpCreateService implements TooMcpCreateService {
    @Override
    public ToolCallback[] buildToolCallback(AiAgentConfigTableVO.Module.ChatModel.ToolMcp toolMcp) throws Exception {
        AiAgentConfigTableVO.Module.ChatModel.ToolMcp.SSEServerParameters sse = toolMcp.getSse();

        String originalBaseUri = sse.getBaseUri();
        String baseUri = originalBaseUri;
        String sseEndpoint = sse.getSseEndpoint();
        if (StringUtils.isBlank(sseEndpoint)) {

            URL url = new URL(originalBaseUri);

            String protocol = url.getProtocol();
            String host = url.getHost();
            int port = url.getPort();

            String baseUrl = port == -1 ? protocol + "://" + host : protocol + "://" + host + ":" + port;

            int index = originalBaseUri.indexOf(baseUrl);
            if (index != -1) {
                sseEndpoint = originalBaseUri.substring(index + baseUrl.length());
            }

            baseUri = baseUrl;
        }

        sseEndpoint = StringUtils.isBlank(sseEndpoint) ? "/sse" : sseEndpoint;

        HttpClientSseClientTransport sseClientTransport = HttpClientSseClientTransport
                .builder(baseUri)
                .sseEndpoint(sseEndpoint)
                .build();

        McpSyncClient mcpSyncClient = McpClient.sync(sseClientTransport).requestTimeout(Duration.ofMinutes(360)).build();
        var init_sse = mcpSyncClient.initialize();
        log.info("Tool SSE MCP Initialized {}", init_sse);

        return SyncMcpToolCallbackProvider.builder()
                .mcpClients(mcpSyncClient).build()
                .getToolCallbacks();

    }
}
