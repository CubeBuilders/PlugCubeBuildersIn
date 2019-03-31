package hk.siggi.bukkit.plugcubebuildersin.nms.v1_13_R1;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.server.v1_13_R1.ArgumentChat;
import net.minecraft.server.v1_13_R1.CompletionProviders;

public class BrigadierUtil extends hk.siggi.bukkit.plugcubebuildersin.nms.BrigadierUtil {

	@Override
	public ArgumentType argumentTypeChat() {
		return ArgumentChat.a();
	}

	@Override
	public SuggestionProvider suggestionProviderAskServer() {
		return CompletionProviders.a;
	}
}
