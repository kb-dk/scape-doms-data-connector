package eu.scape_project.dataconnetor.doms;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.sun.jersey.core.util.Base64;
import dk.statsbiblioteket.util.Bytes;
import dk.statsbiblioteket.util.Checksums;
import eu.scape_project.dataconnetor.doms.exceptions.ParsingException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

public class WireMockFedora {
    static void setupObject(String pid, String entityIdentifier, String representationIdentifier, String fileIdentifier,
                            String username, String password, String contentModel, String title,
                            String contentDatastream, String fileName, String mimetype, String file_url,
                            String... datastreams) throws ParsingException {

        givenThat(
                WireMock.get(
                        urlEqualTo("/fedora/objects/" + URLEncoder.encode(contentModel) + "/datastreams/DS-COMPOSITE-MODEL/content?asOfDateTime="))
                        .withHeader(
                                "Authorization", equalTo(encode(username, password.getBytes())))
                        .willReturn(
                                aResponse().withHeader("Content-type", "text/xml").withBody(
                                        MockFedora.getDsComp())));

        givenThat(
                WireMock.get(
                        urlEqualTo("/fedora/objects/" + URLEncoder.encode(pid) + "/datastreams/DC/content?asOfDateTime="))
                        .withHeader(
                                "Authorization", equalTo(encode(username, password.getBytes())))
                        .willReturn(
                                aResponse().withHeader("Content-type", "text/xml").withBody(
                                        MockFedora.getDC(
                                                pid, entityIdentifier, representationIdentifier, fileIdentifier))));

        givenThat(
                WireMock.get(
                        urlEqualTo("/fedora/objects/" + URLEncoder.encode(pid) + "?format=text/xml")).withHeader(
                        "Authorization", equalTo(encode(username, password.getBytes()))).willReturn(
                        aResponse().withHeader("Content-type", "text/xml").withBody(
                                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<objectProfile xmlns=\"http://www.fedora.info/definitions/1/0/access/\"\n" +
                                "               xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
                                "               xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                                "               xsi:schemaLocation=\"http://www.fedora.info/definitions/1/0/access/ http://www.fedora.info/definitions/1/0/objectProfile.xsd\"\n" +
                                "               pid=\"" + pid + "\">\n" +
                                "    <objLabel>Newspaper batch</objLabel>\n" +
                                "    <objOwnerId></objOwnerId>\n" +
                                "    <objModels>\n" +
                                "        <model>info:fedora/" + contentModel + "</model>\n" +
                                "    </objModels>\n" +
                                "    <objCreateDate>2014-01-16T10:37:46.181Z</objCreateDate>\n" +
                                "    <objLastModDate>2014-01-16T10:38:19.752Z</objLastModDate>\n" +
                                "    <objDissIndexViewURL>\n" +
                                "        http://localhost:7880/fedora/objects/" + URLEncoder.encode(pid) + "/methods/fedora-system%3A3/viewMethodIndex\n" +
                                "    </objDissIndexViewURL>\n" +
                                "    <objItemIndexViewURL>\n" +
                                "        http://localhost:7880/fedora/objects/" + URLEncoder.encode(pid) + "/methods/fedora-system%3A3/viewItemIndex\n" +
                                "    </objItemIndexViewURL>\n" +
                                "    <objState>I</objState>\n" +
                                "</objectProfile>")));

        givenThat(
                WireMock.get(
                        urlEqualTo("/fedora/objects/" + pid + "/relationships/?subject=info:fedora/" + pid + "&format=n-triples"))
                        .withHeader(
                                "Authorization", equalTo(encode(username, password.getBytes())))
                        .willReturn(
                                aResponse().withBody(
                                        "<info:fedora/" + pid + "> <info:fedora/fedora-system:def/model#hasModel> <info:fedora/fedora-system:FedoraObject-3.0> .\n" +
                                        "<info:fedora/" + pid + "> <info:fedora/fedora-system:def/model#hasModel> <info:fedora/doms:ContentModel_DOMS> .\n")));

        givenThat(
                WireMock.get(
                        urlEqualTo("/fedora/objects/" + URLEncoder.encode(pid) + "/datastreams?format=text/xml"))
                        .withHeader(
                                "Authorization", equalTo(encode(username, password.getBytes())))
                        .willReturn(
                                aResponse().withHeader("Content-type", "text/xml").withBody(
                                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                        "<objectDatastreams xmlns=\"http://www.fedora.info/definitions/1/0/access/\"\n" +
                                        "                   xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
                                        "                   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                                        "                   xsi:schemaLocation=\"http://www.fedora.info/definitions/1/0/access/ http://www.fedora-commons.org/definitions/1/0/listDatastreams.xsd\"\n" +
                                        "                   pid=\"" + pid + "\"\n" +
                                        "                   baseURL=\"http://localhost:7880/fedora/\">\n" +
                                        getDatastreamsDefs(contentDatastream, fileName, datastreams) +
                                        "</objectDatastreams>")));


        String binaryContent = getFileContent();
        givenThat(
                WireMock.get(
                        urlEqualTo("/fedora/objects/" + URLEncoder.encode(pid) + "/datastreams/" + contentDatastream + "?asOfDateTime=&format=text/xml"))
                        .withHeader(
                                "Authorization", equalTo(encode(username, password.getBytes())))
                        .willReturn(
                                aResponse().withHeader("Content-type", "text/xml").withBody(
                                        getDatastreamContentProfile(
                                                pid, contentDatastream, binaryContent, file_url, fileName, mimetype))));


        givenThat(
                WireMock.get(
                        urlEqualTo("/fedora/objects/" + URLEncoder.encode(pid) + "/datastreams/" + contentDatastream + "/content?asOfDateTime="))
                        .withHeader(
                                "Authorization", equalTo(encode(username, password.getBytes())))
                        .willReturn(
                                aResponse().withStatus(307).withHeader("Location", file_url)));


        for (String datastream : datastreams) {
            String datastreamContent = getDatastreamContent(datastream, title);
            givenThat(
                    WireMock.get(
                            urlEqualTo("/fedora/objects/" + URLEncoder.encode(pid) + "/datastreams/" + datastream + "?asOfDateTime=&format=text/xml"))
                            .withHeader(
                                    "Authorization", equalTo(encode(username, password.getBytes())))
                            .willReturn(
                                    aResponse().withHeader("Content-type", "text/xml").withBody(
                                            getDatastreamMetadataProfile(
                                                    pid, datastream, datastreamContent))));


            givenThat(
                    WireMock.get(
                            urlEqualTo("/fedora/objects/" + URLEncoder.encode(pid) + "/datastreams/" + datastream + "/content?asOfDateTime="))
                            .withHeader(
                                    "Authorization", equalTo(encode(username, password.getBytes())))
                            .willReturn(
                                    aResponse().withHeader("Content-type", "text/xml").withBody(
                                            datastreamContent)));

        }

    }

    private static String getDatastreamContentProfile(String pid, String datastream, String content, String location,
                                               String fileName, String mimetype) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
               "<datastreamProfile xmlns=\"http://www.fedora.info/definitions/1/0/management/\"\n" +
               "                   xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
               "                   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
               "                   xsi:schemaLocation=\"http://www.fedora.info/definitions/1/0/management/ http://www.fedora.info/definitions/1/0/datastreamProfile.xsd\"\n" +
               "                   pid=\"" + pid + "\"\n" +
               "                   dsID=\"" + datastream + "\">\n" +
               "    <dsLabel>" + fileName + "</dsLabel>\n" +
               "    <dsVersionID>DC1.0</dsVersionID>\n" +
               "    <dsCreateDate>2014-01-16T10:37:46.181Z</dsCreateDate>\n" +
               "    <dsState>A</dsState>\n" +
               "    <dsMIME>" + mimetype + "</dsMIME>\n" +
               "    <dsFormatURI></dsFormatURI>\n" +
               "    <dsControlGroup>R</dsControlGroup>\n" +
               "    <dsSize>" + content.length() + "</dsSize>\n" +
               "    <dsVersionable>true</dsVersionable>\n" +
               "    <dsInfoType></dsInfoType>\n" +
               "    <dsLocation>" + location + "</dsLocation>\n" +
               "    <dsLocationType></dsLocationType>\n" +
               "    <dsChecksumType>MD5</dsChecksumType>\n" +
               "    <dsChecksum>" + Bytes.toHex(Checksums.md5(content)).toUpperCase() + "</dsChecksum>\n" +
               "</datastreamProfile>";

    }

    private static String getFileContent() throws ParsingException {
        return MockFedora.getEmptyTextMD();
    }

    private static String getDatastreamContent(String datastream, String title) throws ParsingException {
        switch (datastream) {
            case "SCAPE_RIGHTS":
                return MockFedora.getSimpleRights();
            case "SCAPE_LIFECYCLE":
                return MockFedora.getSimpleLifeCycle();
            case "SCAPE_PROVENANCE":
                return MockFedora.getSimpleProvenance();
            case "SCAPE_SOURCE":
                return MockFedora.getSimpleSource();
            case "SCAPE_DESCRIPTIVE":
                return MockFedora.getDescriptive(title);
            case "SCAPE_REPRESENTATION_TECHNICAL":
                return MockFedora.getEmptyTextMD();
            case "SCAPE_FILE_TECHNICAL":
                return MockFedora.getEmptyTextMD();
            default:
                return "";
        }


    }

    private static String getDatastreamsDefs(String contentsDatastream, String filename, String... datastreams) {

        StringBuilder builder = new StringBuilder();
        builder.append("<datastream dsid=\"")
               .append(contentsDatastream)
               .append("\" label=\"")
               .append(filename)
               .append("\" mimeType=\"application/octet-stream\"/>\n");
        for (String datastream : datastreams) {
            builder.append("<datastream dsid=\"").append(datastream).append("\" label=\"\" mimeType=\"text/xml\"/>\n");
        }
        return builder.toString();
    }

    private static String getDatastreamMetadataProfile(Object pid, Object datastream, String content) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
               "<datastreamProfile xmlns=\"http://www.fedora.info/definitions/1/0/management/\"\n" +
               "                   xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
               "                   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
               "                   xsi:schemaLocation=\"http://www.fedora.info/definitions/1/0/management/ http://www.fedora.info/definitions/1/0/datastreamProfile.xsd\"\n" +
               "                   pid=\"" + pid + "\"\n" +
               "                   dsID=\"" + datastream + "\">\n" +
               "    <dsLabel>Label</dsLabel>\n" +
               "    <dsVersionID>DC1.0</dsVersionID>\n" +
               "    <dsCreateDate>2014-01-16T10:37:46.181Z</dsCreateDate>\n" +
               "    <dsState>A</dsState>\n" +
               "    <dsMIME>text/xml</dsMIME>\n" +
               "    <dsFormatURI></dsFormatURI>\n" +
               "    <dsControlGroup>M</dsControlGroup>\n" +
               "    <dsSize>" + content.length() + "</dsSize>\n" +
               "    <dsVersionable>true</dsVersionable>\n" +
               "    <dsInfoType></dsInfoType>\n" +
               "    <dsLocation>doms:Template_Batch+DC+DC1.0</dsLocation>\n" +
               "    <dsLocationType></dsLocationType>\n" +
               "    <dsChecksumType>MD5</dsChecksumType>\n" +
               "    <dsChecksum>" + Bytes.toHex(Checksums.md5(content)).toUpperCase() + "</dsChecksum>\n" +
               "</datastreamProfile>";
    }

    public static String encode(String username, byte[] password) {
        try {

            final byte[] prefix = (username + ":").getBytes("UTF-8");
            final byte[] usernamePassword = new byte[prefix.length + password.length];

            System.arraycopy(prefix, 0, usernamePassword, 0, prefix.length);
            System.arraycopy(password, 0, usernamePassword, prefix.length, password.length);

            return "Basic " + new String(Base64.encode(usernamePassword), "ASCII");
        } catch (UnsupportedEncodingException ex) {
            // This should never occur
            throw new RuntimeException(ex);
        }
    }
}
