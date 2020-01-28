package org.girino.tray.mmonittray;

import java.awt.Color;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.thisptr.jackson.jq.BuiltinFunctionLoader;
import net.thisptr.jackson.jq.JsonQuery;
import net.thisptr.jackson.jq.Scope;
import net.thisptr.jackson.jq.Versions;

public class MMonitConsumer {

	String server;
	String authType;
	List<Pair<String, String>> authStrings;
	Map<String, String> pageMap;
	boolean isLoggedIn = false;
	BasicCookieStore cookieStore = new BasicCookieStore();
	
	public MMonitConsumer(String server, String authType, List<Pair<String, String>> authStrings, Map<String, String> pageMap) {
		this.server = server;
		this.authType = authType;
		this.authStrings = authStrings;
		this.pageMap = pageMap;
		isLoggedIn = false;
	}

	
	void logout() throws URISyntaxException, ClientProtocolException, IOException {
		if (!isLoggedIn) {
			return;
		}
		
		URL logoutUrl = new URL(server + pageMap.get("logout"));

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

		// Basic Auth doesnt need login.
		if (authType.equalsIgnoreCase("FORM")) {
			URL main = new URL(server + pageMap.get("init"));
			URL loginUrl = new URL(server + pageMap.get("login"));
	
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
	
				RequestBuilder builder = RequestBuilder.post().setUri(loginUrl.toURI());
				for (Pair<String, String> pair : authStrings) {
					builder.addParameter(pair.getKey(), pair.getValue());
				}
				HttpUriRequest loginRequest = builder.build();
				
				
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
		}
		isLoggedIn = true;
	}
	
	String getStatusListAsString() throws URISyntaxException, IOException {
		
		if (!isLoggedIn) {
			logIn();
		}
		
		URL logoutUrl = new URL(server + pageMap.get("api"));

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
	
	private static final ObjectMapper MAPPER = new ObjectMapper();
	private static final Scope rootScope = Scope.newEmptyScope();
	static {
		BuiltinFunctionLoader.getInstance().loadFunctions(Versions.JQ_1_6, rootScope);
	}

	JsonNode getStatusListAsJsonNode() throws URISyntaxException, IOException {
		String jsonString = getStatusListAsString();
		JsonNode root = MAPPER.readTree(jsonString);
		return root;
	}
	
	JsonNode getJQResult(String query, String json) throws IOException {
		JsonQuery q = JsonQuery.compile(query, Versions.JQ_1_6);
		JsonNode in = MAPPER.readTree(json);

		final List<JsonNode> out = new ArrayList<JsonNode>(1);
		q.apply(rootScope, in, out::add);
		
		return out.get(0);
	}
	
	int getJQResultAsIt(String query, String json) throws IOException {
		JsonNode n = getJQResult(query, json);
		return n.asInt();
	}

	public Color getWorstStatus(List<Pair<Color, String>> queries, Color defaultColor) throws URISyntaxException, IOException {
		
		String json = getStatusListAsString();
		
		for (Pair<Color, String> pair : queries) {
			if (getJQResultAsIt(pair.getValue(), json) > 0) {
				return pair.getKey();
			}
		}
		return defaultColor;
	}

}
