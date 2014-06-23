package eu.scape_project.dataconnetor.doms;

import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import eu.scape_project.dataconnetor.doms.exceptions.CommunicationException;
import versions.ObjectFactory;
import versions.VersionType;
import versions.VersionsType;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;

public class VersionUtils {


    public static Long findVersionInScapeVersions(String scapeVersions, String versionID) {
        //TODO make this better
        try {
            VersionsType versions = getJaxbContext().createUnmarshaller().unmarshal(
                    new StreamSource(new StringReader(scapeVersions)), VersionsType.class).getValue();
            for (VersionType version : versions.getVersion()) {
                if (version.getId().toString().equals(versionID)){
                    return version.getTimestamp();
                }
            }
        } catch (JAXBException e) {
            return null;
        }
        return null;
    }

    private static JAXBContext getJaxbContext() throws JAXBException {
        return JAXBContext.newInstance(ObjectFactory.class);
    }

    static void setVersions(String pid, EnhancedFedora fedora, String logMessage, VersionsType versions) throws
                                                                                                         CommunicationException,
                                                                                                         BackendInvalidCredsException,
                                                                                                         BackendMethodFailedException,
                                                                                                         BackendInvalidResourceException {
        String xml = toXml(versions);

        fedora.modifyDatastreamByValue(pid, EntityManipulator.SCAPE_VERSIONS,xml,null,logMessage);
    }

    public static String toXml(VersionsType versions) throws CommunicationException {
        StringWriter sink = new StringWriter();
        try {
            getJaxbContext().createMarshaller().marshal(new ObjectFactory().createVersions(versions),sink);
        } catch (JAXBException e) {
            throw new CommunicationException(e);
        }
        return sink.toString();
    }

    static VersionsType getVersions(String pid, EnhancedFedora fedora) throws
                                                                        BackendInvalidCredsException,
                                                                        BackendMethodFailedException,
                                                                        CommunicationException {
        VersionsType versions = null;
        try {
            String versionsXml = fedora.getXMLDatastreamContents(pid, EntityManipulator.SCAPE_VERSIONS, null);
            if (versionsXml == null){
                return new VersionsType();
            }
            versions = getJaxbContext().createUnmarshaller().unmarshal(
                    new StreamSource(new StringReader(versionsXml)), VersionsType.class).getValue();

        } catch (BackendInvalidResourceException e){
            versions = new VersionsType();
        } catch (JAXBException e) {
            throw new CommunicationException(e);
        }
        return versions;
    }
}
