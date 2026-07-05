# Codex Remote Setup

Use this when you want to control Codex from a phone, similar to remote control flows in other coding agents.

## Recommended setup: Codex App plus ChatGPT mobile

Remote control is configured from the Codex desktop app, not from the Codex CLI or IDE extension.

1. Install and open the Codex App on this Mac.
2. Sign in with the same ChatGPT account and workspace you use on your phone.
3. Select this project folder:

   ```text
   /Users/swapnilagarwal/IdeaProjects/TestingTesting
   ```

4. In the Codex App sidebar, choose **Set up Codex mobile**.
5. Scan the QR code with your phone.
6. Finish the flow in the ChatGPT mobile app.
7. Open Codex in ChatGPT mobile and choose the connected Mac/host.

## Important constraints

- The Mac must stay awake, online, and signed in.
- The phone controls the host; commands still run on the Mac.
- Files, credentials, plugins, MCP servers, browser setup, sandboxing, and approvals come from the Mac.
- If the Mac sleeps, loses network access, or closes Codex, remote access stops until it is available again.
- Mobile setup cannot be started from the Codex CLI.
- A managed ChatGPT workspace may need admin permission for remote control.

## SSH host option

If the project lives on a remote machine, add the SSH host in the Codex App instead. The phone still connects to the Codex App host, and Codex works against the remote filesystem and shell through SSH.

Basic SSH-host requirements:

1. Add the remote host to `~/.ssh/config`.
2. Confirm `ssh <host-alias>` works from the machine running the Codex App.
3. Install and authenticate Codex on the remote host.
4. In Codex App, open **Settings > Connections**, enable the SSH host, and choose the remote project folder.

Source: OpenAI Codex manual, Remote connections and Codex app sections.
