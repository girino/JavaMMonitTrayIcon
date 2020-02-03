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

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.Timer;

import org.apache.http.client.ClientProtocolException;

import net.harawata.appdirs.AppDirs;
import net.harawata.appdirs.AppDirsFactory;

/**
 * Hello world!
 *
 */
public class App 
{
	
	TrayIcon trayIcon;
	Timer timer;
	MMonitConsumer consumer;
	final SystemTray tray = SystemTray.getSystemTray();
	List<Pair<Color, String>> queries;
	Properties properties;

	private static Map<Color, Image> imageCache = new HashMap<Color, Image>();  
	static Image makeImage(Color status) {
		// gets tray icon preffered size
		Dimension d = SystemTray.getSystemTray().getTrayIconSize();
		if (!imageCache.containsKey(status)) {
	        Image image = new BufferedImage(d.height, d.width, BufferedImage.TYPE_INT_ARGB);
	        Graphics g = image.getGraphics(); 
	        g.setColor(status);
	        g.fillOval(0, 0, d.height, d.width);
	        
	        imageCache.put(status, image);
		}
		return imageCache.get(status);
	}
	
	public App() throws IOException, URISyntaxException {
		
		loadPropertiesFromFile();
		
		updateFromProperties();

		createTray();
		start();
	}

	AppDirs appDirs = AppDirsFactory.getInstance();
	private boolean propertiesLoaded;
	
	private void loadPropertiesFromFile() throws IOException {
		properties = new Properties();
		
		// check if there is a local file
		File file = getPropertiesFileLocation();
		
		if (file.exists()) {
			properties.load(new FileReader(file));
			propertiesLoaded = true;
		} else {
			properties.load(App.class.getResourceAsStream("/properties/config.properties-sample"));
			openSettings();
			propertiesLoaded = false;
		}
	}

	private File getPropertiesFileLocation() {
		String path = appDirs.getUserConfigDir(null, null, "JavaMMonitTrayIcon");
		String fileName = path + File.separator + "config.properties";
		File file = new File(fileName);
		return file;
	}

	private void updateFromProperties() {
		String server = properties.getProperty("server", "https://mmonit.example.com");
		
		// List of pages
		Map<String,String> pageMap = toMap(getPropertiesAsList(properties, "path."));
		
		// JQ queries
		queries = getPropertiesAsList(properties, "query.").stream()
				.sorted((a,b) -> a.getKey().compareTo(b.getKey()))
				.map((q) -> new Pair<Color, String>(getColorByName(q.getKey(), true), q.getValue()))
				.collect(Collectors.toList());
		
		List<Pair<String, String>> authStrings = new ArrayList<Pair<String,String>>();
		List<String> requiredPages = Arrays.asList("api");
		String authType = properties.getProperty("auth.type", "BASIC"); 
		if (authType.equalsIgnoreCase("FORM")) {
			authStrings = getPropertiesAsList(properties, "auth.form.");
			requiredPages = Arrays.asList("login", "logout", "init", "api");
		}
		
		if (!pageMap.keySet().containsAll(requiredPages)) {
			throw new RuntimeException("Missing requeired pages.");
		}
		
		consumer = new MMonitConsumer(server, authType, authStrings, pageMap);
	}

	private Color getColorByName(String colorName, boolean removeNumberPrefix) {
		
		if (removeNumberPrefix) {
			colorName = colorName.replaceFirst("^\\d+\\.", "");
		}
		
		try {
			if (colorName.startsWith("#")) {
				return Color.decode(colorName);
			} else {
				return (Color) Color.class.getDeclaredField(colorName).get(null);
			}
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException  e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private <A, B> Map<A, B> toMap(List<Pair<A, B>> pages) {
		return pages.stream().collect(Collectors.toMap(Pair<A, B>::getKey, Pair<A, B>::getValue));
	}

	private List<Pair<String, String>> getPropertiesAsList(Properties p, String prefix) {
		
		return p.keySet().stream()
				.filter((key) -> ((String)key).startsWith(prefix))
				.map( (key) -> new Pair<String, String>( ((String)key).substring(prefix.length()), p.getProperty((String)key)) )
				.collect(Collectors.toList());
	}
	
    //Obtain the image URL
    protected static Image createImage(String path, String description) {
        URL imageURL = App.class.getResource(path);
         
        if (imageURL == null) {
            System.err.println("Resource not found: " + path);
            return null;
        } else {
            return (new ImageIcon(imageURL, description)).getImage();
        }
    }
	
	public void createTray() {
		//Check the SystemTray is supported
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
            return;
        }
        final PopupMenu popup = new PopupMenu();
        trayIcon = new TrayIcon(makeImage(Color.BLUE));
       
        // Create a pop-up menu components
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// this should terminate everything
				App.this.stop();
			}
		});
        MenuItem settingsItem = new MenuItem("Settings");
        settingsItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				App.this.openSettings();
			}
		});
       
        //Add components to pop-up menu
        popup.add(exitItem);
        popup.add(settingsItem);
        trayIcon.setPopupMenu(popup);
        trayIcon.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
				    try {
						Desktop.getDesktop().browse(new URI(consumer.server));
					} catch (IOException | URISyntaxException e1) {
						e1.printStackTrace();
						JOptionPane.showMessageDialog(null, "Cannot open URL: " + e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
        
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.out.println("TrayIcon could not be added.");
            stop();
        }
	}
	
	protected void openSettings() {
		ConfigurationFrame frame = new ConfigurationFrame(properties, new Callback() {
			@Override
			public void onSave(Properties p) {
				properties = p;
				updateFromProperties();
				propertiesLoaded = true;
				try {
					saveProperties(p);
				} catch (IOException e) {
					e.printStackTrace();
	                JOptionPane.showMessageDialog(null, "Error Saving Properties File: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
				// run now;
				timer.restart();
			}
		});
		frame.setVisible(true);

	}

	private void saveProperties(Properties p) throws IOException {
		File file = getPropertiesFileLocation();
		file.getParentFile().mkdirs();
		FileWriter writer = new FileWriter(file);
		p.store(writer, "Settings for JavaMMonitTray v. 0.1");
	}

	
	
	protected void start() throws ClientProtocolException, URISyntaxException, IOException {
		
		timer = new javax.swing.Timer(0, (ae) -> { consume(); });
		timer.setDelay(10000);
		timer.setRepeats(true);
		timer.start();
	}
	
    protected void stop() {
    	try {
	    	timer.stop();
			try {
				consumer.logout();
			} catch (URISyntaxException | IOException e) {
				e.printStackTrace();
			}
			tray.remove(trayIcon);
    	} finally {
    		System.exit(0); // force quit
		}
	}

	private synchronized void consume() {
		try {
			if (propertiesLoaded) {
				Color status = consumer.getWorstStatus(queries, Color.BLUE);
				trayIcon.setImage(makeImage(status));
			} else {
				System.err.println("waiting for properties");
			}
		} catch (Exception e) {
			e.printStackTrace();
			trayIcon.setImage(makeImage(Color.BLACK)); // network error = black
		}
	}

	public static void main( String[] args ) throws IOException, URISyntaxException
    {
        new App();
    }
}
