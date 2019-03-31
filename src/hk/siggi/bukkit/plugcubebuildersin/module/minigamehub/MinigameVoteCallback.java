package hk.siggi.bukkit.plugcubebuildersin.module.minigamehub;

import org.bukkit.inventory.ItemStack;

public interface MinigameVoteCallback {
	public void voteCompleted(String tag, ItemStack stack);
}
