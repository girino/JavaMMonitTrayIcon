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

import java.util.Properties;

public abstract class Callback {
	public abstract void onSave(Properties p);
	public void onCancel() {
		// do nothing
	}
}
