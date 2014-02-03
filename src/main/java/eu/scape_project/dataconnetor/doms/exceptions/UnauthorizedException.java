package eu.scape_project.dataconnetor.doms.exceptions;

public class UnauthorizedException extends Exception {
    public UnauthorizedException(Exception e) {
        super(e);
    }
}
