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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PairTest {

	@Test
	public void accessors() {
		Pair<String, Integer> p = new Pair<>("k", 7);
		assertEquals("k", p.getKey());
		assertEquals(Integer.valueOf(7), p.getValue());
	}

	@Test
	public void customEquals_sameValues() {
		Pair<String, String> a = new Pair<>("x", "y");
		Pair<String, String> b = new Pair<>("x", "y");
		assertTrue(a.equals(b));
		assertTrue(b.equals(a));
	}

	@Test
	public void customEquals_differentKey() {
		Pair<String, String> a = new Pair<>("x", "y");
		Pair<String, String> b = new Pair<>("z", "y");
		assertFalse(a.equals(b));
	}

	@Test
	public void customEquals_differentValue() {
		Pair<String, String> a = new Pair<>("x", "y");
		Pair<String, String> b = new Pair<>("x", "z");
		assertFalse(a.equals(b));
	}

	@Test
	public void customEquals_null() {
		assertFalse(new Pair<>("a", "b").equals(null));
	}

	@Test
	public void hashCode_consistentWithCustomEquals() {
		Pair<String, String> a = new Pair<>("x", "y");
		Pair<String, String> b = new Pair<>("x", "y");
		assertEquals(a.hashCode(), b.hashCode());
	}

	@Test
	public void toString_nonNull() {
		assertNotNull(new Pair<>(1, 2).toString());
	}
}
