# SvTestCompatibility

The markdown recording at the root of this repo was previously made by Servertium being in the middle of the command `svn co https://svn.apache.org/repos/asf/synapse/tags/3.0.0/modules/distribution/src/main/conf` and the apache Subversion server.

That would look like:

1. Launch your Servirtium Apache-Subversion recorded as a standalone process
2. Run `svn co http://localhost:61417/repos/asf/synapse/tags/3.0.0/modules/distribution/src/main/conf`
3. kill the process of #1

The challenge is to get all the Java, Ruby, JavaScript, Kotlin (etc) implementations to record the same servirtium markdown recording. Exactly the same - all chars all lines.

Then there's a simpling script needed such that the same recording could be used for a fresh repeat of #2 to make a local checkout thats identical for all files.

