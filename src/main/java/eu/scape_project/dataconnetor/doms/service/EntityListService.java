package eu.scape_project.dataconnetor.doms.service;

import com.sun.jersey.multipart.BodyPart;
import com.sun.jersey.multipart.MultiPart;
import eu.scape_project.dataconnetor.doms.EntityInterface;
import eu.scape_project.dataconnetor.doms.EntityInterfaceFactory;
import eu.scape_project.dataconnetor.doms.XmlUtils;
import eu.scape_project.dataconnetor.doms.exceptions.CommunicationException;
import eu.scape_project.dataconnetor.doms.exceptions.ConfigurationException;
import eu.scape_project.dataconnetor.doms.exceptions.NotFoundException;
import eu.scape_project.dataconnetor.doms.exceptions.ParsingException;
import eu.scape_project.dataconnetor.doms.exceptions.UnauthorizedException;
import eu.scape_project.model.IntellectualEntity;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Path("/entity-list")
public class EntityListService {

    /*
    5.4.3 Retrieve a set of Intellectual Entities
    In order to make fetching a whole set of entities feasible this GET method
    consumes a list of URIs sent with the request. It resolves the URIs to Intellectual
    Entities and creates a response consisting of the corresponding METS
    representations. If at least one URI could not be resolved the implementation
    returns a HTTP 404 Not Found status message.
    Path:
    /entity-list
    Method:
    HTTP/1.1 POST
    Consumes:
    A text/uri-list of the entities to be retrieved
    Produces:
    METS representations of the requested entities.
    Content-Type:
    multipart
     */
    @POST
    @Consumes("text/uri-list")
    @Produces(com.sun.jersey.multipart.MultiPartMediaTypes.MULTIPART_MIXED)
    public Response retrieve(InputStream entityUriList) throws
                                                        ConfigurationException,
                                                        UnauthorizedException,
                                                        ParsingException,
                                                        CommunicationException,
                                                        NotFoundException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(entityUriList));
        String line;
        EntityInterface entities = EntityInterfaceFactory.getInstance();

        MultiPart multiPartEntity = new MultiPart();
        try {
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                IntellectualEntity entity = entities.readFromEntityID(line, true);
                multiPartEntity.bodyPart(new BodyPart(XmlUtils.toString(entity),MediaType.APPLICATION_XML_TYPE));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Response.ok().entity(multiPartEntity.getEntity()).build();
    }
}
