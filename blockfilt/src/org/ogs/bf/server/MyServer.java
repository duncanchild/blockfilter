package org.ogs.bf.server;

import java.io.File;
import java.util.Arrays;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.LocalReference;
import org.restlet.data.Protocol;
import org.restlet.resource.Directory;
import org.restlet.routing.Router;

public class MyServer {

	public static void main(String[] args) throws Exception {
		System.out.println("starting server .... ");
		Component component = new Component();
		component.getServers().add(Protocol.HTTP, 80);
		component.getDefaultHost().attach("/run", ComputeResource.class);
		component.getClients().add(Protocol.FILE);

		final Application application = new Application() {
			@Override
			public Restlet createInboundRoot() {
				System.out.println("initial inbound root");
				String loc = "" + LocalReference.createFileReference(new File(System
								.getProperty("user.dir"))) + "/web";
				System.out.println(loc);
				Directory directory = new Directory(getContext(), loc);
				directory.setListingAllowed(true);

				return directory;
			}
		};
		
		component.getDefaultHost().attach("/", application);
		
		component.start();
	}
}
