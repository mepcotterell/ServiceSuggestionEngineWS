/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sse;

import com.sun.jersey.spi.resource.Singleton;
import java.util.logging.Level;
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
@Singleton
public class ServiceSuggestion {

    private static final Logger logger = Logger.getLogger(ServiceSuggestion.class.getName());
    
    @Context
    private UriInfo context;

    /**
     * test doc
     * @param format
     * @param direction
     * @return 
     */
    @GET 
    @Path("get/jsonp/{direction}")
    @Consumes("text/html")
    @Produces("text/html")
    public String getSuggestionsJsonp (
        @PathParam("direction")   String direction,
        @QueryParam("desired")    String desired,
        @QueryParam("candidates") String candidates,
        @QueryParam("workflow")   String workflow,
        @QueryParam("callback")   String callback
    ) {
        
        logger.log(Level.INFO, "getSuggestionsJsonp operation invoked.");
        
        if (candidates == null) {
            logger.log(Level.WARNING, 
                    "Query paramter 'candidates' required for this operation.");
        } // if
        
        if (workflow == null) {
            logger.log(Level.WARNING, 
                    "Query paramter 'workflow' required for this operation.");
        } // if
        
        if (callback == null) {
            logger.log(Level.WARNING, 
                    "Query paramter 'callback' required for this operation.");
        } // if
        
        return direction + " suggestion, using jsonp format! " + desired;
    } // getSuggestionsJsonp
    
} // ServiceSuggestion
