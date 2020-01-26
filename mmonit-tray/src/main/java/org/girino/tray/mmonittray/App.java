package org.girino.tray.mmonittray;

import java.awt.AWTException;
import java.awt.Color;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

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
	

	static final Map<Color,Image> COLOR_MAP = makeColorImageMap();
	
	static Image makeImage(Color status) {
        Image i = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics g = i.getGraphics(); 
        g.setColor(status);
        g.fillOval(0, 0, 16, 16);
        return i;
	}
	
	static Map<Color,Image> makeColorImageMap() {
		
		Map<Color,Image> ret = new HashMap<Color, Image>();
		
		ret.put(Color.RED, makeImage(Color.RED));
		ret.put(Color.GREEN, makeImage(Color.GREEN));
		ret.put(Color.GRAY, makeImage(Color.GRAY));
		ret.put(Color.ORANGE, makeImage(Color.ORANGE));
		ret.put(Color.BLUE, makeImage(Color.BLUE));

		return ret;
	}
	
	public App() throws IOException, URISyntaxException {
		
		Properties p = new Properties();
		p.load(App.class.getResourceAsStream("/properties/config.properties"));
		
		String server = p.getProperty("server", "https://mmonit.example.com");
		String user = p.getProperty("user", "admin");
		String password = p.getProperty("password", "password");

		consumer = new MMonitConsumer(server, user, password);
		
		start();
		createTray();
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
        trayIcon = new TrayIcon(COLOR_MAP.get(Color.GRAY));
       
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
					Color status = consumer.getWorstStatus();
					trayIcon.setImage(COLOR_MAP.get(status));
				} catch (Exception e) {
					e.printStackTrace();
					trayIcon.setImage(COLOR_MAP.get(Color.BLUE));
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
