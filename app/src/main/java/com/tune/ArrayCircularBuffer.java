package com.tune;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
/**
 * Thread safe fixed size circular buffer implementation. Backed by an array.
 *
 * @author brad
 */
public class ArrayCircularBuffer<T> {
	// internal data storage
	private T[] data;
	// indices for inserting and removing from queue
	private int front = 0;
	private int insertLocation = 0;
	// number of elements in queue
	private int size = 0;
	/**
	 * Creates a circular buffer with the specified size.
	 *
	 * @param bufferSize
	 *      - the maximum size of the buffer
	 */
	public ArrayCircularBuffer(int bufferSize) {
		data = (T[]) new Object[bufferSize];
	}
	/**
	 * Inserts an item at the end of the queue. If the queue is full, the oldest
	 * value will be removed and head of the queue will become the second oldest
	 * value.
	 *
	 * @param item
	 *      - the item to be inserted
	 */
	public synchronized void insert(T item) {
		data[insertLocation] = item;
		insertLocation = (insertLocation + 1) % data.length;
		/**
		 * If the queue is full, this means we just overwrote the front of the
		 * queue. So increment the front location.
		 */
		if (size == data.length) {
			front = (front + 1) % data.length;
		} else {
			size++;
		}
	}

    public synchronized void insertArray(List<T> items) {
        for(int i = 0; i < items.size(); ++i) {
            data[insertLocation] = items.get(i);
            insertLocation = (insertLocation + 1) % data.length;
            /**
             * If the queue is full, this means we just overwrote the front of the
             * queue. So increment the front location.
             */
            if (size == data.length) {
                front = (front + 1) % data.length;
            } else {
                size++;
            }
        }
    }
	/**
	 * Returns the number of elements in the buffer
	 *
	 * @return int - the number of elements inside this buffer
	 */
	public synchronized int size() {
		return size;
	}
	/**
	 * Returns the head element of the queue.
	 *
	 * @return T
	 */
	public synchronized T removeFront() {
		if (size == 0) {
			throw new NoSuchElementException();
		}
		T retValue = data[front];
		front = (front + 1) % data.length;
		size--;
		return retValue;
	}

	public synchronized List<T> removeAll() {
		List<T> ret = new ArrayList<T>();
		while(size > 0) {
			T val = data[front];
			front = (front + 1) % data.length;
			size--;
			ret.add(val);
		}
		return ret;
	}
	/**
	 * Returns the head of the queue but does not remove it.
	 *
	 * @return
	 */
	public synchronized T peekFront() {
		if (size == 0) {
			return null;
		} else {
			return data[front];
		}
	}
	/**
	 * Returns the last element of the queue but does not remove it.
	 *
	 * @return T - the most recently added value
	 */
	public synchronized T peekLast() {
		if (size == 0) {
			return null;
		} else {
			int lastElement = insertLocation - 1;
			if (lastElement < 0) {
				lastElement = data.length - 1;
			}
			return data[lastElement];
		}
	}
}