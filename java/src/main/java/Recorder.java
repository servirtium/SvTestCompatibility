import org.http4k.core.HttpHandler;
import org.http4k.server.Http4kServer;
import org.http4k.servirtium.ServirtiumRecording;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Recorder {

  public static void main(String[] args) throws IOException {
    String baseUrl = "https://svn.apache.org";
    String interactionFile = "ExampleSubversionCheckoutRecording.md";

    // Combine debugging and recording directly in the handler
    HttpHandler handler = ServirtiumRecording.recording(baseUrl).then(request -> {
      System.out.println("Request: " + request);
      return request;
    }).then(response -> {
      System.out.println("Response: " + response);
      return response;
    });

    // Use Http4kServer with recording specification
    Http4kServer server = Http4kServer.newServer(handler, ServirtiumRecording.spec(interactionFile), 61417);


    server.start();

    System.out.println("Recording started");

    // Wait for 5 seconds before performing the SVN checkout command
    try {
      TimeUnit.SECONDS.sleep(5);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    // Perform the SVN checkout command (unchanged)
    ProcessBuilder pb = new ProcessBuilder("svn", "co", "http://localhost:61417/repos/asf/synapse/tags/3.0.0/modules/distribution/src/main/conf");
    pb.inheritIO();
    try {
      Process process = pb.start();
      int exitCode = process.waitFor();
      if (exitCode != 0) {
        System.err.println("SVN command failed with exit code " + exitCode);
      }
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }

    // Ensure the Servirtium proxy is closed and the record is written when done
    server.stop();
    System.out.println("Recording ended");
  }
}
