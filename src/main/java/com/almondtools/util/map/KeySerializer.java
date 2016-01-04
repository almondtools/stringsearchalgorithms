package com.almondtools.util.map;

public interface KeySerializer<T> {

	long[] toLongArray(T object);
}
