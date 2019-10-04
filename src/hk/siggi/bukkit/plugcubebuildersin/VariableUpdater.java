package hk.siggi.bukkit.plugcubebuildersin;

import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

public class VariableUpdater {

	public final Block block;
	public final Chunk chunk;
	public final int lineNumber;
	public final String variable;

	public VariableUpdater(Block block, int lineNumber, String variable) {
		this.block = block;
		this.lineNumber = lineNumber;
		this.variable = variable;
		this.chunk = block.getChunk();
	}

	public boolean usesChunk(Chunk chunk) {
		return chunk.equals(chunk);
	}

	public void update(String variable, String value) {
		if (!chunk.isLoaded()) {
			return;
		}
		try {
			if (this.variable.equals(variable)) {
				Sign sign = (Sign) block.getState();
				sign.setLine(lineNumber, value);
				sign.update();
			}
		} catch (Exception e) {
		}
	}
}
