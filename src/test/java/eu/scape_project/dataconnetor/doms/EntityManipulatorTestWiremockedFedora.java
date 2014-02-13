package eu.scape_project.dataconnetor.doms;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedoraImpl;
import dk.statsbiblioteket.doms.webservices.authentication.Credentials;
import eu.scape_project.model.File;
import eu.scape_project.model.IntellectualEntity;
import eu.scape_project.model.Representation;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public class EntityManipulatorTestWiremockedFedora {
    private WireMockServer wireMockServer;
    private String fedoraLocation;

    @BeforeMethod
    public void setUp() throws Exception {
        wireMockServer
                = new WireMockServer(wireMockConfig().port(7880)); //No-args constructor will start on port 8080, no HTTPS
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());
        fedoraLocation = "http://localhost:" + wireMockServer.port() + "/fedora";
    }

    @AfterMethod
    public void tearDown() throws Exception {
        wireMockServer.stop();

    }

    @Test
    public void testRead() throws Exception {
        String pid = "uuid:testPid";
        String entityIdentifier = "entity-1";
        String representationIdentifier = "representation-1";
        String fileIdentifier = "file-1";
        String title = "entity 1 title";
        String fileName = "header_image";
        String mimeType = "image/png";
        String fileUrl = "http://www.scape-project.eu/wp-content/themes/medani/images/scape_logo.png";

        String username = "fedoraAdmin";
        String password = "fedoraAdminPass";


        String scape_descriptive = "SCAPE_DESCRIPTIVE";
        String scape_lifecycle = "SCAPE_LIFECYCLE";

        String scape_rights = "SCAPE_RIGHTS";
        String scape_provenance = "SCAPE_PROVENANCE";
        String scape_source = "SCAPE_SOURCE";
        String scape_representation_technical = "SCAPE_REPRESENTATION_TECHNICAL";

        String scape_file_technical = "SCAPE_FILE_TECHNICAL";
        String scape_file_content = "SCAPE_FILE_CONTENT";

        String scape_content_model = "scape:ContentModel_SCAPE";
        List<String> collections = new ArrayList<>();


        WireMockFedora.setupObject(
                pid,
                entityIdentifier,
                representationIdentifier,
                fileIdentifier,
                username,
                password,
                scape_content_model,
                title,
                scape_file_content,
                fileName,
                mimeType,
                fileUrl,
                scape_descriptive,
                scape_file_technical,
                scape_lifecycle,
                scape_provenance,
                scape_representation_technical,
                scape_rights,
                scape_source);


        EnhancedFedora fedora = new EnhancedFedoraImpl(
                new Credentials(username, password), fedoraLocation, null, null);
        EntityManipulator entityManipulator = new EntityManipulator(collections, fedora, scape_content_model);

        IntellectualEntity entity = entityManipulator.read(pid, null, true);

        XMLUnit.setIgnoreWhitespace(true);
        XMLAssert.assertXMLEqual(
                MockFedora.getDescriptive(title), XmlUtils.toString(entity.getDescriptive()));

        XMLAssert.assertXMLEqual(
                MockFedora.getSimpleLifeCycle(), XmlUtils.toString(entity.getLifecycleState()));

        for (Representation representation : entity.getRepresentations()) {
            XMLAssert.assertXMLEqual(
                    MockFedora.getEmptyTextMD(),
                    XmlUtils.toString(representation.getTechnical().getContent().get(0).getContents()));

            XMLAssert.assertXMLEqual(
                    MockFedora.getSimpleSource(), XmlUtils.toString(representation.getSource()));

            XMLAssert.assertXMLEqual(
                    MockFedora.getSimpleRights(), XmlUtils.toString(representation.getRights()));

            XMLAssert.assertXMLEqual(
                    MockFedora.getSimpleProvenance(), XmlUtils.toString(representation.getProvenance()));
            for (File file : representation.getFiles()) {
                XMLAssert.assertXMLEqual(
                        MockFedora.getEmptyTextMD(),
                        XmlUtils.toString(file.getTechnical().getContent().get(0).getContents()));
                Assert.assertEquals(file.getFilename(), fileName);
                Assert.assertEquals(file.getMimetype(), mimeType);
                Assert.assertEquals(file.getUri(), URI.create(fileUrl));
            }

        }

    }


}
