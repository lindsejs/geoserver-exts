/* Copyright (c) 2001 - 2012 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.opengeo.gsr.core.format;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;

import org.geoserver.rest.format.ReflectiveJSONFormat;
import org.opengeo.gsr.core.feature.FeatureConverter;
import org.opengeo.gsr.core.feature.FieldTypeConverter;
import org.opengeo.gsr.core.geometry.Geometry;
import org.opengeo.gsr.core.geometry.GeometryTypeConverter;
import org.opengeo.gsr.core.geometry.Point;
import org.opengeo.gsr.core.geometry.SpatialReference;
import org.opengeo.gsr.core.geometry.SpatialReferenceWKID;
import org.opengeo.gsr.service.CatalogService;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;
import com.thoughtworks.xstream.io.json.JsonWriter;

/**
 * 
 * @author Juan Marin, OpenGeo
 * 
 */
public class GeoServicesJsonFormat extends ReflectiveJSONFormat {

    XStream xStream;

    public XStream getXStream() {
        return xStream;
    }

    public void setXStream(XStream xStream) {
        this.xStream = xStream;
    }

    public GeoServicesJsonFormat() {
        super();
        configureXStream();
    }

    private void configureXStream() {
        XStream xstream = new XStream(new JsonHierarchicalStreamDriver() {
            public HierarchicalStreamWriter createWriter(Writer writer) {
                return new JsonWriter(writer, JsonWriter.DROP_ROOT_MODE);
            }
        });

        xstream.alias("", Geometry.class);
        xstream.alias("", Point.class);
        xstream.alias("", SpatialReference.class);
        xstream.alias("", SpatialReferenceWKID.class);

        // omit fields
        xstream.omitField(CatalogService.class, "name");
        xstream.omitField(CatalogService.class, "type");
        xstream.omitField(CatalogService.class, "serviceType");
        xstream.omitField(CatalogService.class, "specVersion");
        xstream.omitField(CatalogService.class, "productName");

        xstream.omitField(Point.class, "geometryType");
        xstream.omitField(SpatialReferenceWKID.class, "geometryType");

        // converters
        xstream.registerConverter(new FieldTypeConverter());
        xstream.registerConverter(new GeometryTypeConverter());
        // xstream.registerConverter(new FeatureConverter());

        this.xStream = xstream;
    }

    @Override
    protected Object read(InputStream input) throws IOException {
        return xStream.fromXML(input);
    }

    @Override
    protected void write(Object data, OutputStream output) throws IOException {
        xStream.toXML(data, output);
    }

}
