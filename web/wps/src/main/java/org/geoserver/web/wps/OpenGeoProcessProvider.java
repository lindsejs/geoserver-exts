package org.geoserver.web.wps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.geoserver.web.wicket.GeoServerDataProvider;
import org.geotools.process.ProcessFactory;
import org.geotools.process.Processors;
import org.opengis.feature.type.Name;

public final class OpenGeoProcessProvider extends GeoServerDataProvider<ProcessDescriptor> {

	private static final long serialVersionUID = -4209175487620050606L;
	
	public static final Property<ProcessDescriptor> NAME = new BeanProperty<ProcessDescriptor>("name", "name");
	public static final Property<ProcessDescriptor> DESCRIPTION = new BeanProperty<ProcessDescriptor>("description", "description");
	public static final Property<ProcessDescriptor> LINKS = new PropertyPlaceholder<ProcessDescriptor>("links");

	@SuppressWarnings("unchecked")
	@Override
	protected List<Property<ProcessDescriptor>> getProperties() {
		return Arrays.asList(NAME, DESCRIPTION, LINKS);
	}

	@Override
	protected List<ProcessDescriptor> getItems() {
	    List<ProcessDescriptor> results = new ArrayList<ProcessDescriptor>();
	    for (ProcessFactory factory : Processors.getProcessFactories()) {
	        for (Name name : factory.getNames()) {
	            results.add(new ProcessDescriptor(name, factory.getDescription(name).toString()));
	        }
	    }
	    
		return results;
	}
}
