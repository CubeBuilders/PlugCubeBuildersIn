package hk.siggi.bukkit.plugcubebuildersin.playerstate;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import hk.siggi.bukkit.plugcubebuildersin.util.Util;
import java.io.IOException;
import org.bukkit.Color;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PotionEffectAdapter extends TypeAdapter<PotionEffect> {

	public PotionEffectAdapter() {
	}

	@Override
	public PotionEffect read(JsonReader reader) throws IOException {
		int amplifier = 0;
		int duration = 0;
		PotionEffectType type = null;
		boolean ambient = false;
		boolean particles = false;
		Color color = null;
		reader.beginObject();
		JsonToken peek;
		while ((peek = reader.peek()) != JsonToken.END_OBJECT) {
			if (peek != JsonToken.NAME) {
				reader.skipValue();
				continue;
			}
			String name = reader.nextName();
			peek = reader.peek();
			if (name.equals("amplifier") && peek == JsonToken.NUMBER) {
				amplifier = reader.nextInt();
			} else if (name.equals("duration") && peek == JsonToken.NUMBER) {
				duration = reader.nextInt();
			} else if (name.equals("type") && peek == JsonToken.STRING) {
				try {
					type = PotionEffectType.getByName(reader.nextString());
				} catch (Exception e) {
				}
			} else if (name.equals("ambient") && peek == JsonToken.BOOLEAN) {
				ambient = reader.nextBoolean();
			} else if (name.equals("particles") && peek == JsonToken.BOOLEAN) {
				particles = reader.nextBoolean();
			} else if (name.equals("color") && peek == JsonToken.STRING) {
				color = Util.fromHex(reader.nextString());
			}
		}
		reader.endObject();
		//if (color == null) {
			return new PotionEffect(type, duration, amplifier, ambient, particles);
		//} else { // color was removed
		//	return new PotionEffect(type, duration, amplifier, ambient, particles, color);
		//}
	}

	@Override
	public void write(JsonWriter writer, PotionEffect effect) throws IOException {
		int amplifier = effect.getAmplifier();
		int duration = effect.getDuration();
		PotionEffectType type = effect.getType();
		boolean ambient = effect.isAmbient();
		boolean particles = effect.hasParticles();
		Color color = effect.getColor();
		writer.beginObject();
		writer.name("amplifier").value(amplifier);
		writer.name("duration").value(duration);
		writer.name("type").value(type.getName());
		writer.name("ambient").value(ambient);
		writer.name("particles").value(particles);
		if (color != null) {
			writer.name("color").value(Util.toHex(color));
		}
		writer.endObject();
	}
}
