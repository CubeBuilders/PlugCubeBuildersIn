package hk.siggi.bukkit.plugcubebuildersin.replay.action;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.UUID;
import org.bukkit.Material;

public abstract class Action {

	private static final Gson gson;

	static {
		final TypeAdapter<ActionBlock> actionBlock = new TypeAdapter<ActionBlock>() {
			@Override
			public ActionBlock read(JsonReader reader) throws IOException {
				String world = null;
				int x = 0;
				int y = 0;
				int z = 0;
				reader.beginObject();
				JsonToken peek;
				while ((peek = reader.peek()) != JsonToken.END_OBJECT) {
					if (peek != JsonToken.NAME) {
						reader.skipValue();
						continue;
					}
					String key = reader.nextName();
					peek = reader.peek();
					if (key.equals("world") && peek == JsonToken.STRING) {
						world = reader.nextString();
					} else if (key.equals("x") && peek == JsonToken.NUMBER) {
						x = reader.nextInt();
					} else if (key.equals("y") && peek == JsonToken.NUMBER) {
						y = reader.nextInt();
					} else if (key.equals("z") && peek == JsonToken.NUMBER) {
						z = reader.nextInt();
					}
				}
				reader.endObject();
				return new ActionBlock(world, x, y, z);
			}

			@Override
			public void write(JsonWriter writer, ActionBlock t) throws IOException {
				writer.beginObject();
				writer.name("world").value(t.world);
				writer.name("x").value(t.x);
				writer.name("y").value(t.y);
				writer.name("z").value(t.z);
				writer.endObject();
			}
		};
		final TypeAdapter<ActionChunk> actionChunk = new TypeAdapter<ActionChunk>() {
			@Override
			public ActionChunk read(JsonReader reader) throws IOException {
				String world = null;
				int x = 0;
				int z = 0;
				reader.beginObject();
				JsonToken peek;
				while ((peek = reader.peek()) != JsonToken.END_OBJECT) {
					if (peek != JsonToken.NAME) {
						reader.skipValue();
						continue;
					}
					String key = reader.nextName();
					peek = reader.peek();
					if (key.equals("world") && peek == JsonToken.STRING) {
						world = reader.nextString();
					} else if (key.equals("x") && peek == JsonToken.NUMBER) {
						x = reader.nextInt();
					} else if (key.equals("z") && peek == JsonToken.NUMBER) {
						z = reader.nextInt();
					}
				}
				reader.endObject();
				return new ActionChunk(world, x, z);
			}

			@Override
			public void write(JsonWriter writer, ActionChunk t) throws IOException {
				writer.beginObject();
				writer.name("world").value(t.world);
				writer.name("x").value(t.x);
				writer.name("z").value(t.z);
				writer.endObject();
			}
		};
		final TypeAdapter<ActionLocation> actionLocation = new TypeAdapter<ActionLocation>() {
			@Override
			public ActionLocation read(JsonReader reader) throws IOException {
				String world = null;
				double x = 0.0;
				double y = 0.0;
				double z = 0.0;
				float pitch = 0.0f;
				float yaw = 0.0f;
				reader.beginObject();
				JsonToken peek;
				while ((peek = reader.peek()) != JsonToken.END_OBJECT) {
					if (peek != JsonToken.NAME) {
						reader.skipValue();
						continue;
					}
					String key = reader.nextName();
					peek = reader.peek();
					if (key.equals("world") && peek == JsonToken.STRING) {
						world = reader.nextString();
					} else if (key.equals("x") && peek == JsonToken.NUMBER) {
						x = reader.nextDouble();
					} else if (key.equals("y") && peek == JsonToken.NUMBER) {
						y = reader.nextDouble();
					} else if (key.equals("z") && peek == JsonToken.NUMBER) {
						z = reader.nextDouble();
					} else if (key.equals("pitch") && peek == JsonToken.NUMBER) {
						pitch = (float) reader.nextDouble();
					} else if (key.equals("yaw") && peek == JsonToken.NUMBER) {
						yaw = (float) reader.nextDouble();
					}
				}
				reader.endObject();
				return new ActionLocation(world, x, y, z, pitch, yaw);
			}

			@Override
			public void write(JsonWriter writer, ActionLocation t) throws IOException {
				writer.beginObject();
				writer.name("world").value(t.world);
				writer.name("x").value(t.x);
				writer.name("y").value(t.y);
				writer.name("z").value(t.z);
				writer.name("pitch").value(t.pitch);
				writer.name("yaw").value(t.yaw);
				writer.endObject();
			}
		};
		final TypeAdapter<MoveAction> moveAction = new TypeAdapter<MoveAction>() {
			@Override
			public MoveAction read(JsonReader reader) throws IOException {
				long time = 0L;
				UUID player = null;
				ActionLocation from = null;
				ActionLocation to = null;
				boolean teleport = false;
				reader.beginObject();
				JsonToken peek;
				while ((peek = reader.peek()) != JsonToken.END_OBJECT) {
					if (peek != JsonToken.NAME) {
						reader.skipValue();
						continue;
					}
					String key = reader.nextName();
					peek = reader.peek();
					if (key.equals("time") && peek == JsonToken.NUMBER) {
						time = reader.nextLong();
					} else if (key.equals("player") && peek == JsonToken.STRING) {
						player = uuidFromString(reader.nextString());
					} else if (key.equals("from") && peek == JsonToken.BEGIN_OBJECT) {
						from = actionLocation.read(reader);
					} else if (key.equals("to") && peek == JsonToken.BEGIN_OBJECT) {
						to = actionLocation.read(reader);
					} else if (key.equals("teleport") && peek == JsonToken.BOOLEAN) {
						teleport = reader.nextBoolean();
					}
				}
				reader.endObject();
				return new MoveAction(time, player, from, to, teleport);
			}

			@Override
			public void write(JsonWriter writer, MoveAction t) throws IOException {
				writer.beginObject();
				writer.name("type").value("move");
				writer.name("time").value(t.time);
				writer.name("player").value(uuidToString(t.player));
				writer.name("from");
				if (t.from == null) {
					writer.nullValue();
				} else {
					actionLocation.write(writer, t.from);
				}
				writer.name("to");
				if (t.to == null) {
					writer.nullValue();
				} else {
					actionLocation.write(writer, t.to);
				}
				if (t.teleport) {
					writer.name("teleport").value(t.teleport);
				}
				writer.endObject();
			}
		};
		final TypeAdapter<ArmswingAction> armswingAction = new TypeAdapter<ArmswingAction>() {
			@Override
			public ArmswingAction read(JsonReader reader) throws IOException {
				long time = 0L;
				UUID player = null;
				reader.beginObject();
				JsonToken peek;
				while ((peek = reader.peek()) != JsonToken.END_OBJECT) {
					if (peek != JsonToken.NAME) {
						reader.skipValue();
						continue;
					}
					String key = reader.nextName();
					peek = reader.peek();
					if (key.equals("time") && peek == JsonToken.NUMBER) {
						time = reader.nextLong();
					} else if (key.equals("player") && peek == JsonToken.STRING) {
						player = uuidFromString(reader.nextString());
					}
				}
				reader.endObject();
				return new ArmswingAction(time, player);
			}

			@Override
			public void write(JsonWriter writer, ArmswingAction t) throws IOException {
				writer.beginObject();
				writer.name("type").value("armswing");
				writer.name("time").value(t.time);
				writer.name("player").value(uuidToString(t.player));
				writer.endObject();
			}
		};
		final TypeAdapter<BlockChangeAction> blockChangeAction = new TypeAdapter<BlockChangeAction>() {
			@Override
			public BlockChangeAction read(JsonReader reader) throws IOException {
				long time = 0L;
				UUID player = null;
				ActionBlock block = null;
				Material from = null;
				byte fromData = (byte) 0;
				Material to = null;
				byte toData = (byte) 0;
				reader.beginObject();
				JsonToken peek;
				while ((peek = reader.peek()) != JsonToken.END_OBJECT) {
					if (peek != JsonToken.NAME) {
						reader.skipValue();
						continue;
					}
					String key = reader.nextName();
					peek = reader.peek();
					if (key.equals("time") && peek == JsonToken.NUMBER) {
						time = reader.nextLong();
					} else if (key.equals("player") && peek == JsonToken.STRING) {
						player = uuidFromString(reader.nextString());
					} else if (key.equals("block") && peek == JsonToken.BEGIN_OBJECT) {
						block = actionBlock.read(reader);
					} else if (key.equals("from") && peek == JsonToken.STRING) {
						try {
							from = Material.valueOf(reader.nextString());
						} catch (Exception e) {
						}
					} else if (key.equals("fromdata") && peek == JsonToken.NUMBER) {
						fromData = (byte) reader.nextInt();
					} else if (key.equals("to") && peek == JsonToken.STRING) {
						try {
							to = Material.valueOf(reader.nextString());
						} catch (Exception e) {
						}
					} else if (key.equals("todata") && peek == JsonToken.NUMBER) {
						toData = (byte) reader.nextInt();
					}
				}
				reader.endObject();
				return new BlockChangeAction(time, player, block, from, fromData, to, toData);
			}

			@Override
			public void write(JsonWriter writer, BlockChangeAction t) throws IOException {
				writer.beginObject();
				writer.name("type").value("blockchange");
				writer.name("time").value(t.time);
				writer.name("player").value(uuidToString(t.player));
				writer.name("block");
				if (t.block == null) {
					writer.nullValue();
				} else {
					actionBlock.write(writer, t.block);
				}
				writer.name("from").value(t.from == null ? null : t.from.toString());
				if (t.fromData != (byte) 0) {
					writer.name("fromdata").value(t.fromData);
				}
				writer.name("to").value(t.to == null ? null : t.to.toString());
				if (t.toData != (byte) 0) {
					writer.name("todata").value(t.toData);
				}
				writer.endObject();
			}
		};
		final TypeAdapter<ChatAction> chatAction = new TypeAdapter<ChatAction>() {
			@Override
			public ChatAction read(JsonReader reader) throws IOException {
				long time = 0L;
				UUID player = null;
				String message = null;
				reader.beginObject();
				JsonToken peek;
				while ((peek = reader.peek()) != JsonToken.END_OBJECT) {
					if (peek != JsonToken.NAME) {
						reader.skipValue();
						continue;
					}
					String key = reader.nextName();
					peek = reader.peek();
					if (key.equals("time") && peek == JsonToken.NUMBER) {
						time = reader.nextLong();
					} else if (key.equals("player") && peek == JsonToken.STRING) {
						player = uuidFromString(reader.nextString());
					} else if (key.equals("message") && peek == JsonToken.STRING) {
						message = reader.nextString();
					}
				}
				reader.endObject();
				return new ChatAction(time, player, message);
			}

			@Override
			public void write(JsonWriter writer, ChatAction t) throws IOException {
				writer.beginObject();
				writer.name("type").value("chat");
				writer.name("time").value(t.time);
				writer.name("player").value(uuidToString(t.player));
				writer.name("message").value(t.message);
				writer.endObject();
			}
		};
		final TypeAdapter<LoginAction> loginAction = new TypeAdapter<LoginAction>() {
			@Override
			public LoginAction read(JsonReader reader) throws IOException {
				long time = 0L;
				UUID player = null;
				ActionLocation location = null;
				reader.beginObject();
				JsonToken peek;
				while ((peek = reader.peek()) != JsonToken.END_OBJECT) {
					if (peek != JsonToken.NAME) {
						reader.skipValue();
						continue;
					}
					String key = reader.nextName();
					peek = reader.peek();
					if (key.equals("time") && peek == JsonToken.NUMBER) {
						time = reader.nextLong();
					} else if (key.equals("player") && peek == JsonToken.STRING) {
						player = uuidFromString(reader.nextString());
					} else if (key.equals("location") && peek == JsonToken.BEGIN_OBJECT) {
						location = actionLocation.read(reader);
					}
				}
				reader.endObject();
				return new LoginAction(time, player, location);
			}

			@Override
			public void write(JsonWriter writer, LoginAction t) throws IOException {
				writer.beginObject();
				writer.name("type").value("login");
				writer.name("time").value(t.time);
				writer.name("player").value(uuidToString(t.player));
				writer.name("location");
				if (t.location == null) {
					writer.nullValue();
				} else {
					actionLocation.write(writer, t.location);
				}
				writer.endObject();
			}
		};
		final TypeAdapter<LogoutAction> logoutAction = new TypeAdapter<LogoutAction>() {
			@Override
			public LogoutAction read(JsonReader reader) throws IOException {
				long time = 0L;
				UUID player = null;
				ActionLocation location = null;
				reader.beginObject();
				JsonToken peek;
				while ((peek = reader.peek()) != JsonToken.END_OBJECT) {
					if (peek != JsonToken.NAME) {
						reader.skipValue();
						continue;
					}
					String key = reader.nextName();
					peek = reader.peek();
					if (key.equals("time") && peek == JsonToken.NUMBER) {
						time = reader.nextLong();
					} else if (key.equals("player") && peek == JsonToken.STRING) {
						player = uuidFromString(reader.nextString());
					} else if (key.equals("location") && peek == JsonToken.BEGIN_OBJECT) {
						location = actionLocation.read(reader);
					}
				}
				reader.endObject();
				return new LogoutAction(time, player, location);
			}

			@Override
			public void write(JsonWriter writer, LogoutAction t) throws IOException {
				writer.beginObject();
				writer.name("type").value("logout");
				writer.name("time").value(t.time);
				writer.name("player").value(uuidToString(t.player));
				writer.name("location");
				if (t.location == null) {
					writer.nullValue();
				} else {
					actionLocation.write(writer, t.location);
				}
				writer.endObject();
			}
		};
		final TypeAdapter<Action> action = new TypeAdapter<Action>() {
			@Override
			public Action read(JsonReader reader) throws IOException {
				com.google.gson.JsonParser parser = new JsonParser();
				JsonObject obj = parser.parse(reader).getAsJsonObject();
				JsonElement typeElement = obj.get("type");
				String type = typeElement.getAsString();
				switch (type) {
					case "move":
						return moveAction.fromJsonTree(obj);
					case "armswing":
						return armswingAction.fromJsonTree(obj);
					case "blockchange":
						return blockChangeAction.fromJsonTree(obj);
					case "chat":
						return chatAction.fromJsonTree(obj);
					case "login":
						return loginAction.fromJsonTree(obj);
					case "logout":
						return logoutAction.fromJsonTree(obj);
				}
				return null;
			}

			@Override
			public void write(JsonWriter writer, Action t) throws IOException {
				if (t == null) {
					writer.nullValue();
				} else if (t instanceof MoveAction) {
					moveAction.write(writer, (MoveAction) t);
				} else if (t instanceof ArmswingAction) {
					armswingAction.write(writer, (ArmswingAction) t);
				} else if (t instanceof BlockChangeAction) {
					blockChangeAction.write(writer, (BlockChangeAction) t);
				} else if (t instanceof ChatAction) {
					chatAction.write(writer, (ChatAction) t);
				} else if (t instanceof LoginAction) {
					loginAction.write(writer, (LoginAction) t);
				} else if (t instanceof LogoutAction) {
					logoutAction.write(writer, (LogoutAction) t);
				} else {
					writer.beginObject();
					writer.name("type").value("unknown");
					writer.name("time").value(t.time);
					writer.name("player").value(uuidToString(t.player));
					writer.endObject();
				}
			}
		};
		gson = new GsonBuilder()
				.registerTypeAdapter(ActionBlock.class, actionBlock)
				.registerTypeAdapter(ActionChunk.class, actionChunk)
				.registerTypeAdapter(ActionLocation.class, actionLocation)
				.registerTypeAdapter(MoveAction.class, moveAction)
				.registerTypeAdapter(ArmswingAction.class, armswingAction)
				.registerTypeAdapter(BlockChangeAction.class, blockChangeAction)
				.registerTypeAdapter(ChatAction.class, chatAction)
				.registerTypeAdapter(LoginAction.class, loginAction)
				.registerTypeAdapter(LogoutAction.class, logoutAction)
				.registerTypeAdapter(Action.class, action)
				.create();
	}

	public static Gson getGson() {
		return gson;
	}
	public final long time;
	public final UUID player;

	Action(long time, UUID player) {
		this.time = time;
		this.player = player;
	}

	public String toJson() {
		return gson.toJson(this);
	}

	public static Action fromJson(String json) {
		return gson.fromJson(json, Action.class);
	}

	private static String uuidToString(UUID uuid) {
		if (uuid == null) {
			return null;
		}
		return uuid.toString().replace("-", "");
	}

	private static UUID uuidFromString(String uuid) {
		if (uuid == null) {
			return null;
		}
		return UUID.fromString(
				uuid
				.replace("-", "")
				.replaceAll(
						"([0-9A-Fa-f]{8})([0-9A-Fa-f]{4})([0-9A-Fa-f]{4})([0-9A-Fa-f]{4})([0-9A-Fa-f]{12})",
						"$1-$2-$3-$4-$5"
				)
		);
	}

}
