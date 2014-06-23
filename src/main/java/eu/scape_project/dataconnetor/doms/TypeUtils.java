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
        if (!identifier.getValue().startsWith(ENTITY)) {
            return ENTITY + identifier.getValue();
        } else {
            return identifier.getValue();
        }
    }

    static String formatRepresentationIdentifier(Identifier identifier) {
        if (!identifier.getValue().startsWith(REPRESENTATION)) {
            return REPRESENTATION + identifier.getValue();
        } else {
            return identifier.getValue();
        }
    }

    static String formatFileIdentifier(Identifier identifier) {
        if (!identifier.getValue().startsWith(FILE)) {
            return FILE + identifier.getValue();
        } else {
            return identifier.getValue();
        }
    }

    static String pickFileIdentifier(String pid, List<String> identifiers) {
        for (String identifier : identifiers) {
            if (identifier.startsWith(FILE)) {
                return identifier;
                //return identifier.replaceFirst("^" + Pattern.quote(FILE), "");
            }
        }
        return FILE + pid;
    }


    static String pickRepresentationIdentifier(String pid, List<String> identifiers) {
        for (String identifier : identifiers) {
            if (identifier.startsWith(REPRESENTATION)) {
                return identifier;
                //return identifier.replaceFirst("^" + Pattern.quote(REPRESENTATION), "");
            }
        }
        return REPRESENTATION + pid;
    }


    static String pickEntityIdentifier(String pid, List<String> identifiers) {
        for (String identifier : identifiers) {
            if (identifier.startsWith(ENTITY)) {
                return identifier;
                //return identifier.replaceFirst("^" + Pattern.quote(ENTITY), "");
            }
        }
        return ENTITY + pid;
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
