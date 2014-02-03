package eu.scape_project.dataconnetor.doms;

import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.util.xml.DOM;
import dk.statsbiblioteket.util.xml.XPathSelector;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DSCompositeModel {
    public static final String DS_COMPOSITE_MODEL = "DS-COMPOSITE-MODEL";
    private String descriptive;
    private String rights;
    private String source;
    private String provenance;
    private String fileTechnical;
    private String fileContent;
    private String representationTechnical;
    private String lifeCycle;

    public DSCompositeModel(String contentModelPid, EnhancedFedora fedora) throws
                                                                           BackendMethodFailedException,
                                                                           BackendInvalidResourceException,
                                                                           BackendInvalidCredsException {
        String dsCompXml = fedora.getXMLDatastreamContents(
                contentModelPid, DS_COMPOSITE_MODEL);

        Document dsDoc = DOM.stringToDOM(dsCompXml, true);
        XPathSelector xpath = DOM.createXPathSelector("d", "info:fedora/fedora-system:def/dsCompositeModel#");
        NodeList scapeDatastreamNodeList = xpath.selectNodeList(
                dsDoc, "/d:dsCompositeModel/d:dsTypeModel[d:extension/@name='SCAPE']");
        for (int i = 0; i < scapeDatastreamNodeList.getLength(); i++) {
            Node node = scapeDatastreamNodeList.item(i);
            String name = node.getAttributes().getNamedItem("ID").getTextContent();
            String value = xpath.selectString(node, "d:extension[@name='SCAPE']/d:mapsAs/@name");
            switch (value) {
                case "descriptive":
                    descriptive = name;
                    break;
                case "rights":
                    rights = name;
                    break;
                case "lifecycle":
                    lifeCycle = name;
                    break;
                case "provenance":
                    provenance = name;
                    break;
                case "source":
                    source = name;
                    break;
                case "representation_technical":
                    representationTechnical = name;
                    break;
                case "file_technical":
                    fileTechnical = name;
                    break;
                case "file_content":
                    fileContent = name;
                    break;
                default:
                    break;
            }
        }
    }

    public DSCompositeModel() {

    }

    public String getDescriptive() {
        return descriptive;
    }

    public String getRights() {
        return rights;
    }

    public String getSource() {
        return source;
    }

    public String getProvenance() {
        return provenance;
    }

    public String getFileTechnical() {
        return fileTechnical;
    }

    public String getFileContent() {
        return fileContent;
    }

    public void merge(DSCompositeModel dsCompositeModel) {
        String temp = dsCompositeModel.getDescriptive();
        if (temp != null) {
            descriptive = temp;
        }
        temp = dsCompositeModel.getRights();
        if (temp != null) {
            rights = temp;
        }

        temp = dsCompositeModel.getLifeCycle();
        if (temp != null) {
            lifeCycle = temp;
        }

        temp = dsCompositeModel.getProvenance();
        if (temp != null) {
            provenance = temp;
        }

        temp = dsCompositeModel.getSource();
        if (temp != null) {
            source = temp;
        }

        temp = dsCompositeModel.getRepresentationTechnical();
        if (temp != null) {
            representationTechnical = temp;
        }

        temp = dsCompositeModel.getFileTechnical();
        if (temp != null) {
            fileTechnical = temp;
        }

        temp = dsCompositeModel.getFileContent();
        if (temp != null) {
            fileContent = temp;
        }

    }

    public String getRepresentationTechnical() {
        return representationTechnical;
    }

    public String getLifeCycle() {
        return lifeCycle;
    }
}
