/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sse;

import com.sun.jersey.spi.resource.Singleton;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import org.json.JSONArray;
import sse.entity.Operation;
import sse.entity.Service;
import sse.entity.SuggestedOperation;
import sse.entity.message.request.ServiceSuggestionRequest;
import sse.entity.message.response.ServiceSuggestionResponse;
import suggest.ForwardSuggest;
import util.WebServiceOpr;
import util.WebServiceOprScore;

/**
 * REST Web Service for Service Suggestion Engine
 * This implementation is based on the JSR-311 specification.
 * @author mepcotterell
 */
@Path("serviceSuggestion")
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
 
    private WebServiceOpr opFromEncodedString (String str) {
        
        String[] tokens = str.split("@");

        if (tokens.length == 2) {
            String opName  = tokens[0];
            String opUrl   = tokens[1];
            return new WebServiceOpr(opName, opUrl);
        } else if (tokens.length == 3) {
            String opName  = tokens[0];
            String opUrl   = tokens[1];
            String opExtra = tokens[2];
            return new WebServiceOpr(opName, opUrl, opExtra);
        } // if
        
        return null;
        
    } // opFromEncodedString
    
    @GET 
    @Path("get/json/{direction}")
    @Consumes("text/html")
    @Produces("application/json")
    public String getJSON (
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
        List<WebServiceOpr> candidateOps = new ArrayList<WebServiceOpr>();
        
        if (candidates != null) {
            
           StringTokenizer opTokens = new StringTokenizer(candidates, ",");
           
           while (opTokens.hasMoreTokens()) {
               
               String next = opTokens.nextToken();
               WebServiceOpr op = opFromEncodedString(next);
               
               if (op != null) {
                   candidateOps.add(op);
               } // if
           
           } // while
           
        } // if
        
        // A list to hold the workflow operations
        List<WebServiceOpr> workflowOps = new ArrayList<WebServiceOpr>();
        
        if (workflow != null) {
            
           StringTokenizer opTokens = new StringTokenizer(workflow, ",");
           
           while (opTokens.hasMoreTokens()) {
               
               String next = opTokens.nextToken();
               WebServiceOpr op = opFromEncodedString(next);
               
               if (op != null) {
                   workflowOps.add(op);
               } // if
           
           } // while
           
        } // if
        
        // Filter out stuff from the candidates that already exist in the workflow
        List <WebServiceOpr> toDelete = new ArrayList<WebServiceOpr>();
        for (WebServiceOpr opA: candidateOps) {
            for (WebServiceOpr opB: workflowOps) {
                if (opA.compareTo(opB)) {
                    toDelete.add(opA);
                } // if
            } // for
        } // for
        candidateOps.removeAll(toDelete);
        
        ForwardSuggest sugg = new ForwardSuggest();
        List<WebServiceOprScore> suggestOpList = sugg.suggestNextService(workflowOps, candidateOps, desired, this.context.getRealPath("WEB-INF/owl/obi.owl"), null);
        
        DecimalFormat df = new DecimalFormat("0.000");
        
        for (WebServiceOprScore score: suggestOpList){
            String [] suggestion = {
                score.getOperationName(), 
                score.getWsDescriptionDoc(), 
                df.format(score.getScore()),
                df.format(score.getDmScore()),
                df.format(score.getFnScore()),
                df.format(score.getPeScore()),
                score.getExtraInfo()
            };
            suggestions.put(suggestion);
        } // for
        
        // return the JSON array wrapped with the callback function
        if (callback == null) {
            return String.format("%s", callback, suggestions.toString());
        } else {
            return String.format("%s(%s);", callback, suggestions.toString());
        }
        
    } // getSuggestionsJsonp
    
    @POST 
    @Path("get/xml/{direction}")
    @Consumes({"application/xml", "text/xml"})
    @Produces({"application/xml", "text/xml"})
    public ServiceSuggestionResponse getXML (
        @PathParam("direction")   String direction,
        ServiceSuggestionRequest  request
    ) {
        
        List<WebServiceOpr> candidateOps = new ArrayList<WebServiceOpr>();
        for (Operation o : request.candiates) {
            candidateOps.add(new WebServiceOpr(o.operationName, o.service.descriptionDocument));
        }
        
        List<WebServiceOpr> workflowOps  = new ArrayList<WebServiceOpr>();
        for (Operation o : request.workflow) {
            workflowOps.add(new WebServiceOpr(o.operationName, o.service.descriptionDocument));
        }
        
        ForwardSuggest sugg = new ForwardSuggest();
        List<WebServiceOprScore> suggestOpList = sugg.suggestNextService(workflowOps, candidateOps, request.desiredFunctionality, this.context.getRealPath("WEB-INF/owl/obi.owl"), null);
        
        List<SuggestedOperation> operations = new ArrayList<SuggestedOperation>();
        for (WebServiceOprScore o : suggestOpList) {
            
            Service s = new Service();
            s.setDescriptionDocument(o.getWsDescriptionDoc());
            
            SuggestedOperation so = new SuggestedOperation();
            so.setService(s);
            so.setOperationName(o.getOperationName());
            so.setScore(o.getScore());
            so.setDataMediationScore(o.getDmScore());
            so.setFunctionalityScore(o.getFnScore());
            so.setPreconditionEffectScore(o.getPeScore());
            
            operations.add(so);
        }
        
        Collections.sort(operations);
        
        ServiceSuggestionResponse response = new ServiceSuggestionResponse();
        response.setOperations(operations);
        
        return response;
        
    } // getXML
    
} // ServiceSuggestion
