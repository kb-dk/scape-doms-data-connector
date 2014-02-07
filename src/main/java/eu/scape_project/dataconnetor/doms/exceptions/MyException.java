package eu.scape_project.dataconnetor.doms.exceptions;

public abstract class MyException extends Exception{
    protected MyException() {
    }

    protected MyException(String message) {
        super(message);
    }

    protected MyException(String message, Throwable cause) {
        super(message, cause);
    }

    protected MyException(Throwable cause) {
        super(cause);
    }

    protected MyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
