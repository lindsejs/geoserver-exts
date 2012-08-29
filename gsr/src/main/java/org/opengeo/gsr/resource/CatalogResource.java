/* Copyright (c) 2001 - 2012 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.opengeo.gsr.resource;

import java.util.ArrayList;
import java.util.List;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.LayerGroupInfo;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.catalog.rest.AbstractCatalogResource;
import org.geoserver.config.GeoServer;
import org.geoserver.config.util.XStreamPersister;
import org.geoserver.rest.format.DataFormat;
import org.opengeo.gsr.core.exception.ServiceError;
import org.opengeo.gsr.core.exception.ServiceException;
import org.opengeo.gsr.service.AbstractService;
import org.opengeo.gsr.service.CatalogService;
import org.opengeo.gsr.service.GeometryService;
import org.opengeo.gsr.service.MapService;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

/**
 * 
 * @author Juan Marin, OpenGeo
 * 
 */
public class CatalogResource extends GeoServicesResource {

    protected Class clazz;

    private String formatValue;

    private String callback;

    private String workspace;

    private final String productName = "OpenGeo Suite";

    private final String specVersion = "1.0";

    private final String currentVersion = "10.1";

    public CatalogResource(Context context, Request request, Response response, Class clazz,
            GeoServer geoServer) {
        super(context, request, response, clazz, geoServer);
        this.formatValue = getAttribute("format");
        this.workspace = getAttribute("workspace");
        this.callback = getRequest().getResourceRef().getQueryAsForm().getFirstValue("callback");
    }

    @Override
    protected Object handleObjectGet() throws Exception {
        try {
            if (!formatValue.equals("json")) {
                List<String> details = new ArrayList<String>();
                details.add("Format " + formatValue + " is not supported");
                return new ServiceException(new ServiceError(
                        (Status.CLIENT_ERROR_BAD_REQUEST.getCode()), "Output format not supported",
                        details));
            }
            List<AbstractService> services = new ArrayList<AbstractService>();
            List<String> folders = new ArrayList<String>();
            List<LayerGroupInfo> layerGroupsInfo = null;
            if (workspace != null) {
                WorkspaceInfo workspaceInfo = catalog.getFacade().getWorkspaceByName(workspace);
                if (workspaceInfo != null) {
                    layerGroupsInfo = catalog.getFacade().getLayerGroupsByWorkspace(workspaceInfo);
                    for (LayerGroupInfo layerGroupInfo : layerGroupsInfo) {
                        MapService mapService = new MapService(layerGroupInfo.getName());
                        services.add(mapService);
                    }
                    // TODO: get Suite version number at runtime
                    return new CatalogService("services", specVersion, productName, currentVersion,
                            folders, services);
                }
            }
            GeometryService geometryService = new GeometryService("Geometry");
            layerGroupsInfo = catalog.getFacade().getLayerGroups();
            for (LayerGroupInfo layerGroupInfo : layerGroupsInfo) {
                if (layerGroupInfo.getWorkspace() == null) {
                    MapService mapService = new MapService(layerGroupInfo.getName());
                    services.add(mapService);
                } else {
                    String folder = layerGroupInfo.getWorkspace().getName();
                    folders.add(folder);
                }
            }
            services.add(geometryService);
            return new CatalogService("services", specVersion, productName, currentVersion,
                    folders, services);
            // TODO: handle JSONP callback
        } catch (Exception e) {
            List<String> details = new ArrayList<String>();
            details.add(e.getMessage());
            return new ServiceException(new ServiceError((Status.SERVER_ERROR_INTERNAL.getCode()),
                    "Internal Server Error", details));
        }
    }
}