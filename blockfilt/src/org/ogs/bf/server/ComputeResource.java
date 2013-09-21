package org.ogs.bf.server;

import java.net.URI;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Map;

import org.ogs.bf.Blockfilter;
import org.restlet.data.Form;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ComputeResource extends ServerResource {
	
	// double[] filter = new double[] {0., 0., 60., 80.};
	double[] impedances = new double[] {1,2,1,3};
    double[] intimes = new double[] {200, 204, 216, 220};
    double UnitSc = 0.001;
    double dsamp = 1;
    double taper_percent = 10;
    double dft_window_top = 170;
    double dft_window_bot = 220;
	
	
	@Get ("json")
	public String getTest() {
		
		Form form = getRequest().getResourceRef().getQueryAsForm();
		Map<String, String> vals = form.getValuesMap();
		for (String key : vals.keySet()) {
			System.out.println(key + " " + vals.get(key));
		}
			
		Blockfilter bf = new Blockfilter(getFilter(vals), parseModel(vals), intimes, UnitSc, dsamp, taper_percent, dft_window_top, dft_window_bot);
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		return gson.toJson(bf);
	}
	
	public double[] getFilter(Map<String, String> params) {
		double[] filter = new double[4];
		for (int i = 1; i < 5; i++) {
			filter[i-1] = Double.parseDouble(params.get("filter" + i));
		}
		return filter;
	}
	
	public double[] parseModel(Map<String, String> params) {
		String rawPts = params.get("model");
		System.out.println("---" + rawPts);
		String pts2 = URLDecoder.decode(rawPts);
		
		System.out.println(pts2);
	
		String[] pts = pts2.split(" ,");
		System.out.println(Arrays.toString(pts));
		double[] res = new double[pts.length];
		for (int i = 0; i < pts.length; i++) {
			res[i] = Double.parseDouble(pts[i]);
		}
		
		return res;
	
	}
	
	

}
