package org.ogs.bf.server;

import org.ogs.bf.Blockfilter;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ComputeResource extends ServerResource {
	
	double[] filter = new double[] {0., 0., 60., 80.};
	double[] impedances = new double[] {1,2,1,3};
    double[] intimes = new double[] {200, 204, 216, 220};
    double UnitSc = 0.001;
    double dsamp = 1;
    double taper_percent = 10;
    double dft_window_top = 170;
    double dft_window_bot = 220;
	
	
	@Get ("json")
	public String getTest() {
			
		Blockfilter bf = new Blockfilter(filter, impedances, intimes, UnitSc, dsamp, taper_percent, dft_window_top, dft_window_bot);
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		return gson.toJson(bf);
	}

}
