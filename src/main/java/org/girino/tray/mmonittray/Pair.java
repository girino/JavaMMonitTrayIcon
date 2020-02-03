/*******************************************************************************
 * Copyright (c) 2019 by Girino Vey.
 * 
 * Permission to use this software, modify and distribute it, or parts of it, is 
 * granted to everyone who wishes provided that the above copyright notice 
 * is kept or the conditions of the full version of this license are met.
 * 
 * See Full license at: https://girino.org/license/
 ******************************************************************************/
package org.girino.tray.mmonittray;

import java.util.Collections;

public class Pair<K,V> {
	
	K key;
	V value;
	
	public Pair(K key, V value) {
		this.key = key;
		this.value = value;
	}

	public boolean equals(Pair<K,V> p) {
		if (p == null) return false;
		return this.key.equals(p.key) && this.value.equals(p.value);
	}

	public K getKey() {
		return key;
	}

	public V getValue() {
		return value;
	}

	public int hashCode() {
		return Collections.singletonMap(key, value).hashCode();
	}

	public String toString() {
		return Collections.singletonMap(key, value).toString();
	}

}
