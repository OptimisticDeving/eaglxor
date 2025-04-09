package dev.optimistic.eaglxor.text;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

public final class TextPacketRegistry {
  public static final TextPacketRegistry INSTANCE =
    new TextPacketRegistry(
      new TextRegistration<>(
        "motd",
        MOTD.class
      )
    );
  private static final Gson GSON = new Gson();

  private final Map<String, Class<?>> typeToClass;
  private final Map<Class<?>, String> classToType;

  private TextPacketRegistry(TextRegistration<?>... registrations) {
    final Map<String, Class<?>> typeToClass = new HashMap<>(registrations.length);
    final Map<Class<?>, String> classToType =
      new IdentityHashMap<>(registrations.length);

    for (final TextRegistration<?> registration : registrations) {
      typeToClass.put(registration.typeName, registration.packetClass);
      classToType.put(registration.packetClass, registration.typeName);
    }

    this.typeToClass = typeToClass;
    this.classToType = classToType;
  }

  public DeserializedPacket deserialize(String json) {
    final var containerObject = GSON.fromJson(json, JsonObject.class);
    final var data = containerObject.remove("data");
    if (data == null)
      throw new DecoderException("Json did not contain any data");
    final var type = containerObject.remove("type");
    if (!(type instanceof final JsonPrimitive typePrimitive)
      || !typePrimitive.isString())
      throw new DecoderException("Type was null or not a string");
    final var typeClass = this.typeToClass.get(type.getAsString());
    if (typeClass == null)
      throw new DecoderException("Tried to deserialize unknown text packet");

    return new DeserializedPacket(
      GSON.fromJson(containerObject, ServerInfoContainer.class),
      GSON.fromJson(data, typeClass)
    );
  }

  public String serialize(ServerInfoContainer serverInfo, Object packet) {
    final var type = this.classToType.get(packet.getClass());
    if (type == null)
      throw new EncoderException("Tried to serialize unknown text packet");
    final var containerElement = GSON.toJsonTree(serverInfo);
    if (!(containerElement instanceof final JsonObject containerObject))
      throw new AssertionError(
        "Serialized server info container not a JsonObject," +
          "this is VERY BAD!"
      );

    containerObject.addProperty("time", System.currentTimeMillis());
    containerObject.addProperty("type", type);

    final var serializedPacket = GSON.toJsonTree(packet);
    containerObject.add("data", serializedPacket);

    return GSON.toJson(containerObject);
  }

  public record DeserializedPacket(
    ServerInfoContainer infoContainer,
    Object packet
  ) {

  }

  private record TextRegistration<T>(String typeName, Class<T> packetClass) {

  }
}
