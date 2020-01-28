package org.girino.tray.mmonittray;

import java.awt.AWTException;
import java.awt.Color;
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
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import javax.swing.ImageIcon;

import org.apache.http.client.ClientProtocolException;

/**
 * Hello world!
 *
 */
public class App 
{
	
	TrayIcon trayIcon;
	Timer timer = new Timer();
	MMonitConsumer consumer;
	final SystemTray tray = SystemTray.getSystemTray();
	List<Pair<Color, String>> queries;

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
		
		Properties p = new Properties();
		p.load(App.class.getResourceAsStream("/properties/config.properties"));
		
		String server = p.getProperty("server", "https://mmonit.example.com");
		
		// List of pages
		Map<String,String> pageMap = toMap(getPropertiesAsList(p, "path."));
		
		// JQ queries
		queries = getPropertiesAsList(p, "query.").stream()
				.sorted((a,b) -> a.getKey().compareTo(b.getKey()))
				.map((q) -> new Pair<Color, String>(getColorByName(q.getKey(), true), q.getValue()))
				.collect(Collectors.toList());
		
		List<Pair<String, String>> authStrings = new ArrayList<Pair<String,String>>();
		List<String> requiredPages = Arrays.asList("api");
		String authType = p.getProperty("auth.type", "BASIC"); 
		if (authType.equalsIgnoreCase("FORM")) {
			authStrings = getPropertiesAsList(p, "auth.form.");
			requiredPages = Arrays.asList("login", "logout", "init", "api");
		}
		
		if (!pageMap.keySet().containsAll(requiredPages)) {
			throw new RuntimeException("Missing requeired pages.");
		}
		
		consumer = new MMonitConsumer(server, authType, authStrings, pageMap);
		
		start();
		createTray();
	}

	private Color getColorByName(String colorName, boolean removeNumberPrefix) {
		
		if (removeNumberPrefix) {
			colorName = colorName.replaceFirst("^\\d+\\.", "");
		}
		
		try {
			return (Color) Color.class.getDeclaredField(colorName).get(null);
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
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
       
        //Add components to pop-up menu
        popup.add(exitItem);
        trayIcon.setPopupMenu(popup);
        
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.out.println("TrayIcon could not be added.");
            stop();
        }
	}
	
	protected void start() throws ClientProtocolException, URISyntaxException, IOException {
		consumer.logIn();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				try {
					Color status = consumer.getWorstStatus(queries, Color.BLUE);
					trayIcon.setImage(makeImage(status));
				} catch (Exception e) {
					e.printStackTrace();
					trayIcon.setImage(makeImage(Color.BLUE));
				}
			}
		}, 0, 10000);
	}
	
    protected void stop() {
    	try {
	    	timer.cancel();
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

	public static void main( String[] args ) throws IOException, URISyntaxException
    {
        new App();
    }
}
