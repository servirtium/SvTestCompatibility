using System;
using System.Diagnostics;
using System.IO;
using System.Text.RegularExpressions;
using Microsoft.Extensions.Logging;
using Servirtium.AspNetCore;
using Servirtium.Core;
using Servirtium.Core.Http;
using Servirtium.Core.Interactions;

namespace Servirtium.StandaloneServer
{
    public class Program
    {
        internal static readonly string RECORDING_OUTPUT_DIRECTORY = @".\test_recording_output".Replace('\\', Path.DirectorySeparatorChar);
        internal static readonly string SUBVERSION_URL = "https://svn.apache.org/repos/asf/synapse/tags/3.0.0/modules/distribution/src/main/conf";
        internal static readonly ushort PORT = 61417;

        public static void Main(string[] args)
        {
            // Ensure the recording output directory exists
            var scriptDirectory = Directory.CreateDirectory(RECORDING_OUTPUT_DIRECTORY);
            var recordingFilePath = Path.Combine(RECORDING_OUTPUT_DIRECTORY, "recording.md");

            if (string.IsNullOrEmpty(recordingFilePath))
            {
                throw new ArgumentException("The recording file path cannot be null or empty.");
            }

            var loggerFactory = LoggerFactory.Create((builder) =>
            {
                builder.SetMinimumLevel(LogLevel.Debug)
                    .AddFilter(level => true)
                    .AddConsole();
            });
            var logger = loggerFactory.CreateLogger<Program>();

            logger.LogInformation($"Recording file path: {recordingFilePath}");
            logger.LogInformation("Initializing Servirtium Standalone Server in 'record' mode");

            // Initialize MarkdownScriptWriter.Settings
            var settings = new MarkdownScriptWriter.Settings
            {
                // Add necessary settings here if needed
            };

            IInteractionMonitor monitor = new InteractionRecorder(
                recordingFilePath,
                new MarkdownScriptWriter(settings, loggerFactory),
                false,
                loggerFactory,
                // Write each interaction after it completes rather than all at the end, so it isn't disrupted by unceremonious teardowns (like via a docker container stopping).
                InteractionRecorder.RecordTime.AfterEachInteraction
            );

            var server = AspNetCoreServirtiumServer.WithCommandLineArgs(
                new[] { $"--urls=http://*:{PORT}" },
                monitor,
                new HttpMessageTransformPipeline(
                    new SimpleHttpMessageTransforms(
                        new Uri(SUBVERSION_URL),
                        new Regex[] { },
                        new[] { new Regex("Date:"), new Regex("Via:"),
                            new Regex("^X-"), new Regex("^Server:") },
                        loggerFactory
                    ),
                    new FindAndReplaceHttpMessageTransforms(
                        new[] {
                            new RegexReplacement(new Regex(Regex.Escape(SUBVERSION_URL)), $"http://localhost:{PORT}", ReplacementContext.ResponseBody)
                        }, loggerFactory)
                ),
                loggerFactory
            );

            logger.LogInformation("Starting up Servirtium Standalone Server.");

            // ProcessExit hook must be attached before server.Start() is called.
            // server.Start() attaches a standard ASP.NET ProcessExit hook and we need this one to run first,
            AppDomain.CurrentDomain.ProcessExit += (a, e) =>
            {
                logger.LogInformation("Servirtium Standalone Server attempting graceful shutdown.");
                server.Stop().Wait();
            };

            try
            {
                server.Start().Wait();
                logger.LogInformation("Servirtium Standalone Server started and listening.");
                while (Console.ReadLine()?.ToLower() != "exit") { }
                logger.LogDebug("Servirtium Standalone Server received 'exit' command, shutting down.");
            }
            catch (Exception ex)
            {
                logger.LogError(ex, "An error occurred while starting the Servirtium Standalone Server.");
            }
            finally
            {
                server.Stop().Wait();
            }
        }
    }
}
