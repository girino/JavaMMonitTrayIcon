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

import static org.junit.Assert.assertNull;

import java.io.OutputStream;
import java.io.PrintStream;

import org.junit.Test;

/**
 * Tests static helpers on {@link App} that do not require {@link java.awt.SystemTray}.
 */
public class AppCreateImageTest {

	@Test
	public void createImage_missingResourceReturnsNull() {
		PrintStream err = System.err;
		System.setErr(new PrintStream(new OutputStream() {
			@Override
			public void write(int b) {
				// discard expected "Resource not found" line from App.createImage
			}
		}));
		try {
			assertNull(App.createImage("/no-such-resource-in-jar-999.png", "desc"));
		} finally {
			System.setErr(err);
		}
	}
}
