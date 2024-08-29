## Dependencies
- Includes [OBS WebSocket Java](https://github.com/obs-websocket-community-projects/obs-websocket-java), used under [MIT](https://github.com/obs-websocket-community-projects/obs-websocket-java/blob/develop/LICENSE)
- Includes [SQLite JDBC Driver](https://github.com/xerial/sqlite-jdbc), used under [Apache 2.0](https://github.com/xerial/sqlite-jdbc/blob/master/LICENSE)
    - SQLite JDBC Driver also includes software by David Crawshaw under [BSD 2-Clause](https://github.com/xerial/sqlite-jdbc/blob/master/LICENSE.zentus)
    - The NOTICE file is both available in the sources and in the mod itself
- Includes [FFmpeg CLI Wrapper for Java](https://github.com/bramp/ffmpeg-cli-wrapper), used under [BSD 2-Clause "Simplified"](https://github.com/bramp/ffmpeg-cli-wrapper/blob/master/LICENCE)
- Requires [ffmpeg](https://ffmpeg.org) with libx264 to be installed
- Requires [Fabric API](https://modrinth.com/mod/fabric-api)
- Requires [Fabric Loader](https://github.com/FabricMC/fabric-loader) 0.15.11 or greater

## Licensing
...is a mess.
### TLDR
#### Modpacks
You can use this mod in a modpack! Don't re-host the file without permission though. I'd love a mention somewhere for bragging rights.
#### Videos
You can use this mod to make videos! I'd love a mention somewhere in a description or something so I can have bragging rights.
#### Programming
You can use and reference all the code in the GitHub repo under MIT.
### What's actually going on
autocut is licensed under MIT, but the JAR file includes several dependencies.
- OBS WebSocket Java is under MIT.
- SQLite JDBC Driver is dual licensed (GPL and Apache) - I used it under Apache.
  - It includes a NOTICE
    - This is why there's a NOTICE file
    - This is why there's a LICENSE.zentus file (it's mentioned in the NOTICE)
- FFmpeg CLI Wrapper for Java is licensed under BSD 2-Clause "Simplified"
  - This is why there's a LICENSE.bramp file
- And finally, there's my own LICENSE file