# Local content directory

Static images and audio for the app are served from **this** folder by the Ktor server
(`StorageService` + `staticFiles` in `Application.kt`), replacing the old `ik.imagekit.io` host.

A file placed at `content/<path>` is reachable at:

```
<CONTENT_BASE_URL>/content/<path>
```

The `DatabaseSeeder` stores absolute URLs built from the **same relative paths** that were
previously used on imagekit — so mirror that exact layout here. For example, a seed entry
`content("words/fruits/apple.png")` expects the file:

```
content/words/fruits/apple.png
```

and an audio entry `content("audio/fruits/apple.mp3")` expects:

```
content/audio/fruits/apple.mp3
```

Category icons live at the root (e.g. `content/Fruits.png`).

## Configuration (env vars)

- `CONTENT_BASE_URL` — public base URL of this server as seen by the client. Defaults to
  `http://10.0.2.2:8080` (Android emulator). Use:
  - iOS simulator / desktop: `http://localhost:8080`
  - real device: `http://<your-machine-LAN-ip>:8080`
  - production: `https://your-domain`
- `CONTENT_DIR` — filesystem directory to serve from. Defaults to `content` (relative to the
  server's working directory).

> After changing `CONTENT_BASE_URL`, re-run the seeder so the stored URLs use the new host.
