package eu.scape_project.dataconnetor.doms;

import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.doms.central.connectors.fedora.structures.ObjectProfile;
import dk.statsbiblioteket.util.xml.DOM;
import eu.scape_project.model.BitStream;
import eu.scape_project.model.File;
import eu.scape_project.model.Identifier;
import eu.scape_project.model.IntellectualEntity;
import eu.scape_project.model.Representation;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class TypeUtils {
    public static final String NAMESPACE_DC = "http://purl.org/dc/elements/1.1/";
    public static final String NAMESPACE_OAIDC = "http://www.openarchives.org/OAI/2.0/oai_dc/";
    public static final String ENTITY_CONTENT_MODEL = "info:fedora/scape:Entity_ContentModel";
    public static final String REPRESENTATION_CONTENT_MODEL = "info:fedora/scape:Representation_ContentModel";
    public static final String FILE_CONTENT_MODEL = "info:fedora/scape:File_ContentModel";
    static final String ENTITY = "scape_entity:";
    static final String REPRESENTATION = "scape_representation:";
    static final String FILE = "scape_file:";
    static final String BITSTREAM = "scape_bitstream:";

    static String formatBitstreamIdentifier(Identifier identifier) {
        return BITSTREAM + identifier.getValue();
    }

    static String formatEntityIdentifier(Identifier identifier) {
        return ENTITY + identifier.getValue();
    }

    static String formatRepresentationIdentifier(Identifier identifier) {
        return REPRESENTATION + identifier.getValue();

    }

    static String formatFileIdentifier(Identifier identifier) {
            return FILE + identifier.getValue();
        }

    static String pickFileIdentifier(List<String> identifiers) {
            for (String identifier : identifiers) {
                if (identifier.startsWith(FILE)){
                    return identifier.replaceFirst("^"+ Pattern.quote(FILE),"");
                }
            }
            return null;
        }

    static boolean isFile(ObjectProfile profile) {
            if (profile.getContentModels().contains(FILE_CONTENT_MODEL)) {
                return true;
            }

            return false;
        }

    static String pickRepresentationIdentifier(List<String> identifiers) {
            for (String identifier : identifiers) {
                if (identifier.startsWith(REPRESENTATION)){
                    return identifier.replaceFirst("^"+ Pattern.quote(REPRESENTATION),"");
                }
            }
            return null;
        }

    static boolean isRepresentation(ObjectProfile profile) {
            if (profile.getContentModels().contains(REPRESENTATION_CONTENT_MODEL)) {
                return true;
            }

            return false;
        }

    static String pickEntityIdentifier(List<String> identifiers) {
            for (String identifier : identifiers) {
                if (identifier.startsWith(ENTITY)){
                    return identifier.replaceFirst("^"+ Pattern.quote(ENTITY),"");
                }
            }
            return null;
        }

    static boolean isIntellectualEntity(ObjectProfile profile) {
            if (profile.getContentModels().contains(ENTITY_CONTENT_MODEL)) {
                return true;
            }

            return false;
        }

    static List<String> formatIdentifiers(IntellectualEntity entity) {
        List<String> result = new ArrayList<>();
        result.add(formatEntityIdentifier(entity.getIdentifier()));
        for (Representation representation : entity.getRepresentations()) {
            result.add(formatRepresentationIdentifier(representation.getIdentifier()));
            for (File file : representation.getFiles()) {
                result.add(formatFileIdentifier(file.getIdentifier()));
                for (BitStream bitStream : file.getBitStreams()) {
                    result.add(formatBitstreamIdentifier(bitStream.getIdentifier()));
                }
            }
        }
        return result;
    }

    static List<String> getDCIdentifiers(EnhancedFedora fedora, String pid, String prefix) throws
                                                                                           BackendMethodFailedException,
                                                                                           BackendInvalidResourceException,
                                                                                           BackendInvalidCredsException {
        Document DCdoc = DOM.stringToDOM(fedora.getXMLDatastreamContents(pid, "DC"), true);
        NodeList
                dcIdentifiers = DOM.createXPathSelector("oai", NAMESPACE_OAIDC, "dc", NAMESPACE_DC)
                                    .selectNodeList(DCdoc, "/oai:dc/dc:identifier");
        List<String> result = new ArrayList<>();
        for (int i = 0; i < dcIdentifiers.getLength(); i++) {
            Node dcIdentifier = dcIdentifiers.item(i);
            String textContent = dcIdentifier.getTextContent();
            if (textContent.startsWith(prefix)) {
                result.add(textContent);
            }

        }
        return result;
    }
}
