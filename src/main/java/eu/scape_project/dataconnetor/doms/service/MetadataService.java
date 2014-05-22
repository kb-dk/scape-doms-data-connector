package eu.scape_project.dataconnetor.doms.service;

import eu.scape_project.dataconnetor.doms.EntityInterfaceFactory;
import eu.scape_project.dataconnetor.doms.EntityManipulator;
import eu.scape_project.dataconnetor.doms.XmlUtils;
import eu.scape_project.dataconnetor.doms.exceptions.CommunicationException;
import eu.scape_project.dataconnetor.doms.exceptions.ConfigurationException;
import eu.scape_project.dataconnetor.doms.exceptions.NotFoundException;
import eu.scape_project.dataconnetor.doms.exceptions.ParsingException;
import eu.scape_project.dataconnetor.doms.exceptions.UnauthorizedException;
import eu.scape_project.dataconnetor.doms.exceptions.VersioningException;
import eu.scape_project.model.BitStream;
import eu.scape_project.model.File;
import eu.scape_project.model.IntellectualEntity;
import eu.scape_project.model.Representation;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.io.InputStream;

@Path("/metadata")
public class MetadataService extends AbstractService {

    private Response retrieveGeneric(String entityID, String representationID, String fileID, String bitstreamID,
                                     String versionID, String metadataID) throws
                                                                          ParsingException,
                                                                          ConfigurationException,
                                                                          UnauthorizedException,
                                                                          NotFoundException,
                                                                          CommunicationException {

        IntellectualEntity entity = EntityInterfaceFactory.getInstance(getCredentials()).readFromEntityID(entityID, versionID, false);

        if (representationID == null) {
            return Response.ok().entity(XmlUtils.toBytes(entity.getDescriptive())).build();
        }
        for (Representation representation : entity.getRepresentations()) {
            if (representation.getIdentifier().getValue().equals(representationID)) {
                if (fileID == null) {
                    switch (metadataID) {
                        case "techMD":
                            return Response.ok().entity(XmlUtils.toBytes(representation.getTechnical())).build();
                        case "rightsMD":
                            return Response.ok().entity(XmlUtils.toBytes(representation.getRights())).build();
                        case "sourceMD":
                            return Response.ok().entity(XmlUtils.toBytes(representation.getSource())).build();
                        case "digiprovMD":
                            return Response.ok().entity(XmlUtils.toBytes(representation.getProvenance())).build();
                        default:
                            throw new NotFoundException();
                    }
                }
                for (File file : representation.getFiles()) {
                    if (file.getIdentifier().getValue().equals(fileID)) {
                        if (bitstreamID == null) {
                            return Response.ok().entity(XmlUtils.toBytes(file.getTechnical())).build();
                        }
                        for (BitStream bitStream : file.getBitStreams()) {
                            if (bitStream.getIdentifier().getValue().equals(bitstreamID)) {
                                return Response.ok().entity(XmlUtils.toBytes(file.getTechnical())).build();
                            }
                        }
                    }
                }
                break;
            }
        }
        throw new NotFoundException();

    }


    /**
     * 5.4.2 Retrieve a metadata record
     * Retrieval of single metadata records of entities is done via a GET request. Since
     * Intellectual Entities can have multiple versions there is an optional version
     * identifier, which when omitted defaults to the most current version of the
     * Intellectual Entity. When successful the response body is a XML representation of
     * the corresponding metadata record.
     * Path:
     * /metadata/<entity-id>/<rep-id>/<file-id>/<bitstream-id>/<version-id>/<md-id>
     * Method:
     * HTTP/1.1 GET
     * Parameters:
     *
     * @param entityID         entity-id: the id of the Intellectual Entity
     * @param representationID the id of the Representation (optional)
     * @param fileID           the id of the File (optional)
     * @param bitstreamID      the id of the requested binary content (optional)
     * @param versionID        the version of the requested bit stream's parent Intellectual Entity (optional)
     * @param metadataID       the id of the metadata to retrieve
     *
     * @return A XML representation of the requested metadata record according to the
     * corresponding metadata’s schema
     * Content-Type:
     * text/xml
     */
    @Path("/{entity-id}/{representation-id}/{file-id}/{bitstream-id}{version-id:(/[^/]+?)?}/{md-id}")
    public Response retrieveBitstream(
            @PathParam("entity-id")
            String entityID,
            @PathParam("representation-id")
            String representationID,
            @PathParam("file-id")
            String fileID,
            @PathParam("bitstream-id")
            String bitstreamID,
            @PathParam("version-id")
            String versionID,
            @PathParam("md-id")
            String metadataID) throws
                               UnauthorizedException,
                               NotFoundException,
                               ParsingException,
                               CommunicationException,
                               ConfigurationException {
        return retrieveGeneric(entityID, representationID, fileID, bitstreamID, versionID, metadataID);

    }

    @Path("/{entity-id}/{representation-id}/{file-id}{version-id:(/[^/]+?)?}/{md-id}")
    public Response retrieveFile(
            @PathParam("entity-id")
            String entityID,
            @PathParam("representation-id")
            String representationID,
            @PathParam("file-id")
            String fileID,
            @PathParam("version-id")
            String versionID,
            @PathParam("md-id")
            String metadataID) throws
                               UnauthorizedException,
                               NotFoundException,
                               ParsingException,
                               CommunicationException,
                               ConfigurationException {
        return retrieveGeneric(entityID, representationID, fileID, null, versionID, metadataID);
    }

    @Path("/{entity-id}/{representation-id}/{version-id}/{md-id}")
    public Response retrieveRepresentation(
            @PathParam("entity-id")
            String entityID,
            @PathParam("representation-id")
            String representationID,
            @PathParam("version-id")
            String versionID,
            @PathParam("md-id")
            String metadataID) throws
                               UnauthorizedException,
                               NotFoundException,
                               ParsingException,
                               CommunicationException,
                               ConfigurationException {
        return retrieveGeneric(entityID, representationID, null, null, versionID, metadataID);

    }

    @Path("/{entity-id}{version-id:(/[^/]+?)?}/{md-id}")
    public Response retrieveEntity(
            @PathParam("entity-id")
            String entityID,
            @PathParam("version-id")
            String versionID,
            @PathParam("md-id")
            String metadataID) throws
                               UnauthorizedException,
                               NotFoundException,
                               ParsingException,
                               CommunicationException,
                               ConfigurationException {
        return retrieveGeneric(entityID, versionID, null, null, null, metadataID);
    }


    /**
     * 5.4.16
     * Update the metadata of an Intellectual Entity
     * When updating only the metadata of an Intellectual Entity validity on binary files
     * can be omitted, thereby saving cpu cycles. An endpoint is exposed to clients for
     * updating the metadata of an Intellectual entity, that consumes a METS
     * representations of an Intellectual Entity.
     * Path:
     * /metadata/<entity-id>/<metadata-id>
     * Method
     * HTTP/1.1 PUT
     *
     * @param entityID   the id of the Intellectual Entity to update
     * @param metadataID the Id of the metadata set to update
     * @param contents   A metadata set's XML representation.
     *
     * @return
     */
    @Path("/{entity-id}/{md-id}")
    @PUT
    public Response updateEntity(
            @PathParam("entity-id")
            String entityID,
            @PathParam("md-id")
            String metadataID, InputStream contents) throws
                                                     ConfigurationException,
                                                     UnauthorizedException,
                                                     ParsingException,
                                                     CommunicationException,
                                                     NotFoundException, VersioningException {
        EntityManipulator instance = EntityInterfaceFactory.getInstance(getCredentials());
        IntellectualEntity entity = instance.readFromEntityID(entityID, null, false);
        IntellectualEntity newEntity = new IntellectualEntity.Builder(entity).descriptive(XmlUtils.toObject(contents))
                                                                             .build();
        instance.updateFromEntityID(entityID, newEntity);
        return Response.ok().build();

    }
}
