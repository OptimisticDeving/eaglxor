# Eaglxor

Small implementation of the Eagler protocol for Paper 1.21.4 servers running
Via*

## Instructions for use

1. Build the plugin with `./gradlew build` or `.\gradlew.bat build`, depending
   on your operating system.
2. Ensure you have installed ViaVersion, ViaBackwards and ViaRewind if you'd
   like 1.8.x client support.
3. Copy the plugin to your server
4. Configure the port the HTTP server binds on (def. `42069`) and the path of
   the websocket (def. `/socket`). It won't accept connections to any other
   path.
5. (Optional) Set up a reverse proxy

## TODO

- [ ] Generify codebase so that we can port it to other platforms (i.e. Fabric &
  standalone)
- [ ] Full legacy client support
- [ ] Integration with Simple Voice Chat/some client mod providing custom
  skins (perhaps with data;image/png;base64, in the textures property) to
  re-implement Eagler functionality
- [ ] Stop depending on ViaVersion/ViaBackwards hacks and use an API if present