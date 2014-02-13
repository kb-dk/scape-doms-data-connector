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
import java.util.UUID;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public class EntityManipulatorTestWiremockedFedora {
    private final int portNumber = 7880;
    private WireMockServer wireMockServer;
    private final String hostname = "http://localhost:" + portNumber;
    private String fedoraLocation = hostname + "/fedora";
    private String pidgeneratorLocation = hostname + "/pidgenerator-service";
    private final String password = "fedoraAdminPass";
    private final String username = "fedoraAdmin";
    private final List<String> collections = new ArrayList<>();
    private final String scape_content_model = "scape:ContentModel_SCAPE";
    private final String scape_file_content = "SCAPE_FILE_CONTENT";
    private final String scape_file_technical = "SCAPE_FILE_TECHNICAL";
    private final String scape_representation_technical = "SCAPE_REPRESENTATION_TECHNICAL";
    private final String scape_source = "SCAPE_SOURCE";
    private final String scape_provenance = "SCAPE_PROVENANCE";
    private final String scape_rights = "SCAPE_RIGHTS";
    private final String scape_lifecycle = "SCAPE_LIFECYCLE";
    private final String scape_descriptive = "SCAPE_DESCRIPTIVE";
    private final String fileUrl = "http://www.scape-project.eu/wp-content/themes/medani/images/scape_logo.png";
    private final String mimeType = "image/png";
    private final String fileName = "header_image";
    private final String rep_title = "rep 1 title";
    private final String entityIdentifier = "entity-1";
    private final String representationIdentifier = "representation-1";
    private final String fileIdentifier = "file-1";
    private final String title = "entity 1 title";


    @BeforeMethod
    public void setUp() throws Exception {
        wireMockServer
                = new WireMockServer(wireMockConfig().port(portNumber)); //No-args constructor will start on port 8080, no HTTPS
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());

    }

    @AfterMethod
    public void tearDown() throws Exception {
        wireMockServer.stop();

    }

    @Test
    public void testRead() throws Exception {
        String pid = "uuid:testPid";


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
                new Credentials(username, password), fedoraLocation, pidgeneratorLocation, null);
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

    @Test
    public void testCreateNew() throws Exception {


        EnhancedFedora fedora = new EnhancedFedoraImpl(
                new Credentials(username, password), fedoraLocation, pidgeneratorLocation, null);

        EntityManipulator entityManipulator = new EntityManipulator(collections, fedora, scape_content_model);

        IntellectualEntity newEntity;
        newEntity = MockFedora.buildNewEntity(
                entityIdentifier,
                representationIdentifier,
                fileIdentifier,
                title,
                rep_title,
                URI.create(fileUrl),
                mimeType,
                fileName,
                scape_file_technical,
                scape_representation_technical);

        String expectedPid = "uuid:" + UUID.randomUUID().toString();
        WireMockFedora.setupForNewObject(
                expectedPid,
                entityIdentifier,
                representationIdentifier,
                fileIdentifier,
                username,
                password,
                scape_content_model,
                title,
                rep_title,
                scape_file_content,
                fileName,
                mimeType,
                fileUrl,
                scape_descriptive,
                XmlUtils.toString(newEntity.getDescriptive()),
                scape_file_technical,
                MockFedora.getEmptyTextMD(),
                scape_lifecycle,
                XmlUtils.toString(newEntity.getLifecycleState()),
                scape_provenance,
                MockFedora.getSimpleProvenance(),
                scape_representation_technical,
                MockFedora.getEmptyTextMD(),
                scape_rights,
                MockFedora.getSimpleRights(),
                scape_source,
                MockFedora.getSimpleSource());


        String pid = entityManipulator.createNew(newEntity);

        Assert.assertEquals(pid, expectedPid);

    }


}
