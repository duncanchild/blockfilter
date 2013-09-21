package org.ogs.bf.server;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class TestResource extends ServerResource {
	
	@Get // ("json")
	public String getTest() {
		System.out.println("found resource");
		return "hello " + System.currentTimeMillis();
	}

}
