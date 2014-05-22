package eu.scape_project.dataconnetor.doms.service;

import javax.ws.rs.Path;

@Path("/sru")
public class SRUService extends AbstractService {

    //TODO

    /**
     * 5.4.10
     Search Intellectual Entities in a collection
     For digital object discovery the implementation exposes a SRU search endpoint.
     The endpoint implements the SRU specifications by the Library of Congress for
     Internet Search queries, utilizing CQL, a standard syntax for representing queries,
     and exposes this functionality via a HTTP GET endpoint. Pagination is done via
     the SRU parameters startRecord and maximumRecords 6
     Path
     /sru/entities
     Parameters
     see SRU specification 7
     Method
     HTTP/1.1 GET
     Produces
     A XML representation as specified by SRU
     Content-Type
     text/xml
     */

    /**
     * 5.4.11
     Search Representations in a collection
     For discovering Representations the implementation exposes a SRU search
     endpoint. The endpoint implements the SRU specifications by the Library of
     Congress for Internet Search queries, utilizing CQL, a standard syntax for
     representing queries, and exposes this functionality via a HTTP GET endpoint.
     Pagination is done via the SRU parameters startRecord and maximumRecords
     Path
     /sru/representations
     Parameters
     see SRU specification
     Method
     HTTP/1.1 GET
     Produces
     A XML representation as specified by SRU
     Content-Type
     text/xml
     */

    /**
     * 5.4.12
     Search Files in a collection
     For discovering Files the implementation exposes a SRU search endpoint. The
     endpoint implements the SRU specifications by the Library of Congress for
     Internet Search queries, utilizing CQL, a standard syntax for representing queries,
     and exposes this functionality via a HTTP GET endpoint. Pagination is done via
     the SRU parameters startRecord and maximumRecords
     Path
     /sru/files
     Parameters
     see SRU specification
     Method
     HTTP/1.1 GET
     Produces
     A XML representation as specified by SRU
     Content-Type
     text/xml
     */
}
