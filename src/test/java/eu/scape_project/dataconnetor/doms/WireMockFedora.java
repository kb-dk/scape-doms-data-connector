package eu.scape_project.dataconnetor.doms;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.sun.jersey.core.util.Base64;
import dk.statsbiblioteket.util.Bytes;
import dk.statsbiblioteket.util.Checksums;
import dk.statsbiblioteket.util.Pair;
import eu.scape_project.dataconnetor.doms.exceptions.ParsingException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Pattern;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;

public class WireMockFedora {
    static void setupObject(String pid, String entityIdentifier, String representationIdentifier, String fileIdentifier,
                            String username, String password, String contentModel, String title,
                            String contentDatastream, String fileName, String mimetype, String file_url,
                            String... datastreams) throws ParsingException {

        setupContentModel(username, password, contentModel);

        setupDC(pid, entityIdentifier, representationIdentifier, fileIdentifier, username, password);

        setupObjectProfile(pid, username, password, contentModel);

        setupRelations(pid, username, password);

        setupDatastreamList(pid, username, password, contentDatastream, fileName, datastreams);


        setupBinaryDatastream(pid, username, password, contentDatastream, fileName, mimetype, file_url);


        setupMetadataDatastreams(pid, username, password, title, datastreams);

    }

    private static void setupMetadataDatastreams(String pid, String username, String password, String title,
                                                 String[] datastreams) throws ParsingException {
        for (String datastream : datastreams) {
            setupMetadataDatastream(pid, username, password, title, datastream);
        }
    }

    private static void setupMetadataDatastream(String pid, String username, String password, String title,
                                                String datastream) throws ParsingException {
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

    private static void setupBinaryDatastream(String pid, String username, String password, String contentDatastream,
                                              String fileName, String mimetype, String file_url) throws
                                                                                                 ParsingException {
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
    }

    private static void setupDatastreamList(String pid, String username, String password, String contentDatastream,
                                            String fileName, String[] datastreams) {
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
    }

    private static void setupRelations(String pid, String username, String password) {
        givenThat(
                WireMock.get(
                        urlEqualTo("/fedora/objects/" + pid + "/relationships/?subject=info:fedora/" + pid + "&format=n-triples"))
                        .withHeader(
                                "Authorization", equalTo(encode(username, password.getBytes())))
                        .willReturn(
                                aResponse().withBody(
                                        "<info:fedora/" + pid + "> <info:fedora/fedora-system:def/model#hasModel> <info:fedora/fedora-system:FedoraObject-3.0> .\n" +
                                        "<info:fedora/" + pid + "> <info:fedora/fedora-system:def/model#hasModel> <info:fedora/doms:ContentModel_DOMS> .\n")));
    }

    private static void setupObjectProfile(String pid, String username, String password, String contentModel) {
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
    }

    private static void setupDC(String pid, String entityIdentifier, String representationIdentifier,
                                String fileIdentifier, String username, String password) {
        givenThat(
                WireMock.get(
                        urlEqualTo("/fedora/objects/" + URLEncoder.encode(pid) + "/datastreams/DC/content?asOfDateTime="))
                        .withHeader(
                                "Authorization", equalTo(encode(username, password.getBytes())))
                        .willReturn(
                                aResponse().withHeader("Content-type", "text/xml").withBody(
                                        MockFedora.getDC(
                                                pid, entityIdentifier, representationIdentifier, fileIdentifier))));
    }

    private static void setupContentModel(String username, String password, String contentModel) {
        givenThat(
                WireMock.get(
                        urlEqualTo("/fedora/objects/" + URLEncoder.encode(contentModel) + "/datastreams/DS-COMPOSITE-MODEL/content?asOfDateTime="))
                        .withHeader(
                                "Authorization", equalTo(encode(username, password.getBytes())))
                        .willReturn(
                                aResponse().withHeader("Content-type", "text/xml").withBody(
                                        MockFedora.getDsComp())));
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

    public static void setupForNewObject(String pid, String entityIdentifier, String representationIdentifier,
                                         String fileIdentifier, String username, String password,
                                         String scape_content_model, String title, String rep_title,
                                         String scape_file_content, String fileName, String mimeType, String fileUrl,
                                         String scape_descriptive, String descriptive_content,
                                         String scape_file_technical, String file_technical_content,
                                         String scape_lifecycle, String lifecycle_content, String scape_provenance,
                                         String provenance_content, String scape_representation_technical,
                                         String representation_technical_content, String scape_rights,
                                         String rights_content, String scape_source, String source_content) {
        setupEmptyIdentifierSearch(username, password);
        setupPidGenerator(pid);
        setupNewObject(username, password, pid, entityIdentifier, representationIdentifier, fileIdentifier);

        setupAddRelations(pid, scape_content_model, username, password);
        setupContentModel(username, password, scape_content_model);

        setupWritableDatastreams(
                username,
                password,
                pid,
                new Pair<>(scape_descriptive, descriptive_content),
                new Pair<>(scape_representation_technical, representation_technical_content),
                new Pair<>(scape_source, source_content),
                new Pair<>(scape_rights, rights_content),
                new Pair<>(scape_provenance, provenance_content),
                new Pair<>(scape_lifecycle, lifecycle_content),
                new Pair<>(scape_file_technical, file_technical_content)
                );
        setupWritableLabel(pid, username, password, rep_title);

        setupWritableContentStream(username, password, pid, scape_file_content, fileName, fileUrl, mimeType);
        setupWritableVersion(pid, "SCAPE_VERSIONS", username, password);

    }

    private static void setupWritableVersion(String pid, String datastream, String username, String password) {

        givenThat(
                WireMock.post(
                        urlEqualTo(
                                "/fedora/objects/" + URLEncoder.encode(pid) + "/datastreams/" + datastream + "?" +
                                "mimeType=text/xml&" +
                                "logMessage=logmessage&" +
                                "controlGroup=M")).withHeader(
                        "Authorization", equalTo(encode(username, password.getBytes())))
                        //.withRequestBody(equalTo(datastream.getRight().toString()))
                        .willReturn(aResponse().withStatus(201)));
    }

    private static void setupWritableContentStream(String username, String password, String pid,
                                                   String scape_file_content, String fileName, String fileUrl,
                                                   String mimeType) {
        givenThat(
                WireMock.post(
                        urlEqualTo(
                                "/fedora/objects/" + URLEncoder.encode(pid) + "/datastreams/" + scape_file_content +
                                "?controlGroup=R&dsLocation=" + fileUrl + "&dsLabel=" + fileName +
                                "&formatURI=unknown&mimeType=" + mimeType + "&logMessage=logmessage"))
                        .withHeader(
                                "Authorization", equalTo(encode(username, password.getBytes())))
                        .willReturn(aResponse().withStatus(200)));
    }

    private static void setupWritableLabel(String pid, String username, String password, String title) {

        givenThat(
                WireMock.put(
                        urlEqualTo(
                                "/fedora/objects/" + URLEncoder.encode(pid) + "?" +
                                "label=" + URLEncoder.encode(title) + "&" +
                                "logMessage=logmessage"))
                        .withHeader(
                                "Authorization", equalTo(encode(username, password.getBytes())))
                        .willReturn(aResponse().withStatus(200)));

    }

    private static void setupWritableDatastreams(String username, String password, String pid,
                                                 Pair... datastreams) {

        for (Pair datastream : datastreams) {
            givenThat(
                    WireMock.put(
                            urlMatching(
                                    "/fedora/objects/" + URLEncoder.encode(pid) + "/datastreams/" + datastream.getLeft() + "?" +
                                    "mimeType=text/xml&" +
                                    "logMessage=.*&" +
                                    "checksumType=MD5&" +
                                    "checksum=.*"))
                            .withHeader(
                                    "Authorization", equalTo(encode(username, password.getBytes())))
                            .willReturn(aResponse().withStatus(404)));

            givenThat(
                    WireMock.post(
                            urlEqualTo(
                                    "/fedora/objects/" + URLEncoder.encode(pid) + "/datastreams/" + datastream.getLeft() + "?" +
                                    "mimeType=text/xml&" +
                                    "logMessage=logmessage&" +
                                    "checksumType=MD5&" +
                                    "checksum=" + Bytes.toHex(Checksums.md5(datastream.getRight().toString()))
                                                       .toLowerCase() + "&controlGroup=M")).withHeader(
                            "Authorization", equalTo(encode(username, password.getBytes())))
                            //.withRequestBody(equalTo(datastream.getRight().toString()))
                            .willReturn(aResponse().withStatus(201)));

        }
    }

    private static void setupAddRelations(String pid, Object scape_ContentModel, String username, String password) {

        givenThat(
                WireMock.post(
                        urlEqualTo(
                                "/fedora/objects/" + pid + "/relationships/new?" +
                                "subject=info:fedora/" + pid + "&" +
                                "predicate=info:fedora/fedora-system:def/model%23hasModel&" +
                                "object=info:fedora/" + scape_ContentModel + "&" +
                                "isLiteral=false"))
                        .withHeader(
                                "Authorization", equalTo(encode(username, password.getBytes())))
                        .willReturn(aResponse().withStatus(201)));


    }

    private static void setupNewObject(String username, String password, String pid, Object entity_identifier,
                                       Object representation_identifier, Object file_identifier) {
        givenThat(
                WireMock.post(
                        urlEqualTo("/fedora/objects/" + URLEncoder.encode(pid) + "?state=I")).withHeader(
                        "Authorization", equalTo(encode(username, password.getBytes()))).withRequestBody(
                        equalTo(
                                "<foxml:digitalObject xmlns:foxml=\"info:fedora/fedora-system:def/foxml#\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" PID=\"" + pid + "\" VERSION=\"1.1\" xsi:schemaLocation=\"info:fedora/fedora-system:def/foxml# http://www.fedora.info/definitions/1/0/foxml1-1.xsd\">\n" +
                                "    <foxml:objectProperties>\n" +
                                "        <foxml:property NAME=\"info:fedora/fedora-system:def/model#state\" VALUE=\"Inactive\"/>\n" +
                                "        <foxml:property NAME=\"info:fedora/fedora-system:def/model#label\" VALUE=\"\"/>\n" +
                                "    </foxml:objectProperties>\n" +
                                "    <foxml:datastream CONTROL_GROUP=\"X\" ID=\"DC\" STATE=\"A\" VERSIONABLE=\"true\">\n" +
                                "        <foxml:datastreamVersion FORMAT_URI=\"http://www.openarchives.org/OAI/2.0/oai_dc/\" ID=\"DC1.0\" LABEL=\"Dublin Core Record for this object\" MIMETYPE=\"text/xml\">\n" +
                                "            <foxml:xmlContent>\n" +
                                "                <oai_dc:dc xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:oai_dc=\"http://www.openarchives.org/OAI/2.0/oai_dc/\">\n" +
                                "                    <dc:identifier>" + pid + "</dc:identifier>\n" +
                                "                <dc:identifier>scape-entity:" + entity_identifier + "</dc:identifier><dc:identifier>scape-representation:" + representation_identifier + "</dc:identifier><dc:identifier>scape-file:" + file_identifier + "</dc:identifier></oai_dc:dc>\n" +
                                "            </foxml:xmlContent>\n" +
                                "        </foxml:datastreamVersion>\n" +
                                "    </foxml:datastream>\n" +
                                "    <foxml:datastream CONTROL_GROUP=\"X\" ID=\"RELS-EXT\" STATE=\"A\" VERSIONABLE=\"true\">\n" +
                                "        <foxml:datastreamVersion FORMAT_URI=\"info:fedora/fedora-system:FedoraRELSExt-1.0\" ID=\"RELS-EXT.0\" LABEL=\"Relationships\" MIMETYPE=\"application/rdf+xml\">\n" +
                                "            <foxml:xmlContent>\n" +
                                "                <rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n" +
                                "\n" +
                                "                    <rdf:Description rdf:about=\"info:fedora/" + pid + "\">\n" +
                                "                        \n" +
                                "                    </rdf:Description>\n" +
                                "\n" +
                                "                </rdf:RDF>\n" +
                                "            </foxml:xmlContent>\n" +
                                "        </foxml:datastreamVersion>\n" +
                                "    </foxml:datastream>\n" +
                                "</foxml:digitalObject>")).willReturn(aResponse().withStatus(201).withBody(pid)));

    }

    private static void setupPidGenerator(String pid) {
        givenThat(
                WireMock.get(
                        urlMatching("/pidgenerator-service/rest/pids/generatePid/.*")).willReturn(
                        aResponse().withBody(pid)));

    }

    private static void setupEmptyIdentifierSearch(String username, String password) {
        givenThat(
                WireMock.get(
                        urlMatching(Pattern.quote("/fedora/objects?pid=true&query=identifier~") + ".*")).withHeader(
                        "Authorization", equalTo(encode(username, password.getBytes()))).willReturn(
                        aResponse().withBody(
                                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<result xmlns=\"http://www.fedora.info/definitions/1/0/types/\" xmlns:types=\"http://www.fedora.info/definitions/1/0/types/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.fedora.info/definitions/1/0/types/ http://localhost:7880/fedora/schema/findObjects.xsd\">\n" +
                                "  <resultList>\n" +
                                "  <objectFields>\n" +
                                //"      <pid>" + pid + "</pid>\n" +
                                "  </objectFields>\n" +
                                "  </resultList>\n" +
                                "</result>")));

    }
}
