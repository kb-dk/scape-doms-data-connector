package eu.scape_project.dataconnetor.doms.service;

public class EntityVersionList {

    /**
     * 5.4.7 Retrieve a version list for an Intellectual Entity
     In order to get a list of all versions of an Intellectual Entity a plain GET request
     can be sent to the implementation with the <id> parameter indicating which
     entity's versions to list. If successful the response consists of the Intellectual
     Entity's version identifiers in a XML representation
     Path:
     /entity-version-list/<entity-id>
     Parameters
     entity-id: the id of the Intellectual Entity
     Method
     HTTP/1.1 GET
     Produces
     A XML representation of all the entities version ids.
     Content-Type
     text/xml
     */
}
