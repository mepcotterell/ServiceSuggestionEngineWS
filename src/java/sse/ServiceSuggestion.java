/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sse;

import com.sun.jersey.spi.resource.Singleton;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import org.json.JSONArray;
import suggest.ForwardSuggest;
import util.OpWsdl;
import util.OpWsdlScore;

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
    private ServletContext context;

    /**
     * Creates an error callback
     * @param error
     * @return 
     */
    private String wsExtensionsErrorJson (String error) {
        logger.log(Level.WARNING, error);
        return String.format("$.wsextensions_error(\"The Service Suggestion Engine Web Service encountered an error on the server side. <pre>%s</pre>\");", error);
    } // wsExtensionsError
 
 
    private OpWsdl opFromEncodedString (String str) {
        
        String[] tokens = str.split("@");

        if (tokens.length == 2) {
            String opName  = tokens[0];
            String opUrl   = tokens[1];
            return new OpWsdl(opName, opUrl);
        } else if (tokens.length == 3) {
            String opName  = tokens[0];
            String opUrl   = tokens[1];
            String opExtra = tokens[2];
            return new OpWsdl(opName, opUrl, opExtra);
        } // if
        
        return null;
        
    } // opFromEncodedString
    
    @GET 
    @Path("get/jsonp/{direction}")
    @Consumes("text/html")
    @Produces("application/json")
    public String getSuggestionsJSONP (
        @PathParam("direction")   String direction,
        @QueryParam("desired")    String desired,
        @QueryParam("candidates") String candidates,
        @QueryParam("workflow")   String workflow,
        @QueryParam("callback")   String callback
    ) {
        
        logger.log(Level.INFO, "getSuggestionsJSONP operation invoked.");
        
        if (candidates == null) {
            return wsExtensionsErrorJson("Query paramter 'candidates' required for this operation.");
        } // if
        
        if (workflow == null) {
            return wsExtensionsErrorJson("Query paramter 'workflow' required for this operation.");
        } // if
        
        if (callback == null) {
            return wsExtensionsErrorJson("Query paramter 'callback' required for this operation.");
        } // if
        
        // create a JSONArray object to store the suggestions
        JSONArray suggestions = new JSONArray();
        
        // A list to hold the candidate operations
        List<OpWsdl> candidateOps = new ArrayList<OpWsdl>();
        
        if (candidates != null) {
            
           StringTokenizer opTokens = new StringTokenizer(candidates, ",");
           
           while (opTokens.hasMoreTokens()) {
               
               String next = opTokens.nextToken();
               OpWsdl op = opFromEncodedString(next);
               
               if (op != null) {
                   candidateOps.add(op);
               } // if
           
           } // while
           
        } // if
        
        // A list to hold the workflow operations
        List<OpWsdl> workflowOps = new ArrayList<OpWsdl>();
        
        if (workflow != null) {
            
           StringTokenizer opTokens = new StringTokenizer(workflow, ",");
           
           while (opTokens.hasMoreTokens()) {
               
               String next = opTokens.nextToken();
               OpWsdl op = opFromEncodedString(next);
               
               if (op != null) {
                   workflowOps.add(op);
               } // if
           
           } // while
           
        } // if
        
        // Filter out stuff from the candidates that already exist in the workflow
        List <OpWsdl> toDelete = new ArrayList<OpWsdl>();
        for (OpWsdl opA: candidateOps) {
            for (OpWsdl opB: workflowOps) {
                if (opA.compareTo(opB)) {
                    toDelete.add(opA);
                } // if
            } // for
        } // for
        candidateOps.removeAll(toDelete);
        
        ForwardSuggest sugg = new ForwardSuggest();
        List<OpWsdlScore> suggestOpList = sugg.getSuggestServices(workflowOps, candidateOps, desired, this.context.getRealPath("WEB-INF/owl/obi.owl"), null);
        
        
        DecimalFormat df = new DecimalFormat("0.000");
        
        for (OpWsdlScore score: suggestOpList){
            String [] suggestion = {
                score.getOpName(), 
                score.getWsdlName(), 
                df.format(score.getScore()),
                df.format(score.getDmScore()),
                df.format(score.getFnScore()),
                df.format(score.getPeScore()),
                score.getExtraInfo()
            };
            suggestions.put(suggestion);
        } // for
        
        // return the JSON array wrapped with the callback function
        return String.format("%s(\"%s\");", callback, suggestions.toString());
        
    } // getSuggestionsJsonp
    
    @GET 
    @Path("get/xml/{direction}")
    @Consumes("text/html")
    @Produces("application/xml")
    public String getSuggestionsXML (
        @PathParam("direction")   String direction,
        @QueryParam("desired")    String desired,
        @QueryParam("candidates") String candidates,
        @QueryParam("workflow")   String workflow,
        @QueryParam("callback")   String callback
    ) {
        
        logger.log(Level.INFO, "getSuggestionsXML operation invoked.");
        
        if (candidates == null) {
            return wsExtensionsErrorJson("Query paramter 'candidates' required for this operation.");
        } // if
        
        if (workflow == null) {
            return wsExtensionsErrorJson("Query paramter 'workflow' required for this operation.");
        } // if
        
        if (callback == null) {
            return wsExtensionsErrorJson("Query paramter 'callback' required for this operation.");
        } // if
        
        return direction + " suggestion, using jsonp format! " + desired;
        
    } // getSuggestionsXML
    
} // ServiceSuggestion
