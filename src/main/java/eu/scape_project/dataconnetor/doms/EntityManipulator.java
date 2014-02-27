package eu.scape_project.dataconnetor.doms;

import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import dk.statsbiblioteket.doms.central.connectors.fedora.structures.DatastreamProfile;
import dk.statsbiblioteket.doms.central.connectors.fedora.structures.ObjectProfile;
import dk.statsbiblioteket.util.Bytes;
import dk.statsbiblioteket.util.Checksums;
import eu.scape_project.dataconnetor.doms.exceptions.AlreadyExistsException;
import eu.scape_project.dataconnetor.doms.exceptions.CommunicationException;
import eu.scape_project.dataconnetor.doms.exceptions.NotFoundException;
import eu.scape_project.dataconnetor.doms.exceptions.ParsingException;
import eu.scape_project.dataconnetor.doms.exceptions.UnauthorizedException;
import eu.scape_project.dataconnetor.doms.exceptions.VersioningException;
import eu.scape_project.model.File;
import eu.scape_project.model.Identifier;
import eu.scape_project.model.IntellectualEntity;
import eu.scape_project.model.LifecycleState;
import eu.scape_project.model.Representation;
import eu.scape_project.model.TechnicalMetadata;
import eu.scape_project.model.TechnicalMetadataList;
import versions.VersionType;
import versions.VersionsType;

import javax.xml.bind.JAXBException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class EntityManipulator {


    public static final String HASMODEL = "info:fedora/fedora-system:def/model#hasModel";
    public static final String SCAPE_VERSIONS = "SCAPE_VERSIONS";
    private List<String> collections;
    private EnhancedFedora enhancedFedora;
    private String scape_content_model;


    public EntityManipulator(List<String> collections, EnhancedFedora enhancedFedora, String scape_content_model) throws
                                                                                                                  JAXBException {
        this.collections = collections;
        this.enhancedFedora = enhancedFedora;
        this.scape_content_model = scape_content_model;
    }

    /**
     * This method reads an object as an intellectual entity and returns this
     *
     * @param pid        the pid of the object
     * @param versionID
     * @param references
     *
     * @return the parsed entity
     * @throws BackendInvalidResourceException
     * @throws BackendInvalidCredsException
     * @throws BackendMethodFailedException
     * @throws eu.scape_project.dataconnetor.doms.exceptions.ParsingException
     * @throws JAXBException
     * @throws PIDGeneratorException
     * @throws MalformedURLException
     */
    IntellectualEntity read(String pid, String versionID, boolean references) throws
                                                                              CommunicationException,
                                                                              UnauthorizedException,
                                                                              NotFoundException,
                                                                              ParsingException {
        EnhancedFedora fedora = getEnhancedFedora();

        List<String> identifiers = null;
        try {

            Long timestamp = null;
            if (versionID != null) {
                try {
                    String scapeVersions = fedora.getXMLDatastreamContents(pid, SCAPE_VERSIONS);
                    timestamp = VersionUtils.findVersionInScapeVersions(scapeVersions, versionID);
                } catch (BackendInvalidResourceException e) {

                }
            }

            identifiers = TypeUtils.getDCIdentifiers(fedora, pid, "scape");

            ObjectProfile profile = fedora.getObjectProfile(pid, timestamp);
            DSCompositeModel model = getDsCompositeModel(fedora, profile.getContentModels(),timestamp);

            //Build the entity
            IntellectualEntity.Builder builder = new IntellectualEntity.Builder();

            String entityIdentifier = TypeUtils.pickEntityIdentifier(identifiers);
            if (entityIdentifier == null){
                entityIdentifier = pid;
            }
            builder.identifier(new Identifier(entityIdentifier));
            builder.descriptive(getIfExists(pid, fedora, profile, model.getDescriptive(),timestamp));
            builder.lifecycleState((LifecycleState) getIfExists(pid, fedora, profile, model.getLifeCycle(),timestamp));
            builder.versionNumber(intOrNull(versionID));

            /*
            Versions will be handled as a separate datastream. It will have hold the
            version "number" along with the timestamp, so that we can request the fedora object with the correct
            timestamp. This does require that EVERYTHING on the fedora object is versioned
             */


            //Build the representation
            Representation.Builder rep_builder = new Representation.Builder();
            String representationIdentifier = TypeUtils.pickRepresentationIdentifier(identifiers);
            if (representationIdentifier == null){
                representationIdentifier = pid;
            }
            rep_builder.identifier(new Identifier(representationIdentifier));
            for (String repTechDatastream : model.getRepresentationTechnical()) {
                rep_builder.technical(repTechDatastream, getIfExists(pid, fedora, profile, repTechDatastream,timestamp));
            }

            rep_builder.rights(getIfExists(pid, fedora, profile, model.getRights(),timestamp));
            rep_builder.source(getIfExists(pid, fedora, profile, model.getSource(),timestamp));
            rep_builder.provenance(getIfExists(pid, fedora, profile, model.getProvenance(),timestamp));
            rep_builder.title(profile.getLabel());

            //Build the File
            File.Builder file_builder = new File.Builder();
            String fileIdentifier = TypeUtils.pickFileIdentifier(identifiers);
            if (fileIdentifier == null){
                fileIdentifier = pid;
            }
            file_builder.identifier(new Identifier(fileIdentifier));
            for (String fileTechDatastream : model.getFileTechnical()) {
                file_builder.technical(fileTechDatastream, getIfExists(pid, fedora, profile, fileTechDatastream,timestamp));
            }

            String contentDatastreamName = model.getFileContent();
            for (DatastreamProfile datastreamProfile : profile.getDatastreams()) {
                if (datastreamProfile.getID().equals(contentDatastreamName)) {
                    file_builder.filename(datastreamProfile.getLabel());
                    file_builder.mimetype(datastreamProfile.getMimeType());
                    file_builder.uri(URI.create(datastreamProfile.getUrl()));
                    break;
                }
            }
            //Build the bitstreams
            rep_builder.files(Arrays.asList(file_builder.build()));
            builder.representations(Arrays.asList(rep_builder.build()));
            return builder.build();
        } catch (BackendMethodFailedException e) {
            throw new CommunicationException(e);
        } catch (BackendInvalidResourceException e) {
            throw new NotFoundException(e);
        } catch (BackendInvalidCredsException e) {
            throw new UnauthorizedException(e);
        }
    }

    private Integer intOrNull(String versionID) {
        if (versionID != null){
            try {
            return Integer.parseInt(versionID);
            } catch (NumberFormatException e){
                return null;
            }
        }
        return 1;
    }

    private Object getIfExists(String pid, EnhancedFedora fedora, ObjectProfile profile, String datastream, Long timestamp) throws
                                                                                                            BackendInvalidCredsException,
                                                                                                            BackendMethodFailedException,
                                                                                                            ParsingException {
        try {
            boolean get = false;
            for (DatastreamProfile datastreamProfile : profile.getDatastreams()) {
                if (datastreamProfile.getID().equals(datastream)) {
                    get = true;
                    break;
                }
            }
            if (get) {
                String rights = fedora.getXMLDatastreamContents(pid, datastream, timestamp);
                return XmlUtils.toObject((rights));
            } else {
                return null;
            }
        } catch (BackendInvalidResourceException e) {
            return null;
        }
    }


    /**
     * Create a new entity in doms from the given entity
     *
     * @param entity the entity to persist
     *
     * @return the pid of the new entity
     * @throws JAXBException
     * @throws PIDGeneratorException
     * @throws MalformedURLException
     * @throws eu.scape_project.dataconnetor.doms.exceptions.AlreadyExistsException
     * @throws ParsingException
     * @throws eu.scape_project.dataconnetor.doms.exceptions.UnauthorizedException
     * @throws eu.scape_project.dataconnetor.doms.exceptions.CommunicationException
     */
    public String createNew(IntellectualEntity entity) throws
                                                       CommunicationException,
                                                       AlreadyExistsException,
                                                       ParsingException,
                                                       UnauthorizedException {
        try {
            try {
                getPids(entity.getIdentifier().getValue());
                throw new AlreadyExistsException(
                        "An entity with id '" + entity.getIdentifier()
                                                      .getValue() + "' already exists");
            } catch (NotFoundException e) {
                return createOrUpdate(null, entity);
            }
        } catch (NotFoundException e) {
            throw new CommunicationException(e);//This should not be possible, so it means something else is broken
        }
    }

    private Object findContents(String representationTechnicalDatastream, TechnicalMetadataList technical) {
        for (TechnicalMetadata technicalMetadata : technical.getContent()) {
            if (technicalMetadata.getId().equals(representationTechnicalDatastream)) {
                return technicalMetadata.getContents();
            }
        }
        return null;
    }


    /**
     * Create a new entity in doms (pid == null) or update an entity already persisted in doms (pid != null)
     *
     * @param pid
     * @param entity
     */
    private String createOrUpdate(String pid, IntellectualEntity entity) throws
                                                                         CommunicationException,
                                                                         NotFoundException,
                                                                         ParsingException,
                                                                         UnauthorizedException {
        EnhancedFedora fedora = getEnhancedFedora();

        try {
            String logmessage = "logmessage";

            List<String> scapeIdentifiers = TypeUtils.formatIdentifiers(entity);

            ObjectProfile profile;
            DSCompositeModel model;
            if (pid == null) {
                //We have a new object here, with the identifiers
                pid = fedora.newEmptyObject(scapeIdentifiers, getCollections(), logmessage);
                //add the content models
                String contentModel = EqualUtils.longForm(scape_content_model);
                fedora.addRelation(
                        pid, EqualUtils.longForm(pid), HASMODEL, contentModel, false, logmessage);

                profile = null;
                model = getDsCompositeModel(fedora, Arrays.asList(contentModel), null);

            } else {
                profile = fedora.getObjectProfile(pid, null);
                setIdentifiers(pid, scapeIdentifiers, fedora);
                model = getDsCompositeModel(fedora, profile.getContentModels(), null);

            }

            //TODO version number

            changeIfNeeded(pid, fedora, logmessage, profile, model.getLifeCycle(), entity.getLifecycleState());
            changeIfNeeded(pid, fedora, logmessage, profile, model.getDescriptive(), entity.getDescriptive());

            for (Representation representation : entity.getRepresentations()) {
                changeIfNeeded(pid, fedora, logmessage, profile, model.getProvenance(), representation.getProvenance());
                changeIfNeeded(pid, fedora, logmessage, profile, model.getRights(), representation.getRights());
                changeIfNeeded(pid, fedora, logmessage, profile, model.getSource(), representation.getSource());

                for (String representationTechnicalDatastream : model.getRepresentationTechnical()) {
                    Object contents = findContents(representationTechnicalDatastream, representation.getTechnical());
                    changeIfNeeded(pid, fedora, logmessage, profile, representationTechnicalDatastream, contents);

                }
                if (representation.getTitle() != null) {
                    fedora.modifyObjectLabel(pid, representation.getTitle(), logmessage);
                } else {
                    //TODO remove label
                }
                for (File file : representation.getFiles()) {
                    for (String fileTechnicalMetadata : model.getFileTechnical()) {
                        Object contents = findContents(fileTechnicalMetadata, file.getTechnical());
                        changeIfNeeded(pid, fedora, logmessage, profile, fileTechnicalMetadata, contents);
                    }
                    if (file.getFilename() != null && file.getUri() != null && file.getMimetype() != null) {
                        fedora.addExternalDatastream(
                                pid,
                                model.getFileContent(),
                                file.getFilename(),
                                file.getUri().toString(),
                                "unknown",
                                file.getMimetype(),
                                null,
                                logmessage);
                    }

                }

            }

            updateVersion(pid,fedora,logmessage);
            return pid;
        } catch (BackendInvalidCredsException e) {
            throw new UnauthorizedException(e);

        } catch (BackendInvalidResourceException | BackendMethodFailedException | PIDGeneratorException e) {
            throw new CommunicationException(e);
        }

    }

    private void updateVersion(String pid, EnhancedFedora fedora, String logMessage) throws
                                                                                     BackendInvalidCredsException,
                                                                                     BackendMethodFailedException,
                                                                                     CommunicationException,
                                                                                     BackendInvalidResourceException {

        VersionsType versions = VersionUtils.getVersions(pid, fedora);
        VersionType version = new VersionType();
        Integer id = getHighestID(versions);
        version.setId(id+1);
        version.setTimestamp(new Date().getTime());
        versions.getVersion().add(version);
        VersionUtils.setVersions(pid, fedora, logMessage, versions);
    }

    private Integer getHighestID(VersionsType versions) {
        Integer result = Integer.MIN_VALUE;
        for (VersionType versionType : versions.getVersion()) {
            if (versionType.getId().compareTo(result) >= 0){
                result = versionType.getId();
            }
        }
        if (result.equals(Integer.MIN_VALUE)){
            return 0;
        } else {
            return result;
        }
    }


    private void changeIfNeeded(String pid, EnhancedFedora fedora, String logmessage, ObjectProfile profile,
                                String datastream, Object content) throws
                                                                   ParsingException,
                                                                   BackendMethodFailedException,
                                                                   BackendInvalidResourceException,
                                                                   BackendInvalidCredsException {

        if (content != null) {

            String contentString = XmlUtils.toString(content);
            String md5sum = Bytes.toHex(Checksums.md5(contentString));

            boolean toWrite = true;
            if (profile != null) {
                for (DatastreamProfile datastreamProfile : profile.getDatastreams()) {
                    if (datastreamProfile.getID().equals(datastream)) {
                        if (md5sum.equalsIgnoreCase(datastreamProfile.getChecksum())) {
                            toWrite = false;
                            break;
                        }
                    }
                }
            }
            if (toWrite) {
                fedora.modifyDatastreamByValue(
                        pid, datastream, contentString, md5sum, null, logmessage);
            }
        } else {
            try {
                fedora.deleteDatastream(pid, datastream, logmessage);
            } catch (BackendInvalidResourceException e) {
                //ignore, the datastream does not exist
            }
        }
    }

    private DSCompositeModel getDsCompositeModel(EnhancedFedora fedora, List<String> contentModels, Long timestamp) throws
                                                                                                    BackendMethodFailedException,
                                                                                                    BackendInvalidResourceException,
                                                                                                    BackendInvalidCredsException {
        DSCompositeModel model = new DSCompositeModel();
        for (String contentModel : contentModels) {
            model.merge(new DSCompositeModel(EqualUtils.shortForm(contentModel), fedora,timestamp));
        }
        return model;
    }


    private void setIdentifiers(String pid, List<String> scapeIdentifiers, EnhancedFedora fedora) {
        //TODO
    }


    private List<String> getCollections() {
        return collections;
    }

    private EnhancedFedora getEnhancedFedora() {
        return enhancedFedora;
    }

    public IntellectualEntity readFromEntityID(String entityID, String versionID, boolean references) throws
                                                                                                      NotFoundException,
                                                                                                      CommunicationException,
                                                                                                      UnauthorizedException,
                                                                                                      ParsingException {
        List<String> pids = null;
        //If you renamed the entity, you are fucked
        pids = getPids(entityID);
        return read(pids.get(0), versionID, references);
    }

    public long updateFromEntityID(String entityID, IntellectualEntity entity) throws
                                                                               NotFoundException,
                                                                               CommunicationException,
                                                                               UnauthorizedException,
                                                                               ParsingException, VersioningException {
        List<String> pids = null;
        pids = getPids(entityID);
        createOrUpdate(pids.get(0), entity);
        return entity.getVersionNumber();
    }

    private List<String> getPids(String entityID) throws
                                                  UnauthorizedException,
                                                  CommunicationException,
                                                  NotFoundException {
        if (entityID.startsWith("uuid:")){
            return Arrays.asList(entityID);
        }
        List<String> pids;
        try {
            pids = getEnhancedFedora().findObjectFromDCIdentifier(
                    TypeUtils.formatEntityIdentifier(
                            new Identifier(
                                    entityID)));
        } catch (BackendInvalidCredsException e) {
            throw new UnauthorizedException(e);
        } catch (BackendMethodFailedException e) {
            throw new CommunicationException(e);
        }
        if (pids.size() == 0) {
            throw new NotFoundException();
        }
        return pids;
    }

}
