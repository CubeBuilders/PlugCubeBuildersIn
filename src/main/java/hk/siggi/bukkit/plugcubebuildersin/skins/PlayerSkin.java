package hk.siggi.bukkit.plugcubebuildersin.skins;

public final class PlayerSkin {
	public final String skin;
	public final String signature;
	public boolean hitDelete = false;
	public boolean move = false;
	public PlayerSkin(String skin, String signature) {
		this.skin = skin;
		this.signature = signature;
	}
}
