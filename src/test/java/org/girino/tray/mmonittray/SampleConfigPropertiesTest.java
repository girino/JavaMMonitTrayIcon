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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.Properties;

import org.junit.Test;

/**
 * Validates the bundled sample configuration used on first run.
 */
public class SampleConfigPropertiesTest {

	@Test
	public void sampleConfig_loadsFromClasspath() throws Exception {
		try (InputStream in = App.class.getResourceAsStream("/properties/config.properties-sample")) {
			assertNotNull(in);
			Properties p = new Properties();
			p.load(in);
			assertTrue(p.getProperty("server", "").startsWith("https://"));
			assertNotNull(p.getProperty("path.api"));
			assertNotNull(p.getProperty("auth.type"));
			assertTrue(p.getProperty("query.0.RED", "").length() > 0);
		}
	}
}
