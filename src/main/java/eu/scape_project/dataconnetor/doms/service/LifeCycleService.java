package eu.scape_project.dataconnetor.doms.service;

import eu.scape_project.dataconnetor.doms.EntityManipulator;
import eu.scape_project.dataconnetor.doms.EntityInterfaceFactory;
import eu.scape_project.dataconnetor.doms.XmlUtils;
import eu.scape_project.dataconnetor.doms.exceptions.CommunicationException;
import eu.scape_project.dataconnetor.doms.exceptions.ConfigurationException;
import eu.scape_project.dataconnetor.doms.exceptions.NotFoundException;
import eu.scape_project.dataconnetor.doms.exceptions.ParsingException;
import eu.scape_project.dataconnetor.doms.exceptions.UnauthorizedException;
import eu.scape_project.model.IntellectualEntity;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@Path("/lifecycle/")
public class LifeCycleService  {
    /**
     * 5.4.13
     * Retrieve the lifecycle status of an entity
     * In order to access the life cycle state of an Intellectual Entity without having to
     * fetch the whole METS representation an endpoint for retrieving this significant
     * property is exposed by the repository.
     * Path
     * /lifecycle/<entity-id>
     * Method
     * HTTP/1.1 GET
     * Parameters
     * entity-id: the id of the Intellectual Entity to update
     * Produces
     * A XML representation of the lifecycle status
     * Content-Type
     * text/xml
     */
    @GET
    @Path("/{entity-id}")
    public Response retrieve(
            @PathParam("entity-id")
            String entityID) throws
                             UnauthorizedException,
                             ParsingException,
                             CommunicationException,
                             NotFoundException,
                             ConfigurationException {
        EntityManipulator instance = EntityInterfaceFactory.getInstance();
        IntellectualEntity entity = instance.readFromEntityID(entityID, null, false);

        return Response.ok().entity(XmlUtils.toString(entity.getLifecycleState())).build();
    }

}