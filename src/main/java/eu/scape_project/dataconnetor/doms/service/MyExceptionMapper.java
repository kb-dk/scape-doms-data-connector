package eu.scape_project.dataconnetor.doms.service;

import eu.scape_project.dataconnetor.doms.exceptions.AlreadyExistsException;
import eu.scape_project.dataconnetor.doms.exceptions.CommunicationException;
import eu.scape_project.dataconnetor.doms.exceptions.ConfigurationException;
import eu.scape_project.dataconnetor.doms.exceptions.MyException;
import eu.scape_project.dataconnetor.doms.exceptions.NotFoundException;
import eu.scape_project.dataconnetor.doms.exceptions.ParsingException;
import eu.scape_project.dataconnetor.doms.exceptions.UnauthorizedException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class MyExceptionMapper implements ExceptionMapper<MyException> {

    @Override
    public Response toResponse(MyException exception) {
        if (exception instanceof AlreadyExistsException) {
            AlreadyExistsException alreadyExistsException = (AlreadyExistsException) exception;
            return Response.status(Response.Status.CONFLICT).build();
        }
        if (exception instanceof CommunicationException) {
            CommunicationException communicationException = (CommunicationException) exception;
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        if (exception instanceof ConfigurationException) {
            ConfigurationException configurationException = (ConfigurationException) exception;
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        if (exception instanceof NotFoundException) {
            NotFoundException notFoundException = (NotFoundException) exception;
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        if (exception instanceof ParsingException) {
            ParsingException parsingException = (ParsingException) exception;
            return Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE).build();
        }
        if (exception instanceof UnauthorizedException) {
            UnauthorizedException unauthorizedException = (UnauthorizedException) exception;
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        return Response.serverError().build();
    }
}
