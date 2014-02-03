package eu.scape_project.dataconnetor.doms;

import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import dk.statsbiblioteket.doms.central.connectors.fedora.structures.DatastreamProfile;
import dk.statsbiblioteket.doms.central.connectors.fedora.structures.ObjectProfile;
import eu.scape_project.dataconnetor.doms.exceptions.AlreadyExistsException;
import eu.scape_project.dataconnetor.doms.exceptions.CommunicationException;
import eu.scape_project.dataconnetor.doms.exceptions.NotFoundException;
import eu.scape_project.dataconnetor.doms.exceptions.ParsingException;
import eu.scape_project.dataconnetor.doms.exceptions.UnauthorizedException;
import eu.scape_project.model.File;
import eu.scape_project.model.Identifier;
import eu.scape_project.model.IntellectualEntity;
import eu.scape_project.model.LifecycleState;
import eu.scape_project.model.Representation;

import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

public class EntityInterface {


    public static final String HASMODEL = "hasModelPredicate";
    private List<String> collections;
    private EnhancedFedora enhancedFedora;
    private String scape_content_model;


    public EntityInterface(List<String> collections, EnhancedFedora enhancedFedora, String scape_content_model) {
        this.collections = collections;
        this.enhancedFedora = enhancedFedora;
        this.scape_content_model = scape_content_model;
    }

    /**
     * This method reads an object as an intellectual entity and returns this
     *
     * @param pid the pid of the object
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
    public IntellectualEntity read(String pid) throws
                                               CommunicationException,
                                               UnauthorizedException,
                                               NotFoundException,
                                               ParsingException {
        EnhancedFedora fedora = getEnhancedFedora();

        List<String> identifiers = null;
        try {
            identifiers = TypeUtils.getDCIdentifiers(fedora, pid, "scape");

            ObjectProfile profile = null;

            profile = fedora.getObjectProfile(pid, null);


            DSCompositeModel model = new DSCompositeModel();
            for (String contentModel : profile.getContentModels()) {
                model.merge(new DSCompositeModel(contentModel.replace("info:fedora/",""), fedora));
            }

            //Build the entity
            IntellectualEntity.Builder builder = new IntellectualEntity.Builder();
            String descriptiveContent = fedora.getXMLDatastreamContents(pid, model.getDescriptive(), null);
            builder.descriptive(XmlUtils.toObject(stream(descriptiveContent)));
            builder.identifier(new Identifier(TypeUtils.pickEntityIdentifier(identifiers)));

            String lifeCycleContents = fedora.getXMLDatastreamContents(pid, model.getLifeCycle(), null);
            LifecycleState o = XmlUtils.toObject(stream(lifeCycleContents));
            builder.lifecycleState(o);
            //TODO version number


            //Build the representation
            Representation.Builder rep_builder = new Representation.Builder();
            rep_builder.identifier(new Identifier(TypeUtils.pickRepresentationIdentifier(identifiers)));

            String representationTechnical = fedora.getXMLDatastreamContents(
                    pid, model.getRepresentationTechnical(), null);
            rep_builder.technical(XmlUtils.toObject(stream(representationTechnical)));

            String rights = fedora.getXMLDatastreamContents(pid, model.getRights(), null);
            rep_builder.rights(XmlUtils.toObject(stream(rights)));

            String source = fedora.getXMLDatastreamContents(pid, model.getSource(), null);
            rep_builder.source(XmlUtils.toObject(stream(source)));

            String provenance = fedora.getXMLDatastreamContents(pid, model.getProvenance(), null);
            rep_builder.provenance(XmlUtils.toObject(stream(provenance)));

            rep_builder.title(profile.getLabel());


            //Build the File
            File.Builder file_builder = new File.Builder();
            file_builder.identifier(new Identifier(TypeUtils.pickFileIdentifier(identifiers)));

            String fileTechnical = fedora.getXMLDatastreamContents(pid, model.getFileTechnical(), null);
            file_builder.technical(XmlUtils.toObject(stream(fileTechnical)));

            String contentDatastreamName = model.getFileContent();
            for (DatastreamProfile datastreamProfile : profile.getDatastreams()) {
                if (datastreamProfile.getID().equals(contentDatastreamName)) {
                    file_builder.filename(datastreamProfile.getLabel());
                    file_builder.mimetype(datastreamProfile.getMimeType());
                    file_builder.uri(URI.create(datastreamProfile.getUrl()));
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


        //We assume that the entire entity is encoded in one object
        //First, get the content models, to construct the combined DS-COMPOSITE-MODEL
        //Second, Find the mapping between each datastream and scape field

    }


    /**
     * Utility method to get a string as an inputstream
     *
     * @param content
     *
     * @return
     */
    private InputStream stream(String content) {
        return new ByteArrayInputStream(content.getBytes());
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
        EnhancedFedora fedora = getEnhancedFedora();

        try {
            String logmessage = "logmessage";
            List<String> existingObject
                    = fedora.findObjectFromDCIdentifier(TypeUtils.formatEntityIdentifier(entity.getIdentifier()));
            if (!existingObject.isEmpty()) {
                throw new AlreadyExistsException(existingObject.get(0));
            }



            List<String> scapeIdentifiers = TypeUtils.formatIdentifiers(entity);

            //We have a new object here, with the identifiers
            String pid = fedora.newEmptyObject(scapeIdentifiers, getCollections(), logmessage);
            //add the content models
            fedora.addRelation(pid,"info:fedora/"+ pid, HASMODEL, "info:fedora/"+scape_content_model, false, logmessage);
            DSCompositeModel model = new DSCompositeModel(scape_content_model, fedora);

            //TODO version number
            fedora.modifyDatastreamByValue(
                    pid, model.getLifeCycle(), XmlUtils.toString(entity.getLifecycleState()), null, logmessage);

            fedora.modifyDatastreamByValue(
                    pid, model.getDescriptive(), XmlUtils.toString(entity.getDescriptive()), null, logmessage);

            for (Representation representation : entity.getRepresentations()) {
                fedora.modifyDatastreamByValue(
                        pid,
                        model.getProvenance(),
                        XmlUtils.toString(representation.getProvenance()),
                        null,
                        logmessage);
                fedora.modifyDatastreamByValue(
                        pid, model.getRights(), XmlUtils.toString(representation.getRights()), null, logmessage);
                fedora.modifyDatastreamByValue(
                        pid, model.getSource(), XmlUtils.toString(representation.getSource()), null, logmessage);
                fedora.modifyDatastreamByValue(
                        pid,
                        model.getRepresentationTechnical(),
                        XmlUtils.toString(representation.getTechnical()),
                        null,
                        logmessage);
                fedora.modifyObjectLabel(pid, representation.getTitle(), logmessage);
                for (File file : representation.getFiles()) {
                    fedora.modifyDatastreamByValue(
                            pid, model.getFileTechnical(), XmlUtils.toString(file.getTechnical()), null, logmessage);
                    fedora.addExternalDatastream(
                            pid,
                            model.getFileContent(),
                            file.getFilename(),
                            file.getUri().toString(),
                            null,
                            file.getMimetype(),
                            null,
                            logmessage);

                }

            }
            return pid;

        } catch (BackendInvalidCredsException e) {
            throw new UnauthorizedException(e);

        } catch (BackendInvalidResourceException | BackendMethodFailedException | PIDGeneratorException e) {
            throw new CommunicationException(e);
        }

    }


    /**
     * Check that the entity does not violate any of our special constraints
     *
     * @param entity the entity to violate
     *
     * @return a list of content models needed by the object in doms
     * @throws ParsingException
     */
    private void checkEntity(IntellectualEntity entity) throws ParsingException {
        if (entity.getRepresentations() != null) {
            if (entity.getRepresentations().size() > 1) {
                throw new ParsingException("You can only have one representation");
            }
            for (Representation representation : entity.getRepresentations()) {
                if (representation.getFiles() != null) {
                    if (representation.getFiles().size() > 1) {
                        throw new ParsingException("You can only have one file per representation");
                    }
                    for (File file : representation.getFiles()) {
                        if (file.getBitStreams() != null) {
                            if (!file.getBitStreams().isEmpty()) {
                                throw new ParsingException("Bitststreams not supported");
                            }
                        }
                    }
                }
            }
        }
    }


    /**
     * Update an entity already persisted in doms
     *
     * @param pid
     * @param updated
     */
    public void update(String pid, IntellectualEntity updated) throws
                                                               CommunicationException,
                                                               NotFoundException,
                                                               ParsingException,
                                                               UnauthorizedException {
        EnhancedFedora fedora = getEnhancedFedora();

        try {
            String logmessage = "logmessage";
            List<String> existingObject
                    = fedora.findObjectFromDCIdentifier(TypeUtils.formatEntityIdentifier(updated.getIdentifier()));
            if (existingObject.isEmpty()) {
                throw new NotFoundException();
            }

            ObjectProfile profile = fedora.getObjectProfile(pid, null);

            List<String> scapeIdentifiers = TypeUtils.formatIdentifiers(updated);
            setIdentifiers(pid, scapeIdentifiers, fedora);

            DSCompositeModel model = new DSCompositeModel();
            for (String contentModel : profile.getContentModels()) {
                model.merge(new DSCompositeModel(contentModel, fedora));
            }
            //TODO version number

            fedora.modifyDatastreamByValue(
                    pid, model.getLifeCycle(), XmlUtils.toString(updated.getLifecycleState()), null, logmessage);


            fedora.modifyDatastreamByValue(
                    pid, model.getDescriptive(), XmlUtils.toString(updated.getDescriptive()), null, logmessage);

            for (Representation representation : updated.getRepresentations()) {
                fedora.modifyDatastreamByValue(
                        pid,
                        model.getProvenance(),
                        XmlUtils.toString(representation.getProvenance()),
                        null,
                        logmessage);
                fedora.modifyDatastreamByValue(
                        pid, model.getRights(), XmlUtils.toString(representation.getRights()), null, logmessage);
                fedora.modifyDatastreamByValue(
                        pid, model.getSource(), XmlUtils.toString(representation.getSource()), null, logmessage);
                fedora.modifyDatastreamByValue(
                        pid,
                        model.getRepresentationTechnical(),
                        XmlUtils.toString(representation.getTechnical()),
                        null,
                        logmessage);
                fedora.modifyObjectLabel(pid, representation.getTitle(), logmessage);
                for (File file : representation.getFiles()) {
                    fedora.modifyDatastreamByValue(
                            pid, model.getFileTechnical(), XmlUtils.toString(file.getTechnical()), null, logmessage);
                    fedora.addExternalDatastream(
                            pid,
                            model.getFileContent(),
                            file.getFilename(),
                            file.getUri().toString(),
                            null,
                            file.getMimetype(),
                            null,
                            logmessage);

                }

            }
        } catch (BackendInvalidCredsException e) {
            throw new UnauthorizedException(e);

        } catch (BackendInvalidResourceException | BackendMethodFailedException e) {
            throw new CommunicationException(e);
        }

    }

    private void setIdentifiers(String pid, List<String> scapeIdentifiers, EnhancedFedora fedora) {
        //TODO
    }



    public List<String> getCollections() {
        return collections;
    }

    public EnhancedFedora getEnhancedFedora() {
        return enhancedFedora;
    }
}
