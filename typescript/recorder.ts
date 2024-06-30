import Servirtium, { IServirtium } from '@servirtium/recorder';
import { exec } from 'child_process';

let servirtium: IServirtium;
const baseUrl = 'https://svn.apache.org';

servirtium = new Servirtium(baseUrl);

servirtium.setCallerRequestHeaderReplacements({
    "user-agent: (.*)": "user-agent: Servirtium-Agent"
});
servirtium.setRecordResponseHeaderReplacements({
    "set-cookie: (.*)": "set-cookie: MASKED",
    "date: (.*)": "date: Sun, 09 Aug 2020 18:42:45 GMT",
    "for_testing: (.*)": "for_testing: SERVIRTIUM-REDACTED"
});
servirtium.setRecordResponseHeadersRemoval([
    "x-github-request-id", "expires", "age", "x-timer", "x-cache",
    "x-cache-hits", "x-served-by", "x-fastly-request-id", "x-origin-cache"
]);

// Set the test name for recording
servirtium.setTestName("ExampleSubversionCheckoutRecording");

servirtium.startRecord(() => {
    console.log('Recording started');

    // Wait for 5 seconds before performing the SVN checkout command
    setTimeout(() => {
        console.log('Svn command issued as sub process');
        exec(`svn co http://localhost:61417/repos/asf/synapse/tags/3.0.0/modules/distribution/src/main/conf`, (error, stdout, stderr) => {
            if (error) {
                console.error(`exec error: ${error}`);
                return;
            }
            console.log(`stdout: ${stdout}`);
            console.error(`stderr: ${stderr}`);

            // Ensure the Servirtium proxy is closed and the record is written when done
            servirtium.writeRecord().then(() => {
                console.log('Recording written');
                servirtium.endRecord(() => {

                    console.log('Recording ended');
                    process.exit();
                });
            });
        });
    }, 5000); // 5 seconds delay
});
