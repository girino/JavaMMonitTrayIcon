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
import static org.junit.Assert.assertNull;

import java.awt.GraphicsEnvironment;
import java.util.Properties;

import javax.swing.SwingUtilities;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests {@link ConfigurationFrame} property import/export without showing the window.
 */
public class ConfigurationFramePropertiesTest {

	@Before
	public void skipWhenHeadless() {
		Assume.assumeFalse(GraphicsEnvironment.isHeadless());
	}

	@Test
	public void toProperties_fromProperties_roundTrip_basicAuth() throws Exception {
		Properties[] holder = new Properties[1];
		Exception[] error = new Exception[1];
		SwingUtilities.invokeAndWait(() -> {
			try {
				Properties in = new Properties();
				in.setProperty("server", "http://example.test:8443");
				in.setProperty("auth.type", "BASIC");
				in.setProperty("path.init", "/index.csp");
				in.setProperty("path.login", "/login");
				in.setProperty("path.logout", "/logout.csp");
				in.setProperty("path.api", "/status/hosts/summary");
				in.setProperty("query.1.GREEN", ".ok");
				in.setProperty("query.0.RED", ".fail");
				in.setProperty("auth.form.z_username", "u");
				ConfigurationFrame f = new ConfigurationFrame();
				f.fromProperties(in);
				holder[0] = f.toProperties();
			} catch (Exception e) {
				error[0] = e;
			}
		});
		if (error[0] != null) {
			throw error[0];
		}
		Properties out = holder[0];
		assertEquals("http://example.test:8443", out.getProperty("server"));
		assertEquals("BASIC", out.getProperty("auth.type"));
		assertEquals("/status/hosts/summary", out.getProperty("path.api"));
		assertEquals("/index.csp", out.getProperty("path.init"));
		assertEquals(".fail", out.getProperty("query.0.RED"));
		assertEquals(".ok", out.getProperty("query.1.GREEN"));
		assertEquals("u", out.getProperty("auth.form.z_username"));
	}

	@Test
	public void toProperties_formAuthSelected() throws Exception {
		Properties[] holder = new Properties[1];
		SwingUtilities.invokeAndWait(() -> {
			Properties in = new Properties();
			in.setProperty("server", "http://x");
			in.setProperty("auth.type", "FORM");
			in.setProperty("path.init", "/i");
			in.setProperty("path.login", "/l");
			in.setProperty("path.logout", "/o");
			in.setProperty("path.api", "/a");
			ConfigurationFrame f = new ConfigurationFrame();
			f.fromProperties(in);
			holder[0] = f.toProperties();
		});
		assertEquals("FORM", holder[0].getProperty("auth.type"));
	}

	@Test
	public void toProperties_skipsEmptyTableRows() throws Exception {
		Properties[] holder = new Properties[1];
		SwingUtilities.invokeAndWait(() -> {
			ConfigurationFrame f = new ConfigurationFrame();
			holder[0] = f.toProperties();
		});
		assertNull(holder[0].getProperty("query.0.RED"));
	}
}
