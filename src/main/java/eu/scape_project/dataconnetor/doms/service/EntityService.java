package eu.scape_project.dataconnetor.doms.service;

import eu.scape_project.dataconnetor.doms.EntityInterfaceFactory;
import eu.scape_project.dataconnetor.doms.XmlUtils;
import eu.scape_project.dataconnetor.doms.exceptions.AlreadyExistsException;
import eu.scape_project.dataconnetor.doms.exceptions.CommunicationException;
import eu.scape_project.dataconnetor.doms.exceptions.ConfigurationException;
import eu.scape_project.dataconnetor.doms.exceptions.NotFoundException;
import eu.scape_project.dataconnetor.doms.exceptions.ParsingException;
import eu.scape_project.dataconnetor.doms.exceptions.UnauthorizedException;
import eu.scape_project.model.IntellectualEntity;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;

@Path("/entity")
public class EntityService {

    //TODO add support for versions


    /**
     * 5.4.1 Retrieve an Intellectual Entity
     * Retrieval of entities is done via a GET request. Since Intellectual Entities can have
     * multiple versions there is an optional version identifier, which when omitted
     * defaults to the most current version of the Intellectual Entity. When successful
     * the response body is a METS representation of the Intellectual Entity. The
     * parameter useReferences controls wether the response is created using
     * references to the metadata via <mdRef> elements or if the metadata should be
     * wrapped inside <mdWrap> elements in the METS document.
     * Path:
     * /entity/<entity-id>/<version-id>?useReferences=[yes|no]
     * Method:
     * HTTP/1.1 GET
     *
     * @param entityID      the id of the requested Intellectual Entity
     * @param versionID     the version of the requested entity (optional)
     * @param useReferences Wether to wrap metadata inside <mdWrap> elements or to reference the metadata using<mdref>
     *                      elements. Defaults to yes.
     *
     * @return A XML representation of the requested Intellectual Entity version
     * Content-Type:
     * text/xml
     */
    @Path("/{entity-id}/{version-id}")
    @Produces(MediaType.APPLICATION_XML)
    public Response retrieve(
            @PathParam("entity-id")
            String entityID,
            @PathParam("version-id")
            String versionID,
            @QueryParam("useReferences")
            String useReferences) throws
                                  ConfigurationException,
                                  NotFoundException,
                                  ParsingException,
                                  CommunicationException,
                                  UnauthorizedException {

        IntellectualEntity entity = null;

        boolean references = toBoolean(useReferences);

        entity = EntityInterfaceFactory.getInstance().readFromEntityID(entityID, references);

        InputStream bytes = XmlUtils.toBytes(entity, references);
        return Response.ok(bytes, MediaType.TEXT_XML_TYPE).build();
    }

    private boolean toBoolean(String useReferences) {
        if (useReferences != null && !useReferences.equalsIgnoreCase("yes")) {
            return false;
        }
        return true;
    }


    /**
     * 5.4.4 Ingest an Intellectual Entity
     * Ingestion of digital objects is done by sending a METS representation of an
     * Intellectual Entity in the body of a HTTP POST request, which gets validated and
     * persisted in the repository. If validation does not succeed the implementation
     * returns a HTTP 415 “Unsupported Media Type” status message. When successful
     * the response body is a plain text document consisting of the ingested entity's
     * identifier.
     * Path
     * /entity
     * Method
     * HTTP/1.1 POST
     * Content-Type
     * text/plain
     *
     * @param ingestXml A XML representation of the entity
     *
     * @return The Intellectual Entity identifier
     */
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.TEXT_PLAIN)
    public Response ingest(InputStream ingestXml) throws
                                                  ConfigurationException,
                                                  ParsingException,
                                                  CommunicationException,
                                                  UnauthorizedException,
                                                  AlreadyExistsException {
        IntellectualEntity intellectualEntity = XmlUtils.toEntity(ingestXml);
        EntityInterfaceFactory.getInstance().createNew(intellectualEntity);
        return Response.ok().entity(intellectualEntity.getIdentifier().getValue()).build();
    }


    /**
     * 5.4.6 Update an Intellectual Entity
     * In order to allow updating of Intellectual Entities the implementation exposes this
     * HTTP PUT endpoint. The mandatory parameter <id> tells the repository which
     * Intellectual Entity is to be updated. The request must include the updated METS
     * representation of the entity in the request body.
     * Path:
     * /entity/<id>
     *
     * @param entityID  the id of the Intellectual Entity to update
     * @param entityXml A digital object's XML representation.
     *
     * @return
     */
    @Path("/{entity-id}")
    @PUT
    @Consumes(MediaType.APPLICATION_XML)
    public Response update(
            @PathParam("entity-id")
            String entityID, InputStream entityXml) throws
                                                    ConfigurationException,
                                                    ParsingException,
                                                    UnauthorizedException,
                                                    NotFoundException,
                                                    CommunicationException {
        EntityInterfaceFactory.getInstance().updateFromEntityID(entityID, XmlUtils.toEntity(entityXml));
        return Response.ok().build();
    }


    @GET
    @Path("/{entity-id}")
    public Response retrieveNewest(
            @PathParam("entity-id")
            String entityID,
            @DefaultValue("no")
            @QueryParam("useReferences")
            String useReferences) throws
                                  NotFoundException,
                                  UnauthorizedException,
                                  ParsingException,
                                  CommunicationException,
                                  ConfigurationException {
        return retrieve(entityID, null, useReferences);
    }

}
