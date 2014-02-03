package eu.scape_project.dataconnetor.doms;

import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import eu.scape_project.model.File;
import eu.scape_project.model.IntellectualEntity;
import eu.scape_project.model.Representation;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.net.URI;

import static org.mockito.Mockito.mock;

public class EntityInterfaceTest {
    @Test
    public void testRead() throws Exception {
        String pid = "uuid:testPid";
        String entityIdentifier = "entity-1";
        String representationIdentifier = "representation-1";
        String fileIdentifier = "file-1";
        String title = "entity 1 title";
        String fileName = "header_image";
        String mimeType = "image/png";
        String fileUrl ="http://www.scape-project.eu/wp-content/themes/medani/images/scape_logo.png";


        EnhancedFedora fedora = mock(EnhancedFedora.class);

        String scape_descriptive = "SCAPE_DESCRIPTIVE";
        String scape_lifecycle = "SCAPE_LIFECYCLE";

        String scape_rights = "SCAPE_RIGHTS";
        String scape_provenance = "SCAPE_PROVENANCE";
        String scape_source = "SCAPE_SOURCE";
        String scape_representation_technical = "SCAPE_REPRESENTATION_TECHNICAL";

        String scape_file_technical = "SCAPE_FILE_TECHNICAL";
        String scape_file_content = "SCAPE_FILE_CONTENT";


        MockFedora.setupContentModels(
                fedora,
                scape_descriptive,
                scape_lifecycle,
                scape_rights,
                scape_provenance,
                scape_source,
                scape_representation_technical,
                scape_file_technical,
                scape_file_content);


        MockFedora.setIdentifiers(pid, entityIdentifier, representationIdentifier, fileIdentifier, fedora);
        MockFedora.setObjectProfile(
                pid,
                title,
                fileName,
                mimeType,
                fileUrl,
                fedora,
                scape_descriptive,
                scape_lifecycle,
                scape_rights,
                scape_provenance,
                scape_source,
                scape_representation_technical,
                scape_file_technical,
                scape_file_content);


        MockFedora.addEntityDatastreams(pid, title, fedora, scape_descriptive, scape_lifecycle);
        MockFedora.addRepresentationDatastreams(
                pid, fedora, scape_rights, scape_provenance, scape_source, scape_representation_technical);
        MockFedora.addFileDatastreams(pid, fedora, scape_file_technical);
        EntityInterface entityInterface = new EntityInterface(fedora);

        IntellectualEntity entity = entityInterface.read(pid);

        XMLUnit.setIgnoreWhitespace(true);
        XMLAssert.assertXMLEqual(
                MockFedora.getDescriptive(title),
                XmlUtils.toString(entity.getDescriptive()));

        XMLAssert.assertXMLEqual(
                MockFedora.getSimpleLifeCycle(),
                XmlUtils.toString(entity.getLifecycleState()));

        for (Representation representation : entity.getRepresentations()) {
            XMLAssert.assertXMLEqual(
                    MockFedora.getEmptyTextMD(),
                    XmlUtils.toString(representation.getTechnical()));

            XMLAssert.assertXMLEqual(
                    MockFedora.getSimpleSource(),
                    XmlUtils.toString(representation.getSource()));

            XMLAssert.assertXMLEqual(
                    MockFedora.getSimpleRights(),
                    XmlUtils.toString(representation.getRights()));

            XMLAssert.assertXMLEqual(
                    MockFedora.getSimpleProvenance(),
                    XmlUtils.toString(representation.getProvenance()));
            for (File file : representation.getFiles()) {
                XMLAssert.assertXMLEqual(
                        MockFedora.getEmptyTextMD(),
                        XmlUtils.toString(file.getTechnical()));
                Assert.assertEquals(file.getFilename(),fileName);
                Assert.assertEquals(file.getMimetype(),mimeType);
                Assert.assertEquals(file.getUri(), URI.create(fileUrl));
            }

        }

    }

    @Test
    public void testCreateNew() throws Exception {

    }

    @Test
    public void testUpdate() throws Exception {

    }
}
