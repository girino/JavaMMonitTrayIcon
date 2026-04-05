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
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.Executors;

import org.junit.After;
import org.junit.Test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * HTTP integration tests for {@link MMonitConsumer} using a local {@link HttpServer}.
 */
public class MMonitConsumerTest {

	private HttpServer server;

	@After
	public void stopServer() {
		if (server != null) {
			server.stop(0);
			server = null;
		}
	}

	private String startServer(HttpHandler handler) throws IOException {
		server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
		server.createContext("/", handler);
		server.setExecutor(Executors.newSingleThreadExecutor(r -> {
			Thread t = new Thread(r, "test-http");
			t.setDaemon(true);
			return t;
		}));
		server.start();
		int port = server.getAddress().getPort();
		return "http://127.0.0.1:" + port;
	}

	private static void respond(HttpExchange ex, int code, String body, String... headerPairs) throws IOException {
		byte[] raw = body.getBytes(StandardCharsets.UTF_8);
		ex.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
		for (int i = 0; i + 1 < headerPairs.length; i += 2) {
			ex.getResponseHeaders().add(headerPairs[i], headerPairs[i + 1]);
		}
		ex.sendResponseHeaders(code, raw.length);
		OutputStream os = ex.getResponseBody();
		os.write(raw);
		os.close();
	}

	private static void drainBody(HttpExchange ex) throws IOException {
		byte[] buf = new byte[1024];
		while (ex.getRequestBody().read(buf) >= 0) {
			// discard
		}
		ex.getRequestBody().close();
	}

	@Test
	public void basic_getWorstStatus_firstPositiveRuleWins() throws Exception {
		String base = startServer(ex -> {
			String path = ex.getRequestURI().getPath();
			if ("/api".equals(path) && "GET".equals(ex.getRequestMethod())) {
				respond(ex, 200, "{\"high\":2,\"low\":1}");
			} else {
				respond(ex, 404, "{}");
			}
		});
		Map<String, String> paths = Collections.singletonMap("api", "/api");
		MMonitConsumer c = new MMonitConsumer(base, "BASIC", Collections.emptyList(), paths);
		List<Pair<Color, String>> queries = new ArrayList<>();
		queries.add(new Pair<>(Color.RED, ".high"));
		queries.add(new Pair<>(Color.GREEN, ".low"));
		assertEquals(Color.RED, c.getWorstStatus(queries, Color.BLUE));
	}

	@Test
	public void basic_getWorstStatus_returnsDefaultWhenAllZero() throws Exception {
		String base = startServer(ex -> {
			if ("/api".equals(ex.getRequestURI().getPath())) {
				respond(ex, 200, "{\"a\":0,\"b\":0}");
			} else {
				respond(ex, 404, "{}");
			}
		});
		Map<String, String> paths = Collections.singletonMap("api", "/api");
		MMonitConsumer c = new MMonitConsumer(base, "BASIC", Collections.emptyList(), paths);
		List<Pair<Color, String>> queries = new ArrayList<>();
		queries.add(new Pair<>(Color.RED, ".a"));
		queries.add(new Pair<>(Color.GREEN, ".b"));
		assertEquals(Color.BLUE, c.getWorstStatus(queries, Color.BLUE));
	}

	@Test
	public void basic_authTypeIsCaseInsensitive() throws Exception {
		String base = startServer(ex -> {
			if ("/api".equals(ex.getRequestURI().getPath())) {
				respond(ex, 200, "{\"x\":1}");
			} else {
				respond(ex, 404, "{}");
			}
		});
		Map<String, String> paths = Collections.singletonMap("api", "/api");
		MMonitConsumer c = new MMonitConsumer(base, "basic", Collections.emptyList(), paths);
		List<Pair<Color, String>> queries = Collections.singletonList(new Pair<>(Color.RED, ".x"));
		assertEquals(Color.RED, c.getWorstStatus(queries, Color.BLUE));
	}

	@Test
	public void basic_getWorstStatus_secondRuleWhenFirstIsZero() throws Exception {
		String base = startServer(ex -> {
			if ("/api".equals(ex.getRequestURI().getPath())) {
				respond(ex, 200, "{\"a\":0,\"b\":5}");
			} else {
				respond(ex, 404, "{}");
			}
		});
		Map<String, String> paths = Collections.singletonMap("api", "/api");
		MMonitConsumer c = new MMonitConsumer(base, "BASIC", Collections.emptyList(), paths);
		List<Pair<Color, String>> queries = new ArrayList<>();
		queries.add(new Pair<>(Color.RED, ".a"));
		queries.add(new Pair<>(Color.GREEN, ".b"));
		assertEquals(Color.GREEN, c.getWorstStatus(queries, Color.BLUE));
	}

	@Test(expected = RuntimeException.class)
	public void basic_non200FromApi_throws() throws Exception {
		String base = startServer(ex -> respond(ex, 503, "{}"));
		Map<String, String> paths = Collections.singletonMap("api", "/api");
		MMonitConsumer c = new MMonitConsumer(base, "BASIC", Collections.emptyList(), paths);
		c.getWorstStatus(Collections.singletonList(new Pair<>(Color.RED, ".x")), Color.BLUE);
	}

	@Test
	public void logout_doesNothingWhenNeverLoggedIn() throws Exception {
		String base = startServer(ex -> {
			respond(ex, 500, "should not be called");
		});
		Map<String, String> paths = new HashMap<>();
		paths.put("api", "/api");
		paths.put("logout", "/logout");
		MMonitConsumer c = new MMonitConsumer(base, "BASIC", Collections.emptyList(), paths);
		c.logout();
	}

	@Test
	public void basic_logout_requestsLogoutPath() throws Exception {
		AtomicInteger logoutGets = new AtomicInteger();
		String base = startServer(ex -> {
			String path = ex.getRequestURI().getPath();
			if ("/api".equals(path) && "GET".equals(ex.getRequestMethod())) {
				respond(ex, 200, "{}");
			} else if ("/logout".equals(path) && "GET".equals(ex.getRequestMethod())) {
				logoutGets.incrementAndGet();
				respond(ex, 200, "");
			} else {
				respond(ex, 404, "");
			}
		});
		Map<String, String> paths = new HashMap<>();
		paths.put("api", "/api");
		paths.put("logout", "/logout");
		MMonitConsumer c = new MMonitConsumer(base, "BASIC", Collections.emptyList(), paths);
		c.getWorstStatus(Collections.singletonList(new Pair<>(Color.RED, ".nope")), Color.BLUE);
		c.logout();
		assertEquals(1, logoutGets.get());
	}

	@Test
	public void form_initLoginThenApi_returnsWorstStatus() throws Exception {
		AtomicInteger initGets = new AtomicInteger();
		AtomicInteger loginPosts = new AtomicInteger();
		String base = startServer(ex -> {
			String path = ex.getRequestURI().getPath();
			String method = ex.getRequestMethod();
			if ("/init".equals(path) && "GET".equals(method)) {
				initGets.incrementAndGet();
				respond(ex, 200, "", "Set-Cookie", "sid=test; Path=/");
			} else if ("/login".equals(path) && "POST".equals(method)) {
				loginPosts.incrementAndGet();
				drainBody(ex);
				respond(ex, 200, "");
			} else if ("/api".equals(path) && "GET".equals(method)) {
				respond(ex, 200, "{\"bad\":1}");
			} else {
				drainBody(ex);
				respond(ex, 404, "{}");
			}
		});
		Map<String, String> paths = new HashMap<>();
		paths.put("init", "/init");
		paths.put("login", "/login");
		paths.put("logout", "/logout");
		paths.put("api", "/api");
		List<Pair<String, String>> auth = Collections.singletonList(new Pair<>("user", "x"));
		MMonitConsumer c = new MMonitConsumer(base, "FORM", auth, paths);
		List<Pair<Color, String>> queries = Collections.singletonList(new Pair<>(Color.RED, ".bad"));
		assertEquals(Color.RED, c.getWorstStatus(queries, Color.BLUE));
		assertEquals(1, initGets.get());
		assertEquals(1, loginPosts.get());
	}

	@Test
	public void form_cookieFromInitSentOnSubsequentRequests() throws Exception {
		AtomicInteger apiWithCookie = new AtomicInteger();
		String base = startServer(ex -> {
			String path = ex.getRequestURI().getPath();
			String method = ex.getRequestMethod();
			if ("/init".equals(path) && "GET".equals(method)) {
				respond(ex, 200, "", "Set-Cookie", "token=abc; Path=/");
			} else if ("/login".equals(path) && "POST".equals(method)) {
				drainBody(ex);
				respond(ex, 200, "");
			} else if ("/api".equals(path) && "GET".equals(method)) {
				String cookie = ex.getRequestHeaders().getFirst("Cookie");
				if (cookie != null && cookie.contains("token=abc")) {
					apiWithCookie.incrementAndGet();
				}
				respond(ex, 200, "{}");
			} else {
				drainBody(ex);
				respond(ex, 404, "{}");
			}
		});
		Map<String, String> paths = new HashMap<>();
		paths.put("init", "/init");
		paths.put("login", "/login");
		paths.put("logout", "/logout");
		paths.put("api", "/api");
		MMonitConsumer c = new MMonitConsumer(base, "FORM", Collections.emptyList(), paths);
		c.getWorstStatus(Collections.singletonList(new Pair<>(Color.RED, ".x")), Color.BLUE);
		assertTrue(apiWithCookie.get() >= 1);
	}
}
