package hk.siggi.bukkit.plugcubebuildersin.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class ReadingIterator<O> implements Iterator<O> {

	private boolean hasNext = false;
	private boolean needToCalc = true;
	private O next;

	/**
	 * Override this method and return the next element (which may be null) or
	 * throw a NoSuchElementException if there are no more elements.
	 *
	 * @return the next element
	 * @throws NoSuchElementException if there are no more elements.
	 */
	protected abstract O read() throws NoSuchElementException;

	private void calc() {
		if (!needToCalc) {
			return;
		}
		needToCalc = false;
		try {
			hasNext = true;
			next = read();
		} catch (NoSuchElementException e) {
			hasNext = false;
			next = null;
		}
	}

	@Override
	public final boolean hasNext() {
		calc();
		return hasNext;
	}

	@Override
	public final O next() {
		calc();
		if (hasNext) {
			needToCalc = true;
			O o = next;
			next = null;
			return o;
		} else {
			throw new NoSuchElementException();
		}
	}
}
