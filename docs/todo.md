# Todo

Parked work. Side issues found mid-task land here rather than derailing the task.

#1 FullHost account — not opened yet. Java 21 is confirmed available there, so nothing
blocks it but the signup.

#2 Deploy the skeleton to FullHost — validates that their build node builds this repo and
runs the jar, while nothing is invested in it.

#4 Menu links go nowhere — every item in the header is `href="#"`. No routes, no pages.

#5 Footer "Last modified" is a placeholder — no source decided for the date.

#6 MCP controller — not started. The Client controller is the only one that exists.

#7 Content from GettingLost — none migrated yet.

#8 Reading the JSON — design started, not built. Settled: the GettingLost per-page JSON
schema is the starting point. Still open: where the files live at runtime, whether they are
loaded at startup or per request, and whether they map to typed records or a generic tree.

#9 Getting content onto the server — the open thread behind #8, and it covers JSON, HTML
and images alike. Pierre wants minimum friction, ideally a remote drive mounted in Finder
for drag and drop. Finder mounts SMB/AFP/NFS/WebDAV read-write and FTP read-only, and has
no SFTP support, so a real mounted volume needs Mountain Duck or Transmit (~$40). The
alternative is no transfer at all: content in the repo, pushed, rebuilt by FullHost.
Question to put to FullHost: which path on the container survives a redeploy.
