package org.girino.tray.mmonittray;

import java.util.Properties;

public abstract class Callback {
	public abstract void onSave(Properties p);
	public void onCancel() {
		// do nothing
	}
}
