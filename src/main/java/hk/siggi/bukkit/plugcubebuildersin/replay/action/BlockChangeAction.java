package hk.siggi.bukkit.plugcubebuildersin.replay.action;

import java.util.Objects;
import java.util.UUID;
import org.bukkit.Material;

public final class BlockChangeAction extends Action {

	public final ActionBlock block;
	public final Material from;
	public final byte fromData;
	public final Material to;
	public final byte toData;

	public BlockChangeAction(long time, UUID player, ActionBlock block, Material from, byte fromData, Material to, byte toData) {
		super(time, player);
		this.block = block;
		this.from = from;
		this.fromData = fromData;
		this.to = to;
		this.toData = toData;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof BlockChangeAction) {
			return equals((BlockChangeAction) other);
		} else {
			return false;
		}
	}

	public boolean equals(BlockChangeAction other) {
		if (other == null) {
			return false;
		}
		return (block == null ? other.block == null : block.equals(other.block))
				&& (from == null ? other.from == null : from.equals(other.from))
				&& (to == null ? other.to == null : to.equals(other.to))
				&& time == other.time
				&& (player == null ? other.player == null : player.equals(other.player));
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 47 * hash + Objects.hashCode(this.block);
		hash = 47 * hash + Objects.hashCode(this.from);
		hash = 47 * hash + Objects.hashCode(this.to);
		hash = 47 * hash + (int) (this.time ^ (this.time >>> 32));
		hash = 47 * hash + Objects.hashCode(this.player);
		return hash;
	}

}
