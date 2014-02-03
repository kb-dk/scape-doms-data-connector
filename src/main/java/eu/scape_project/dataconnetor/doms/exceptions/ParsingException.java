package eu.scape_project.dataconnetor.doms.exceptions;

public class ParsingException extends Exception {
    public ParsingException(Exception e) {
        super(e);
    }

    public ParsingException() {
        super();
    }

    public ParsingException(String s) {
        super(s);
    }
}
