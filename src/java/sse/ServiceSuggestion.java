/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sse;

import java.util.logging.Logger;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

/**
 * REST Web Service for Service Suggestion Engine
 * This implementation is based on the JSR-311 specification.
 * @author mepcotterell
 */
@Path("suggestion")
public class ServiceSuggestion {

    private static final Logger logger = Logger.getLogger(ServiceSuggestion.class.getName());
    
    @Context
    private UriInfo context;

    /**
     * Creates a new instance of GenericResource
     */
    public ServiceSuggestion() {
        // intentionally left blank
    }

    @GET 
    @Path("test/{id}")
    @Consumes("text/html")
    @Produces("text/html")
    public String testOp(@PathParam("id") String id) {
        return "" + id;
    }
    
}
