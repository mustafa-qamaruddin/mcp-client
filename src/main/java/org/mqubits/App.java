package org.mqubits;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.mqubits.mcp.MCPClient;

@QuarkusMain
public class App {

  public static void main(String[] args) {
    Quarkus.run(QApp.class,
      (exitCode, exception) -> {
        Logger.getLogger(App.class).error("Error:" + exitCode + ", " + exception.getMessage());
      },
      args);
  }

  public static class QApp implements QuarkusApplication {
    @Inject
    MCPClient mcpClient;

    @Override
    public int run(String... args) throws Exception {
      String serverName = mcpClient.connect();
      Logger.getLogger(this.getClass().getName()).info("(*) Connected to " + serverName);
      mcpClient.explainClient();
      mcpClient.callRepeater("repeater");
      Quarkus.waitForExit();
      return 0;
    }
  }
}
