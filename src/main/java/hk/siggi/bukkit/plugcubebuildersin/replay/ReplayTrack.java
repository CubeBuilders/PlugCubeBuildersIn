package hk.siggi.bukkit.plugcubebuildersin.replay;

import hk.siggi.bukkit.plugcubebuildersin.replay.action.Action;
import hk.siggi.bukkit.plugcubebuildersin.util.ReadingIterator;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class ReplayTrack {

	private final ActionRecorder recorder;

	private long earliestLoaded = 0L;
	private long latestLoaded = 0L;

	private int end = 0;

	private Element first;
	private Element current;
	private Element last;

	private final boolean readingFromLiveRecording;

	public ReplayTrack(ActionRecorder recorder, long startTime) {
		this.recorder = recorder;
		long[] bounds = recorder.getLogBounds();
		long low = bounds[0];
		long high = bounds[1];
		long logNumber = ActionRecorder.getLogNumber(startTime);
		long firstLogNumber = logNumber;
		Action[] actions = recorder.readLogFile(logNumber);
		while (actions.length == 0) {
			logNumber -= 1;
			if (logNumber < low) {
				{
					logNumber = firstLogNumber;
					while (actions.length == 0) {
						logNumber += 1;
						if (logNumber > high) {
							break;
						}
						actions = recorder.readLogFile(logNumber);
					}
				}
				break;
			}
			actions = recorder.readLogFile(logNumber);
		}
		if (actions.length == 0) {
			throw new RuntimeException("No action to hold on to!");
		}
		ActionReplayImpl.sortActions(actions);
		first = current = last = new Element(actions[0]);
		for (int i = 1; i < actions.length; i++) {
			append(actions[i]);
			if (actions[i].time <= startTime) {
				current = last;
			}
		}
		if (first.action.time > startTime) {
			end = -1;
		} else if (last.action.time <= startTime) {
			end = 1;
		}
		earliestLoaded = first.action.time;
		latestLoaded = last.action.time;
		readingFromLiveRecording = true;
	}

	public ReplayTrack(Action[] actions) {
		if (actions.length == 0) {
			throw new RuntimeException("No action to hold on to!");
		}
		recorder = null;
		Action[] localActions = new Action[actions.length];
		System.arraycopy(actions, 0, localActions, 0, actions.length);
		ActionReplayImpl.sortActions(localActions);
		first = current = last = new Element(actions[0]);
		for (int i = 1; i < actions.length; i++) {
			append(actions[i]);
		}
		end = -1;
		earliestLoaded = first.action.time;
		latestLoaded = last.action.time;
		readingFromLiveRecording = false;
	}

	public Action current() {
		if (end != 0) {
			return null;
		}
		return current.action;
	}

	public Action previous() {
		if (end == 1) {
			end = 0;
			return current.action;
		}
		if (current.prev != null) {
			end = 0;
			current = current.prev;
			return current.action;
		}
		end = -1;
		return null;
	}

	public Action next() {
		if (end == -1) {
			end = 0;
			return current.action;
		}
		if (current.next != null) {
			end = 0;
			current = current.next;
			return current.action;
		}
		end = 1;
		return null;
	}

	public Action peekPrevious() {
		if (end == 1) {
			return current.action;
		}
		if (current.prev != null) {
			return current.prev.action;
		}
		return null;
	}

	public Action peekNext() {
		if (end == -1) {
			return current.action;
		}
		if (current.next != null) {
			return current.next.action;
		}
		return null;
	}

	private class Element {

		Element prev;
		Element next;
		public final Action action;

		public Element(Action action) {
			this.action = action;
		}
	}

	private long lastHighLog = 0L;
	private long lastHighSkipBytes = 0L;

	public boolean loadAndUnload(long currentTime, long minimumLoad, long maximumLoad) {
		if (!readingFromLiveRecording) {
			return false;
		}

		long low, high;
		{
			long[] x = recorder.getLogBounds();
			low = x[0];
			high = x[1];
		}
		// unload anything more than 30 mins in the past or future except if it's the current.
		unloadBefore(currentTime - maximumLoad);
		unloadAfter(currentTime + maximumLoad);

		// if we have less than 5 seconds of recording towards the past or future, load more.
		// may load up to 10 minutes more info.
		long maxLow = currentTime - minimumLoad;
		long minHigh = currentTime + minimumLoad;
		while (maxLow < earliestLoaded) {
			long loadLog = earliestLoaded - 1L;
			long logNumber = ActionRecorder.getLogNumber(loadLog);
			if (logNumber < low) {
				break;
			}
			Action[] read = recorder.readLogFile(logNumber);
			ActionReplayImpl.sortActions(read);
			for (int i = read.length - 1; i >= 0; i--) {
				if (read[i].time < earliestLoaded) {
					prepend(read[i]);
				}
			}
			earliestLoaded = ActionRecorder.getLogTime(logNumber);
			if (logNumber <= low) {
				break;
			}
		}
		boolean didAppend = false;
		while (minHigh > latestLoaded) {
			long loadLog = latestLoaded + 1L;
			long logNumber = ActionRecorder.getLogNumber(loadLog);
			if (logNumber > high) {
				break;
			}
			if (lastHighLog != logNumber) {
				lastHighLog = logNumber;
				lastHighSkipBytes = 0L;
			}
			ActionRecorder.ReadResult readResult = recorder.readLogFileExtended(logNumber, lastHighSkipBytes);
			Action[] read = readResult.actions;
			lastHighSkipBytes = readResult.fileOffset;
			ActionReplayImpl.sortActions(read);
			for (int i = 0; i < read.length; i++) {
				if (read[i].time > latestLoaded) {
					append(read[i]);
					didAppend = true;
				}
			}
			latestLoaded = Math.min(System.currentTimeMillis(), ActionRecorder.getLogTime(logNumber + 1L) - 1L);
			if (logNumber >= high) {
				break;
			}
		}
		return didAppend;
	}

	private void unloadBefore(long time) {
		while (first.action.time < time && removeFirst()) {
		}
		earliestLoaded = Math.max(earliestLoaded, Math.min(time, first.action.time));
	}

	private void unloadAfter(long time) {
		while (last.action.time > time && removeLast()) {
		}
		latestLoaded = Math.min(latestLoaded, Math.max(time, last.action.time));
	}

	private void prepend(Action action) {
		first.prev = new Element(action);
		first.prev.next = first;
		first = first.prev;
	}

	private void append(Action action) {
		last.next = new Element(action);
		last.next.prev = last;
		last = last.next;
	}

	private boolean removeFirst() {
		if (first == current || first == bookmarkedElement) {
			return false;
		}
		first = first.next;
		first.prev = null;
		return true;
	}

	private boolean removeLast() {
		if (last == current || first == bookmarkedElement) {
			return false;
		}
		last = last.prev;
		last.next = null;
		lastHighSkipBytes = 0L;
		return true;
	}

	public Iterator<Action> reverseIterateToCurrent() {
		return new ReadingIterator<Action>() {
			boolean jumpOver = false;
			Element cur = last;

			@Override
			protected Action read() throws NoSuchElementException {
				if (cur == null || (cur == current && (jumpOver || end != -1))) {
					throw new NoSuchElementException();
				}
				if (end == -1) {
					jumpOver = true;
				}
				Action val = cur.action;
				cur = cur.prev;
				return val;
			}
		};
	}

	private Element bookmarkedElement = null;
	private int bookmarkedEnd = 0;

	public void bookmark() {
		bookmarkedElement = current;
		bookmarkedEnd = end;
	}

	public void unbookmark() {
		bookmarkedElement = null;
		bookmarkedEnd = 0;
	}

	public void returnToBookmark() {
		if (bookmarkedElement != null) {
			current = bookmarkedElement;
			end = bookmarkedEnd;
			bookmarkedElement = null;
			bookmarkedEnd = 0;
		}
	}
}
