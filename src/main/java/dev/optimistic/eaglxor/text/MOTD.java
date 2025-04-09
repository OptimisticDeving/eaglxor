package dev.optimistic.eaglxor.text;

import com.google.gson.annotations.SerializedName;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Server;
import org.bukkit.util.CachedServerIcon;

import java.util.Collections;
import java.util.List;

public record MOTD(
  @SerializedName("cache") boolean clientShouldCache,
  @SerializedName("motd") List<String> lines,
  @SerializedName("icon") boolean favicon,
  int online,
  int max,
  List<String> players
) {
  public static MOTD create(Server server) {
    final CachedServerIcon serverIcon;
    final var players = server.getOnlinePlayers();

    return new MOTD(
      false,
      // TODO: Will eagler properly handle this?
      Collections.singletonList(LegacyComponentSerializer.legacySection()
        .serialize(server.motd())),
      (serverIcon = server.getServerIcon()) != null && !serverIcon.isEmpty(),
      players.size(),
      server.getMaxPlayers(),
      server.getHideOnlinePlayers() ?
        Collections.emptyList()
        :
        players.stream()
          .map(
            player ->
              player.isAllowingServerListings()
                ?
                player.getPlayerProfile().getName()
                :
                "Anonymous Player"
          )
          .toList()
    );
  }
}
