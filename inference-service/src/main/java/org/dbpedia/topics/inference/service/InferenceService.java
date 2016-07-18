package org.dbpedia.topics.inference.service;

import org.dbpedia.topics.inference.Inferencer;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;

/**
 * Root resource (exposed at "get-topics" path)
 */
@Path("inference-service")
public class InferenceService {
    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @POST
    @Path("get-topics")
    @Produces(MediaType.TEXT_PLAIN)
    public Response predict(@FormParam("text") String text) {
        double[] prediction = Inferencer.predictTopicCoverage(text);
        return Response.status(200).entity(Arrays.toString(prediction)).header("Access-Control-Allow-Origin", "*").build();
    }

    @GET
    @Path("get-graph/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGraph(@PathParam("id") Integer id) {
        String graph = "";
        return Response.status(200).entity(graph).header("Access-Control-Allow-Origin", "*").build();
    }
}
