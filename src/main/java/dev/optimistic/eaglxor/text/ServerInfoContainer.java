package dev.optimistic.eaglxor.text;

import com.google.gson.annotations.SerializedName;
import dev.optimistic.eaglxor.Main;
import org.bukkit.Server;

import java.util.UUID;

public record ServerInfoContainer(
  @SerializedName("name") String serverName,
  @SerializedName("brand") String brandName,
  @SerializedName("vers") String brandVersion,
  @SerializedName("cracked") boolean offline,
  UUID uuid
) {
  public static ServerInfoContainer generate(Server server, UUID id) {
    return new ServerInfoContainer(
      Main.NAME,
      Main.NAME,
      Main.INSTANCE_SUPPLIER.get().getBrandVersion(),
      !server.getOnlineMode(),
      id
    );
  }
}
