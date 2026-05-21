package org.mqubits.mcp;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class MCPClient {
  McpSyncClient _client;

  private static final Logger logger = Logger.getLogger(MCPClient.class);

  public String connect() {
    McpClientTransport transport = HttpClientSseClientTransport
      .builder("http://localhost:8080/mcp/sse")
      .sseEndpoint("/mcp/sse")
      .connectTimeout(Duration.ofSeconds(10))
      .build();
    this._client = McpClient.sync(transport).build();

    McpSchema.InitializeResult initRes = this._client.initialize();
    return initRes.serverInfo().name();
  }

  public void explainClient() {
    logger.info("*** Client Info:");
    logger.info(this._client.getClientInfo().name());
    logger.info(this._client.getClientInfo().title());
    logger.info(this._client.getClientInfo().version());
    logger.info("**** Tools");
    for (McpSchema.Tool tool : this._client.listTools().tools()) {
      logger.info("+ " + tool.title());
      logger.info("\t " + tool.description());
    }
  }

  public void callRepeater(String tool) {
    Map<String, Object> payload = Map.of("prompt", "lorem ipsum amet dolor");
    McpSchema.CallToolRequest req = new McpSchema.CallToolRequest(tool, payload);
    McpSchema.CallToolResult response = this._client.callTool(req);

    if (response.isError()) {
      List<String> errors = response.content().stream()
        .filter(l -> l.type().equals(McpSchema.TextContent.class.getTypeName()))
        .map(l -> ((McpSchema.TextContent) l).text())
        .toList();
      for (String err :
        errors) {
        logger.error(err);
      }
      return;
    }

    List<McpSchema.TextContent> results = response.content().stream()
      .filter(content -> content.getClass().getTypeName().equals(McpSchema.TextContent.class.getTypeName()))
      .map(content -> (McpSchema.TextContent) content)
      .toList();

    for (McpSchema.TextContent content: results ) {
      logger.info(content.text());
    }
  }
}
