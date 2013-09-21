package org.ogs.bf.server;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.data.Protocol;

public class MyServer extends Application {

	public static void main(String[] args) throws Exception {
		System.out.println("starting server .... ");
		Component component = new Component();
		component.getServers().add(Protocol.HTTP, 80);
		component.getDefaultHost().attach("/run", ComputeResource.class);
		component.start();
	}
}
