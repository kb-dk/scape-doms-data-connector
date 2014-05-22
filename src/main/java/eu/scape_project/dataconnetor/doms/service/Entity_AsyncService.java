package eu.scape_project.dataconnetor.doms.service;

import javax.ws.rs.Path;

@Path("/entity-async")
public class Entity_AsyncService extends AbstractService {

    /**
     * 5.4.5 Ingest an Intellectual Entity asynchronously
     Ingestion is done by sending a SIP to this endpoint. The method returns instantly
     and supplies the User with an ID which can be used to request the status of the
     ingestion.
     Path
     /entity-async
     Method
     HTTP/1.1 POST
     Parameters
     Consumes
     A XML representation of the entity
     Produces
     An Identifier which can be used to request the lifecycle status of the digital object
     ingested.
     Content-Type
     text/plain
     */
    //TODO
}
