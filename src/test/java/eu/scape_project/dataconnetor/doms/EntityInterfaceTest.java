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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

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
        String fileUrl = "http://www.scape-project.eu/wp-content/themes/medani/images/scape_logo.png";


        EnhancedFedora fedora = mock(EnhancedFedora.class);

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


        MockFedora.setupContentModels(
                fedora,
                scape_content_model,
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
                scape_content_model,
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
        EntityInterface entityInterface = new EntityInterface(collections, fedora, scape_content_model);

        IntellectualEntity entity = entityInterface.read(pid);

        XMLUnit.setIgnoreWhitespace(true);
        XMLAssert.assertXMLEqual(
                MockFedora.getDescriptive(title), XmlUtils.toString(entity.getDescriptive()));

        XMLAssert.assertXMLEqual(
                MockFedora.getSimpleLifeCycle(), XmlUtils.toString(entity.getLifecycleState()));

        for (Representation representation : entity.getRepresentations()) {
            XMLAssert.assertXMLEqual(
                    MockFedora.getEmptyTextMD(), XmlUtils.toString(representation.getTechnical()));

            XMLAssert.assertXMLEqual(
                    MockFedora.getSimpleSource(), XmlUtils.toString(representation.getSource()));

            XMLAssert.assertXMLEqual(
                    MockFedora.getSimpleRights(), XmlUtils.toString(representation.getRights()));

            XMLAssert.assertXMLEqual(
                    MockFedora.getSimpleProvenance(), XmlUtils.toString(representation.getProvenance()));
            for (File file : representation.getFiles()) {
                XMLAssert.assertXMLEqual(
                        MockFedora.getEmptyTextMD(), XmlUtils.toString(file.getTechnical()));
                Assert.assertEquals(file.getFilename(), fileName);
                Assert.assertEquals(file.getMimetype(), mimeType);
                Assert.assertEquals(file.getUri(), URI.create(fileUrl));
            }

        }

    }

    @Test
    public void testCreateNew() throws Exception {
        String entityIdentifier = "entity-1";
        String representationIdentifier = "representation-1";
        String fileIdentifier = "file-1";
        String title = "entity 1 title";
        String fileName = "header_image";
        String mimeType = "image/png";
        String fileUrl = "http://www.scape-project.eu/wp-content/themes/medani/images/scape_logo.png";


        EnhancedFedora fedora = mock(EnhancedFedora.class);

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


        MockFedora.setupContentModels(
                fedora,
                scape_content_model,
                scape_descriptive,
                scape_lifecycle,
                scape_rights,
                scape_provenance,
                scape_source,
                scape_representation_technical,
                scape_file_technical,
                scape_file_content);




        EntityInterface entityInterface = new EntityInterface(collections, fedora, scape_content_model);

        IntellectualEntity newEntity;
        newEntity = MockFedora.buildNewEntity(
                entityIdentifier,
                representationIdentifier,
                fileIdentifier,
                title,
                null,
                URI.create(fileUrl),
                mimeType,
                fileName);

        String expectedPid = "uuid:" + UUID.randomUUID().toString();
        when(fedora.newEmptyObject(anyList(), anyList(), anyString())).thenReturn(expectedPid);

        when(fedora.getXMLDatastreamContents(eq(scape_content_model),eq(DSCompositeModel.DS_COMPOSITE_MODEL),anyLong())).thenReturn(MockFedora.getDsComp());

        String pid = entityInterface.createNew(newEntity);

        Assert.assertEquals(pid, expectedPid);

        verify(fedora).findObjectFromDCIdentifier(eq(TypeUtils.formatEntityIdentifier(newEntity.getIdentifier())));

        verify(fedora, times(1)).newEmptyObject(anyList(), anyList(), anyString());

        verify(fedora).addRelation(
                eq(expectedPid),
                eq("info:fedora/" + expectedPid),
                eq(EntityInterface.HASMODEL),
                eq("info:fedora/" + scape_content_model),
                eq(false),
                anyString());

        verify(fedora).getXMLDatastreamContents(eq(scape_content_model),eq(DSCompositeModel.DS_COMPOSITE_MODEL));
        verify(fedora).modifyDatastreamByValue(
                eq(expectedPid),
                eq(scape_lifecycle),
                eq(XmlUtils.toString(newEntity.getLifecycleState())),
                anyList(),
                anyString());
        verify(fedora).modifyDatastreamByValue(
                eq(expectedPid),
                eq(scape_descriptive),
                eq(XmlUtils.toString(newEntity.getDescriptive())),
                anyList(),
                anyString());
        for (Representation representation : newEntity.getRepresentations()) {
            verify(fedora).modifyDatastreamByValue(
                    eq(expectedPid),
                    eq(scape_provenance),
                    eq(XmlUtils.toString(representation.getProvenance())),
                    anyList(),
                    anyString());
            verify(fedora).modifyDatastreamByValue(
                    eq(expectedPid),
                    eq(scape_rights),
                    eq(XmlUtils.toString(representation.getRights())),
                    anyList(),
                    anyString());
            verify(fedora).modifyDatastreamByValue(
                    eq(expectedPid),
                    eq(scape_source),
                    eq(XmlUtils.toString(representation.getSource())),
                    anyList(),
                    anyString());
            verify(fedora).modifyDatastreamByValue(
                    eq(expectedPid),
                    eq(scape_representation_technical),
                    eq(XmlUtils.toString(representation.getTechnical())),
                    anyList(),
                    anyString());
            verify(fedora).modifyObjectLabel(anyString(), eq(representation.getTitle()), anyString());
            for (File file : representation.getFiles()) {
                verify(fedora).modifyDatastreamByValue(
                        eq(expectedPid),
                        eq(scape_file_technical),
                        eq(XmlUtils.toString(file.getTechnical())),
                        anyList(),
                        anyString());
                verify(fedora).addExternalDatastream(
                        eq(expectedPid), eq(scape_file_content), eq(file.getFilename()), eq(
                        file.getUri().toString()), anyString(), eq(file.getMimetype()), anyList(), anyString());
            }
        }
        verifyNoMoreInteractions(fedora);

    }

    @Test
    public void testUpdate() throws Exception {

    }
}
