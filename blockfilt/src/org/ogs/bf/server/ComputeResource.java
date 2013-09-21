package org.ogs.bf.server;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import com.google.gson.Gson;

public class ComputeResource extends ServerResource {
	
	@Get ("json")
	public String getTest() {
		
		Gson gson = new Gson();
		return gson.toJson(new Results());
	}

}
