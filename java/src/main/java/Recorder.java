import org.http4k.core.HttpHandler;
import org.http4k.core.Method;
import org.http4k.core.Request;
import org.http4k.core.Response;
import org.http4k.core.Status;
import org.http4k.server.SunHttp;
import org.http4k.servirtium.InteractionStorage;
import org.http4k.servirtium.ServirtiumServer;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Recorder {
    public static void main(String[] args) throws IOException {
        String baseUrl = "https://svn.apache.org";
        String interactionFile = "ExampleSubversionCheckoutRecording.md";

        HttpHandler handler = request -> {
            System.out.println("Request: " + request);
            return Response.create().status(Status.OK);
        };

        ServirtiumServer server = ServirtiumServer.Recording(
            baseUrl,
            SunHttp(61417),
            InteractionStorage.Disk(new File(interactionFile)),
            handler
        );

        server.start();
        System.out.println("Recording started");

        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

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

        server.stop();
        System.out.println("Recording ended");
    }
}
