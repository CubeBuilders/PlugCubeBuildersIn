package hk.siggi.bukkit.plugcubebuildersin;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class NicknameCache {

	private final HashMap<UUID, String> nicknamesByUUID = new HashMap<>();
	private final HashMap<String, UUID> uuidByNicknames = new HashMap<>();

	public void setNicknameCache(UUID player, String nickname) {
		String get = nicknamesByUUID.get(player);
		if (get != null) {
			uuidByNicknames.remove(get.toLowerCase());
		}
		if (nickname == null) {
			nicknamesByUUID.remove(player);
		} else {
			nicknamesByUUID.put(player, nickname);
			uuidByNicknames.put(nickname.toLowerCase(), player);
		}
	}

	public String getNickname(UUID user) {
		return nicknamesByUUID.get(user);
	}

	public UUID getUserByNickname(String nickname) {
		return uuidByNicknames.get(nickname.toLowerCase());
	}

	public boolean isNicknameUsed(String nickname) {
		return nicknamesByUUID.containsValue(nickname);
	}

	public List<String> getUsersWithNameStartingWith(String nickname) {
		return getUsersWithNameStartingWith(nickname, 0);
	}

	public List<String> getUsersWithNameStartingWith(String nickname, int limit) {
		List<String> nicks = new LinkedList<>();
		nickname = nickname.toLowerCase();
		for (String nick : nicknamesByUUID.values()) {
			if (nick.toLowerCase().startsWith(nickname)) {
				nicks.add(nick);
				if (limit > 0 && nicks.size() >= limit) {
					break;
				}
			}
		}
		return nicks;
	}
}
