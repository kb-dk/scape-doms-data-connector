package eu.scape_project.dataconnetor.doms.service;

import dk.statsbiblioteket.doms.webservices.authentication.Credentials;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

public class AbstractService {

    @Context
    private HttpServletRequest request;


    public Credentials getCredentials() {
        Credentials creds = (Credentials) request.getAttribute("Credentials");
        if (creds == null) {
            //            log.warn("Attempted call at Central without credentials");
            creds = new Credentials("", "");
        }
        return creds;
    }
}
