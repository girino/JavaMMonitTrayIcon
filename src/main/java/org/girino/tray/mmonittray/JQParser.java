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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.thisptr.jackson.jq.BuiltinFunctionLoader;
import net.thisptr.jackson.jq.JsonQuery;
import net.thisptr.jackson.jq.Scope;
import net.thisptr.jackson.jq.Versions;

public class JQParser {
	private static final ObjectMapper MAPPER = new ObjectMapper();
	private static final Scope rootScope = Scope.newEmptyScope();
	static {
		BuiltinFunctionLoader.getInstance().loadFunctions(Versions.JQ_1_6, rootScope);
	}

	private static JQParser _instance = new JQParser();
	private JQParser() {
		
	}
	public static JQParser getInstance() {
		return _instance;
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
}
