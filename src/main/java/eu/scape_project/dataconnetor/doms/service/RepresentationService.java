package eu.scape_project.dataconnetor.doms.service;

import eu.scape_project.dataconnetor.doms.EntityManipulator;
import eu.scape_project.dataconnetor.doms.EntityInterfaceFactory;
import eu.scape_project.dataconnetor.doms.XmlUtils;
import eu.scape_project.dataconnetor.doms.exceptions.CommunicationException;
import eu.scape_project.dataconnetor.doms.exceptions.ConfigurationException;
import eu.scape_project.dataconnetor.doms.exceptions.NotFoundException;
import eu.scape_project.dataconnetor.doms.exceptions.ParsingException;
import eu.scape_project.dataconnetor.doms.exceptions.UnauthorizedException;
import eu.scape_project.dataconnetor.doms.exceptions.VersioningException;
import eu.scape_project.model.IntellectualEntity;
import eu.scape_project.model.Representation;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.List;

@Path("/representation")
public class RepresentationService  {

    //TODO where are the version ids?



    /**
     * 5.4.14
     * Retrieve a Representation
     * For fetching Representations without having to retrieve the METS representation
     * of the whole Intellectual Entity a dedicated endpoint is exposed by the repository
     * Path:
     * /representation/<entity-id>/<representation-id>
     * Method:
     * HTTP/1.1 GET
     * Content-Type:
     * text/xml
     *
     * @param entityID         the id of the Intellectual Entity to update
     * @param representationID the id of the Representation to update
     *
     * @return A XML representation of the requested Representation
     */
    @Path("/{entity-id}/{representation-id}")
    @GET
    public Response retrieve(
            @PathParam("entity-id")
            String entityID,
            @PathParam("representation-id")
            String representationID) throws
                                     ConfigurationException,
                                     UnauthorizedException,
                                     ParsingException,
                                     CommunicationException,
                                     NotFoundException {
        EntityManipulator instance = EntityInterfaceFactory.getInstance();
        IntellectualEntity entity = instance.readFromEntityID(entityID, null, false);

        for (Representation representation : entity.getRepresentations()) {
            if (representation.getIdentifier().getValue().equals(representationID)) {
                return Response.ok().entity(XmlUtils.toBytes(representation)).build();
            }
        }
        throw new NotFoundException();
    }

    /**
     * 5.4.15
     Update a Representation of an Intellectual Entity
     For updating a Representation of an Intellectual entity without sending a METS
     representation of the Intellectual Entity an endpoint is exposed by the repository.
     The repository has to create a new Version of the Intellectual Entity with the
     updated Representation.
     Path:
     /representation/<entity-id>/<representation-id>
     Method
     HTTP/1.1 PUT
     Consumes
     A Representations' XML representation.
     * @param entityID the id of the Intellectual Entity to update
     * @param representationID the id of the Representation to update
     * @param contents
     * @return
     */
    @Path("/{entity-id}/{representation-id}")
    @PUT
    public Response update(
            @PathParam("entity-id")
            String entityID,
            @PathParam("representation-id")
            String representationID, InputStream contents) throws
                                                           ConfigurationException,
                                                           UnauthorizedException,
                                                           ParsingException,
                                                           CommunicationException,
                                                           NotFoundException, VersioningException {
        EntityManipulator instance = EntityInterfaceFactory.getInstance();
        IntellectualEntity entity = instance.readFromEntityID(entityID, null, false);
        List<Representation> representations = entity.getRepresentations();
        for (int i = 0; i < representations.size(); i++) {
            Representation representation = representations.get(i);
            if (representation.getIdentifier().getValue().equals(representationID)) {
                representations.add(i, XmlUtils.toRepresentation(contents));
                instance.updateFromEntityID(entityID,entity);
                return Response.ok().build();
            }
        }

        throw new NotFoundException();
    }


}
