package com.example;

import org.http4k.client.ApacheClient;
import org.http4k.core.*;
import org.http4k.server.SunHttp;
import org.http4k.servirtium.InteractionStorage;
import org.http4k.servirtium.InteractionOptions;
import org.http4k.servirtium.ServirtiumServer;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Recorder {
    public static void main(String[] args) throws IOException, InterruptedException {

        InteractionOptions io = new  InteractionOptions() {

            public Request modify(Request request) {
                return request
                        .replaceHeader("Host", "svn.apache.org")
                        .replaceHeader("User-agent", "Svn CLI")
                        .replaceHeader("Date", "Tue, 28 Jan 2020 14:15:55 GMT");
            }

            public Response modify(Response response) {
                return response
                        //.removeHeaders("X-")
                        //.removeHeader("Set-Cookie")
                        .replaceHeader("Date", "Tue, 28 Jan 2020 14:15:55 GMT");
            }


            public boolean debugTraffic() {
                return true;
            }
        };

        ServirtiumServer server = ServirtiumServer.Recording(
            "ExampleSubversionCheckoutRecording", Uri.of("https://svn.apache.org"),
            InteractionStorage.Disk(new File("..")),
            io, 61417,
            SunHttp::new,
            ApacheClient.create()
        );

        server.start();
        System.out.println("Recording started");

        TimeUnit.SECONDS.sleep(5);

        ProcessBuilder pb = new ProcessBuilder("svn", "co", "http://localhost:61417/repos/asf/synapse/tags/3.0.0/modules/distribution/src/main/conf");
        pb.inheritIO();
        Process process = pb.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            System.err.println("SVN command failed with exit code " + exitCode);
        }

        server.stop();
        System.out.println("Recording ended");
    }
}
