package org.girino.tray.mmonittray;

import java.awt.Color;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class MMonitConsumer {

	String server = "https://graphs.girino.org:8443";
	String user = "validator";
	String password = "jMPt0MiKa95h9PrXg";
	boolean isLoggedIn = false;
	BasicCookieStore cookieStore = new BasicCookieStore();
	
	public MMonitConsumer(String server, String user, String password) {
		this.server = server;
		this.user = user;
		this.password = password;
		isLoggedIn = false;
	}

	
	void logout() throws URISyntaxException, ClientProtocolException, IOException {
		if (!isLoggedIn) {
			return;
		}
		
		URL logoutUrl = new URL(server + "/login/logout.csp");

		CloseableHttpClient httpclient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
		try {
			HttpGet sessionGet = new HttpGet(logoutUrl.toURI());
			try (CloseableHttpResponse response = httpclient.execute(sessionGet)) {
				int status = response.getStatusLine().getStatusCode(); 
				if ( status == 200 ) {
					EntityUtils.consume(response.getEntity());
				} else {
					throw new RuntimeException("Bad response status code: " + status);
				}
			}

		} finally {
			httpclient.close();
		}
	}

	void logIn() throws URISyntaxException, ClientProtocolException, IOException {
		URL main = new URL(server + "/index.csp");
		URL loginUrl = new URL(server + "/z_security_check");

		CloseableHttpClient httpclient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
		try {
			HttpGet sessionGet = new HttpGet(main.toURI());
			try (CloseableHttpResponse response = httpclient.execute(sessionGet)) {
				int status = response.getStatusLine().getStatusCode(); 
				if ( status == 200 ) {
					EntityUtils.consume(response.getEntity());
				} else {
					throw new RuntimeException("Bad response status code: " + status);
				}
			}

			HttpUriRequest loginRequest = RequestBuilder.post().setUri(loginUrl.toURI()).addParameter("z_username", user)
					.addParameter("z_password", password)
					.addParameter("z_csrf_protection", "off")
					.build();
			
			try (CloseableHttpResponse response = httpclient.execute(loginRequest)) {
				int status = response.getStatusLine().getStatusCode(); 
				if ( status == 200 || status == 302 ) {
					EntityUtils.consume(response.getEntity());
				} else {
					throw new RuntimeException("Bad response status code: " + status);
				}
			}

		} finally {
			httpclient.close();
		}
		isLoggedIn = true;
	}
	
	String getStatusListAsString() throws URISyntaxException, IOException {
		
		if (!isLoggedIn) {
			logIn();
		}
		
		URL logoutUrl = new URL(server + "/status/hosts/list");

		CloseableHttpClient httpclient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
		try {
			HttpGet sessionGet = new HttpGet(logoutUrl.toURI());
			try (CloseableHttpResponse response = httpclient.execute(sessionGet)) {
				int status = response.getStatusLine().getStatusCode(); 
				if ( status == 200 ) {
					return EntityUtils.toString(response.getEntity());
				} else {
					throw new RuntimeException("Bad response status code: " + status);
				}
			}

		} finally {
			httpclient.close();
		}
		
	}
	
	JsonObject getStatusListAsJsonObject() throws URISyntaxException, IOException {
		String jsonString = getStatusListAsString();
		JsonElement root = JsonParser.parseString(jsonString);
		return root.getAsJsonObject();
	}
	
	final static Color[] COLORS = {Color.RED, Color.ORANGE, Color.GREEN, Color.GRAY};
	List<Color> getStatusList() throws URISyntaxException, IOException {
		JsonObject j = getStatusListAsJsonObject();
		if (!j.has("records")) {
			throw new RuntimeException("No records present.");
		}
		List<Color> ret = new ArrayList<Color>();
		JsonArray records = j.getAsJsonArray("records");
		records.forEach(new Consumer<JsonElement>() {
			@Override
			public void accept(JsonElement t) {
				JsonObject record = t.getAsJsonObject();
				if (!record.has("led")) {
					throw new RuntimeException("One Record does not contain leds.");
				} else {
					ret.add(COLORS[record.getAsJsonPrimitive("led").getAsInt()]);
				}
			}
		});
		return ret;
	}
	
	Color getWorstStatus() throws URISyntaxException, IOException {
		Set<Color> l = new HashSet<Color>(getStatusList());
		if (l.contains(Color.RED)) {
			return Color.RED;
		} else if (l.contains(Color.YELLOW)) {
			return Color.YELLOW;
		} else if (l.contains(Color.GRAY)) {
			return Color.GRAY;
		}
		return Color.GREEN;
	}

}
