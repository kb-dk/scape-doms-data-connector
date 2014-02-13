package eu.scape_project.dataconnetor.doms;

import eu.scape_project.dataconnetor.doms.exceptions.CommunicationException;
import eu.scape_project.dataconnetor.doms.exceptions.ParsingException;
import eu.scape_project.model.Identifier;
import eu.scape_project.model.LifecycleState;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;

public class EqualUtils {
    public static boolean xmlEquals(Object a, Object b) throws ParsingException, CommunicationException {
        InputStream aXml = XmlUtils.toBytes(a);
        InputStream bXml = XmlUtils.toBytes(b);
        Diff diff = null;
        try {
            diff = XMLUnit.compareXML(new InputSource(aXml), new InputSource(bXml));
        } catch (SAXException e) {
            throw new ParsingException(e);
        } catch (IOException e) {
            throw new CommunicationException(e);
        }
        return diff.identical();

    }

    public static boolean representationSourceEquals(Object a, Object b) throws
                                                                         ParsingException,
                                                                         CommunicationException {
        return xmlEquals(a, b);
    }

    public static boolean representationProvenanceEquals(Object a, Object b) throws
                                                                             ParsingException,
                                                                             CommunicationException {
        return xmlEquals(a, b);
    }

    public static boolean representationRightsEquals(Object a, Object b) throws
                                                                         ParsingException,
                                                                         CommunicationException {
        return xmlEquals(a, b);
    }

    public static boolean representationTechnicalEquals(Object a, Object b) throws
                                                                            ParsingException,
                                                                            CommunicationException {
        return xmlEquals(a, b);
    }

    public static boolean lifeCycleEquals(LifecycleState lifecycleState, LifecycleState lifecycleState1) {
        return lifecycleState.equals(lifecycleState1);
    }

    public static boolean alternativeIdentifiersEquals(List<Identifier> alternativeIdentifiers,
                                                       List<Identifier> alternativeIdentifiers1) {
        HashSet<Identifier> temp = new HashSet<>(alternativeIdentifiers);
        temp.removeAll(alternativeIdentifiers1);
        return temp.isEmpty();
    }

    public static boolean descriptiveEquals(Object a, Object b) throws ParsingException, CommunicationException {
        return xmlEquals(a, b);
    }

    public static boolean fileTechnicalEquals(Object a, Object b) throws ParsingException, CommunicationException {
        return xmlEquals(a, b);
    }

    public static boolean bitstreamTechicalEquals(Object a, Object b) throws ParsingException, CommunicationException {
        return xmlEquals(a, b);
    }

    public static String longForm(String pid) {
        String s = "info:fedora/";
        if (!pid.startsWith(s)) {
            return s + pid;
        }
        return pid;
    }

    public static String shortForm(String pid) {
        return pid.replaceFirst("^info:fedora/", "");
    }
}
