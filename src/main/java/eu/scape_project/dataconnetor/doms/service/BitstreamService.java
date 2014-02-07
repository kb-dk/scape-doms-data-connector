package eu.scape_project.dataconnetor.doms.service;

import javax.ws.rs.Path;

@Path("/bitstream")
public class BitstreamService {

    //BITSTREAMS HAVE NO USEFUL IDENTIFIER IN THE DATAMODEL SO THIS CANNOT BE SUPPORTED

    /**
     * 5.4.9 Retrieve named bit streams
     For fetching a named subset of Files, such as an entry in an ARC container, the
     implementation exposes a HTTP GET method. The mandatory parameter <id> is
     the identifier of the requested bit stream in the Intellectual Entity. Depending on
     the Storage Strategy the implementation returns the bit stream directly in the
     response body, or it redirects the request using HTTP 302 to the referenced
     content. This requires special care when using Referenced Content as a Storage
     Strategy since the implementation is only able to redirect to referenced bit
     streams, making the redirect target responsible for answering the request
     properly.
     Path:
     /bitstream/<entity-id>/<rep-id>/<file-id>/<bitstream-id>/<version-id>
     Method
     HTTP/1.1 GET
     Parameters
     entity-id: the id of the Intellectual Entity
     rep-id: the id of the Representation
     file-id: the id of the File
     bitstream-id: the id of the requested binary content
     version-id: the version of the requested bit stream's parent Intellectual Entity
     (optional)
     Produces
     the binary content associated requested or a redirect to the binary content.
     Content-Type
     depends on content's type, but defaults to application/octet-stream.
     */
}
