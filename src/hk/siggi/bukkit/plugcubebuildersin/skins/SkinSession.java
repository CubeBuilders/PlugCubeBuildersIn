package hk.siggi.bukkit.plugcubebuildersin.skins;

import java.lang.ref.WeakReference;
import org.bukkit.entity.Player;

public class SkinSession {
	private final SkinServerHandler handler;
	private final WeakReference<Player> p;
	public SkinSession(SkinServerHandler handler, Player p) {
		this.handler=handler;
		this.p=new WeakReference<>(p);
	}
	private Player getPlayer(){return p.get();}
}
