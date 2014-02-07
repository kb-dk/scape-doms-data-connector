package eu.scape_project.dataconnetor.doms;

import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
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
    static final String ENTITY = "scape-entity:";
    static final String REPRESENTATION = "scape-representation:";
    static final String FILE = "scape-file:";
    static final String BITSTREAM = "scape-bitstream:";

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
            if (identifier.startsWith(FILE)) {
                return identifier.replaceFirst("^" + Pattern.quote(FILE), "");
            }
        }
        return null;
    }


    static String pickRepresentationIdentifier(List<String> identifiers) {
        for (String identifier : identifiers) {
            if (identifier.startsWith(REPRESENTATION)) {
                return identifier.replaceFirst("^" + Pattern.quote(REPRESENTATION), "");
            }
        }
        return null;
    }


    static String pickEntityIdentifier(List<String> identifiers) {
        for (String identifier : identifiers) {
            if (identifier.startsWith(ENTITY)) {
                return identifier.replaceFirst("^" + Pattern.quote(ENTITY), "");
            }
        }
        return null;
    }


    static List<String> formatIdentifiers(IntellectualEntity entity) {
        List<String> result = new ArrayList<>();
        result.add(formatEntityIdentifier(entity.getIdentifier()));
        if (entity.getRepresentations() != null) {
            for (Representation representation : entity.getRepresentations()) {
                result.add(formatRepresentationIdentifier(representation.getIdentifier()));
                if (representation.getFiles() != null) {
                    for (File file : representation.getFiles()) {
                        result.add(formatFileIdentifier(file.getIdentifier()));
                        if (file.getBitStreams() != null) {
                            for (BitStream bitStream : file.getBitStreams()) {
                                result.add(formatBitstreamIdentifier(bitStream.getIdentifier()));
                            }
                        }
                    }
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
        NodeList dcIdentifiers = DOM.createXPathSelector("oai", NAMESPACE_OAIDC, "dc", NAMESPACE_DC)
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
