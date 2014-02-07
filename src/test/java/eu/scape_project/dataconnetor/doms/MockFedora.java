package eu.scape_project.dataconnetor.doms;

import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.doms.central.connectors.fedora.structures.DatastreamProfile;
import dk.statsbiblioteket.doms.central.connectors.fedora.structures.ObjectProfile;
import eu.scape_project.dataconnetor.doms.exceptions.ParsingException;
import eu.scape_project.model.File;
import eu.scape_project.model.Identifier;
import eu.scape_project.model.IntellectualEntity;
import eu.scape_project.model.LifecycleState;
import eu.scape_project.model.Representation;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

public class MockFedora {
    static void setIdentifiers(String pid, String entityIdentifier, String representationIdentifier,
                               String fileIdentifier, EnhancedFedora fedora) throws
                                                                             BackendInvalidCredsException,
                                                                             BackendMethodFailedException,
                                                                             BackendInvalidResourceException {
        when(fedora.getXMLDatastreamContents(eq(pid), eq("DC"))).thenReturn(
                getDC(pid, entityIdentifier, representationIdentifier, fileIdentifier));
    }

    static void setObjectProfile(String pid, String scape_contentModel, String title, String fileName, String mimeType, String fileUrl,
                                 EnhancedFedora fedora, String scape_descriptive, String scape_lifecycle,
                                 String scape_rights, String scape_provenance, String scape_source,
                                 String scape_representation_technical, String scape_file_technical,
                                 String scape_file_content) throws
                                                            BackendMethodFailedException,
                                                            BackendInvalidCredsException,
                                                            BackendInvalidResourceException {
        ObjectProfile objectProfile = new ObjectProfile();
        objectProfile.setLabel(title);
        objectProfile.setPid(pid);
        objectProfile.setContentModels(
                Arrays.asList("info:fedora/"+scape_contentModel));


        List<DatastreamProfile> datastreamProfiles = new ArrayList<>();
        for (String name : Arrays.asList(
                scape_descriptive,
                scape_lifecycle,
                scape_rights,
                scape_provenance,
                scape_source,
                scape_representation_technical,
                scape_file_technical)) {
            DatastreamProfile managed = makeManagedDatastream(pid, name);
            datastreamProfiles.add(managed);
        }
        DatastreamProfile fileContent = new DatastreamProfile();
        fileContent.setID(scape_file_content);

        fileContent.setLabel(fileName);

        fileContent.setMimeType(mimeType);

        fileContent.setUrl(fileUrl);
        fileContent.setInternal(false);
        datastreamProfiles.add(fileContent);
        objectProfile.setDatastreams(datastreamProfiles);

        when(fedora.getObjectProfile(eq(pid), anyLong())).thenReturn(objectProfile);
    }

    static void addFileDatastreams(String pid, EnhancedFedora fedora, String scape_file_technical) throws
                                                                                                   BackendInvalidCredsException,
                                                                                                   BackendMethodFailedException,
                                                                                                   BackendInvalidResourceException,
                                                                                                   ParsingException {
        when(fedora.getXMLDatastreamContents(eq(pid), eq(scape_file_technical), anyLong())).thenReturn(
                getEmptyTextMD());
    }

    static void addRepresentationDatastreams(String pid, EnhancedFedora fedora, String scape_rights,
                                             String scape_provenance, String scape_source,
                                             String scape_representation_technical) throws
                                                                                    BackendInvalidCredsException,
                                                                                    BackendMethodFailedException,
                                                                                    BackendInvalidResourceException,
                                                                                    ParsingException {
        when(fedora.getXMLDatastreamContents(eq(pid), eq(scape_provenance), anyLong())).thenReturn(
                getSimpleProvenance());

        when(fedora.getXMLDatastreamContents(eq(pid), eq(scape_rights), anyLong())).thenReturn(
                getSimpleRights());

        when(fedora.getXMLDatastreamContents(eq(pid), eq(scape_representation_technical), anyLong())).thenReturn(
                getEmptyTextMD());
        when(fedora.getXMLDatastreamContents(eq(pid), eq(scape_source), anyLong())).thenReturn(
                getSimpleSource());
    }

    static void addEntityDatastreams(String pid, String title, EnhancedFedora fedora, String scape_descriptive,
                                     String scape_lifecycle) throws
                                                             BackendInvalidCredsException,
                                                             BackendMethodFailedException,
                                                             BackendInvalidResourceException,
                                                             ParsingException {
        when(fedora.getXMLDatastreamContents(eq(pid), eq(scape_descriptive), anyLong())).thenReturn(
                getDescriptive(title));
        when(
                fedora.getXMLDatastreamContents(
                        eq(pid), eq(scape_lifecycle), anyLong())).thenReturn(
                getSimpleLifeCycle());
    }

    static void setupContentModels(EnhancedFedora fedora, String scape_contentModel_pid, String scape_descriptive, String scape_lifecycle,
                                   String scape_rights, String scape_provenance, String scape_source,
                                   String scape_representation_technical, String scape_file_technical,
                                   String scape_file_content) throws
                                                              BackendInvalidCredsException,
                                                              BackendMethodFailedException,
                                                              BackendInvalidResourceException {
        when(
                fedora.getXMLDatastreamContents(
                        eq(scape_contentModel_pid), eq(DSCompositeModel.DS_COMPOSITE_MODEL))).thenReturn(
                getDsComp());

    }

    public static String getDsComp() {
        return "<dsCompositeModel xmlns=\"info:fedora/fedora-system:def/dsCompositeModel#\">\n" +
               "    <dsTypeModel ID=\"SCAPE_DESCRIPTIVE\">\n" +
               "        <form MIME=\"text/xml\"/>\n" +
               "        <extension name=\"SCAPE\">\n" +
               "            <mapsAs name=\"descriptive\"/>\n" +
               "        </extension>\n" +
               "    </dsTypeModel>\n" +
               "\n" +
               "    <dsTypeModel ID=\"SCAPE_LIFECYCLE\" optional=\"true\">\n" +
               "        <form MIME=\"text/xml\"/>\n" +
               "        <extension name=\"SCAPE\">\n" +
               "            <mapsAs name=\"lifecycle\"/>\n" +
               "        </extension>\n" +
               "    </dsTypeModel>\n" +
               "\n" +
               "    <dsTypeModel ID=\"SCAPE_RIGHTS\" optional=\"true\">\n" +
               "        <form MIME=\"text/xml\"/>\n" +
               "        <extension name=\"SCAPE\">\n" +
               "            <mapsAs name=\"rights\"/>\n" +
               "        </extension>\n" +
               "    </dsTypeModel>\n" +
               "\n" +
               "    <dsTypeModel ID=\"SCAPE_PROVENANCE\" optional=\"true\">\n" +
               "        <form MIME=\"text/xml\"/>\n" +
               "        <extension name=\"SCAPE\">\n" +
               "            <mapsAs name=\"provenance\"/>\n" +
               "        </extension>\n" +
               "    </dsTypeModel>\n" +
               "\n" +
               "    <dsTypeModel ID=\"SCAPE_SOURCE\" optional=\"true\">\n" +
               "        <form MIME=\"text/xml\"/>\n" +
               "        <extension name=\"SCAPE\">\n" +
               "            <mapsAs name=\"source\"/>\n" +
               "        </extension>\n" +
               "    </dsTypeModel>\n" +
               "\n" +
               "    <dsTypeModel ID=\"SCAPE_REPRESENTATION_TECHNICAL\" optional=\"true\">\n" +
               "        <form MIME=\"text/xml\"/>\n" +
               "        <extension name=\"SCAPE\">\n" +
               "            <mapsAs name=\"representation_technical\"/>\n" +
               "        </extension>\n" +
               "    </dsTypeModel>\n" +
               "\n" +
               "    <dsTypeModel ID=\"SCAPE_FILE_TECHNICAL\" optional=\"true\">\n" +
               "        <form MIME=\"text/xml\"/>\n" +
               "        <extension name=\"SCAPE\">\n" +
               "            <mapsAs name=\"file_technical\"/>\n" +
               "        </extension>\n" +
               "    </dsTypeModel>\n" +
               "\n" +
               "    <dsTypeModel ID=\"SCAPE_FILE_CONTENT\">\n" +
               "        <extension name=\"SCAPE\">\n" +
               "            <mapsAs name=\"file_content\"/>\n" +
               "        </extension>\n" +
               "    </dsTypeModel>\n" +
               "\n" +
               "</dsCompositeModel>\n";
    }

    static String getDC(String pid, String entityIdentifier, String representationIdentifier, String fileIdentifier) {
        return "<oai_dc:dc xmlns:oai_dc=\"http://www.openarchives.org/OAI/2.0/oai_dc/\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd\">" +
               "  <dc:identifier>" + pid + "</dc:identifier>" +
               "  <dc:identifier>" + TypeUtils.formatEntityIdentifier(new Identifier(entityIdentifier)) + "</dc:identifier>" +
               "  <dc:identifier>" + TypeUtils.formatRepresentationIdentifier(new Identifier(representationIdentifier)) + "</dc:identifier>" +
               "  <dc:identifier>" + TypeUtils.formatFileIdentifier(new Identifier(fileIdentifier)) + "</dc:identifier>" +
               "</oai_dc:dc>";
    }

    static DatastreamProfile makeManagedDatastream(String pid, String id) {
        DatastreamProfile managed = new DatastreamProfile();
        managed.setID(id);
        managed.setMimeType("text/xml");
        managed.setInternal(true);
        return managed;
    }

    static String getSimpleSource() throws ParsingException {
        return XmlUtils.toString(XmlUtils.toObject( " <dc:dublin-core xmlns:dc=\"http://purl.org/dc/elements/1.1/\">" +
               "                        <dc:title>Source object 1</dc:title>" +
               "                    </dc:dublin-core>"));
    }

    static String getDescriptive(String title) {
        return "<dc:dublin-core xmlns:mets=\"http://www.loc.gov/METS/\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:scape=\"http://scape-project.eu/model\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:premis=\"info:lc/xmlns/premis-v2\" xmlns:textmd=\"info:lc/xmlns/textmd-v3\" xmlns:fits=\"http://hul.harvard.edu/ois/xml/ns/fits/fits_output\" xmlns:ns9=\"http://www.loc.gov/mix/v20\" xmlns:gbs=\"http://books.google.com/gbs\" xmlns:vmd=\"http://www.loc.gov/videoMD/\" xmlns:ns12=\"http://www.loc.gov/audioMD/\" xmlns:marc=\"http://www.loc.gov/MARC21/slim\"><dc:title>" + title + "</dc:title></dc:dublin-core>";
    }

    static String getSimpleLifeCycle() throws ParsingException {
        return XmlUtils.toString(
                new LifecycleState(
                        "details", LifecycleState.State.INGESTED));
    }

    static String getSimpleProvenance() throws ParsingException {
        return XmlUtils.toString(XmlUtils.toObject(
         "                    <premis:premis version=\"2.2\" xmlns:premis=\"info:lc/xmlns/premis-v2\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">" +
               "                        <premis:event>" +
               "                            <premis:eventType>INGEST</premis:eventType>" +
               "                            <premis:eventDetail>inital ingest</premis:eventDetail>" +
               "                            <premis:linkingAgentIdentifier xlink:role=\"CREATOR\" xlink:title=\"Testman Testrevicz\"/>" +
               "                        </premis:event>" +
               "                    </premis:premis>"));
    }

    static String getSimpleRights() throws ParsingException {
        return XmlUtils.toString(XmlUtils.toObject( "                    <premis:rights xmlns:premis=\"info:lc/xmlns/premis-v2\">" +
               "                        <premis:rightsStatement>" +
               "                            <premis:copyrightInformation>" +
               "<premis:copyrightStatus>no copyright</premis:copyrightStatus>" +
               "<premis:copyrightJurisdiction>de</premis:copyrightJurisdiction>" +
               "                            </premis:copyrightInformation>" +
               "                        </premis:rightsStatement>" +
               "                    </premis:rights>"));
    }

    static String getEmptyTextMD() throws ParsingException {
        return XmlUtils.toString(XmlUtils.toObject( "<textmd:textMD xmlns:textmd=\"info:lc/xmlns/textmd-v3\">" +
               "<textmd:encoding>" +
               "<textmd:encoding_platform linebreak=\"LF\"/>" +
               "</textmd:encoding>" +
               "</textmd:textMD>"));
    }

    public static IntellectualEntity buildNewEntity(String entityIdentifier, String representationIdentifier,
                                                    String fileIdentifier, String title, String repTitle, URI fileUri,
                                                    String mimetype, String filename, String scape_file_technical,
                                                    String scape_representation_technical) throws ParsingException {
        IntellectualEntity.Builder builder = new IntellectualEntity.Builder();
        builder.identifier(new Identifier(entityIdentifier));
        builder.descriptive(XmlUtils.toObject(getDescriptive(title)));
        builder.lifecycleState(
                new LifecycleState(
                        "details", LifecycleState.State.INGESTED));
        Representation.Builder repBuilder = new Representation.Builder();
        repBuilder.title(repTitle);
        repBuilder.identifier(new Identifier(representationIdentifier));
        repBuilder.rights(XmlUtils.toObject(getSimpleRights()));
        repBuilder.technical(scape_representation_technical, XmlUtils.toObject(getEmptyTextMD()));
        repBuilder.provenance(XmlUtils.toObject(getSimpleProvenance()));
        repBuilder.source(XmlUtils.toObject(getSimpleSource()));

        File.Builder fileBuilder = new File.Builder();
        fileBuilder.uri(fileUri);
        fileBuilder.mimetype(mimetype);
        fileBuilder.filename(filename);
        fileBuilder.identifier(new Identifier(fileIdentifier));
        fileBuilder.technical(scape_file_technical, XmlUtils.toObject(getEmptyTextMD()));
        repBuilder.files(Arrays.asList(fileBuilder.build()));
        builder.representations(Arrays.asList(repBuilder.build()));
        return builder.build();


    }


}
