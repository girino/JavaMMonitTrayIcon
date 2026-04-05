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

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Tests for {@link JQParser} (jq expressions used for status rules).
 */
public class JQParserTest {

	private final JQParser parser = JQParser.getInstance();

	@Test
	public void getJQResultAsIt_numericField() throws Exception {
		assertEquals(42, parser.getJQResultAsIt(".value", "{\"value\":42}"));
	}

	@Test
	public void getJQResultAsIt_zeroWhenMissing() throws Exception {
		assertEquals(0, parser.getJQResultAsIt(".missing", "{}"));
	}

	@Test
	public void getJQResultAsIt_readmeStyleBooleanToInt() throws Exception {
		String json = "{ \"red\": \"enabled\", \"green\": \"enabled\" }";
		String redRule = ".red == \"enabled\" | if . then 1 else 0 end";
		assertEquals(1, parser.getJQResultAsIt(redRule, json));
		String greenRule = ".green == \"enabled\" | if . then 1 else 0 end";
		assertEquals(1, parser.getJQResultAsIt(greenRule, json));
	}

	@Test
	public void getJQResultAsIt_mmonitStyleSummarySlice() throws Exception {
		String json = "{\"status\":["
				+ "{\"label\":\"failed\",\"data\":3},"
				+ "{\"label\":\"ok\",\"data\":10}"
				+ "]}";
		String failed = ".status[] | select( .label == \"failed\" ) | .data";
		assertEquals(3, parser.getJQResultAsIt(failed, json));
		String ok = ".status[] | select( .label == \"ok\" ) | .data";
		assertEquals(10, parser.getJQResultAsIt(ok, json));
	}

	@Test
	public void getJQResult_returnsJsonNode() throws Exception {
		JsonNode n = parser.getJQResult(".name", "{\"name\":\"x\"}");
		assertTrue(n.isTextual());
		assertEquals("x", n.asText());
	}

	@Test
	public void getJQResult_arrayLength() throws Exception {
		JsonNode n = parser.getJQResult("[.items[]] | length", "{\"items\":[1,2,3]}");
		assertTrue(n.isNumber());
		assertEquals(3, n.asInt());
	}

	@Test(expected = com.fasterxml.jackson.core.JsonParseException.class)
	public void getJQResultAsIt_invalidJson() throws Exception {
		parser.getJQResultAsIt(".", "not json");
	}

	@Test(expected = net.thisptr.jackson.jq.exception.JsonQueryException.class)
	public void getJQResultAsIt_invalidJqSyntax() throws Exception {
		parser.getJQResultAsIt("this is not jq", "{}");
	}
}
