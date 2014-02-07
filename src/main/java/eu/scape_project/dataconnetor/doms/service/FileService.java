package eu.scape_project.dataconnetor.doms.service;

import eu.scape_project.dataconnetor.doms.EntityInterface;
import eu.scape_project.dataconnetor.doms.EntityInterfaceFactory;
import eu.scape_project.dataconnetor.doms.exceptions.CommunicationException;
import eu.scape_project.dataconnetor.doms.exceptions.ConfigurationException;
import eu.scape_project.dataconnetor.doms.exceptions.NotFoundException;
import eu.scape_project.dataconnetor.doms.exceptions.ParsingException;
import eu.scape_project.dataconnetor.doms.exceptions.UnauthorizedException;
import eu.scape_project.model.File;
import eu.scape_project.model.IntellectualEntity;
import eu.scape_project.model.Representation;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@Path("/file")
public class FileService  {

    /**
     * 5.4.8 Retrieve a File
     * For fetching Files associated with Intellectual Entities the implementation exposes
     * a HTTP GET endpoint. Requests sent to this endpoint must have a <id>
     * parameter indicating which File to fetch. The parameter <version-id> indicating
     * the version to fetch is optional and defaults to the most current version of the
     * File. Depending on the Storage Strategy the response body is the binary file with
     * the corresponding Content-Type set by the repository or a HTTP 302 redirect in
     * the case of referenced content.
     * Path:
     * /file/<entity-id>/<representation-id>/<file-id>/<version-id>
     * Method
     * HTTP/1.1 GET
     *
     * @param entityID         the id of the Intellectual Entity
     * @param representationID representation-id: the id of the Representation
     * @param fileID           the id of the File
     * @param versionID        the version of the requested File's parent Intellectual Entity (optional)
     *
     * @return the file requested or a redirect to the file when using referenced content.
     * Content-Type
     * depends on File's metadata, but defaults to application/octet-stream.
     */
    //TODO add support for versions
    @Path("/{entity-id}/{representation-id}/{file-id}/{version-id}")
    public Response retrieve(
            @PathParam("entity-id")
            String entityID,
            @PathParam("representation-id")
            String representationID,
            @PathParam("file-id")
            String fileID,
            @PathParam("version-id")
            String versionID) throws
                              NotFoundException,
                              ConfigurationException,
                              ParsingException,
                              UnauthorizedException,
                              CommunicationException {
        EntityInterface instance = EntityInterfaceFactory.getInstance();
        IntellectualEntity entity = instance.readFromEntityID(entityID, false);
        for (Representation representation : entity.getRepresentations()) {
            if (representation.getIdentifier().getValue().equals(representationID)) {
                for (File file : representation.getFiles()) {
                    if (file.getIdentifier().getValue().equals(fileID)) {
                        return Response.temporaryRedirect(file.getUri()).build();
                    }
                }
                break;
            }
        }
        throw new NotFoundException();


    }
}
