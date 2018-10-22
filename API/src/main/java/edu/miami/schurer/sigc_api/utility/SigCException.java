package edu.miami.schurer.sigc_api.utility;

public class SigCException extends Exception {


    private Integer statusCode = 500;
    // Parameterless Constructor
    public SigCException() {}

    // Constructor that accepts a message
    public SigCException(String message)
    {
        super(message);
    }
    // Constructor that accepts a message
    public SigCException(String message,Integer code)
    {
        super(message);
        this.statusCode=code;
    }

    public Integer getStatusCode() {
        return statusCode;
    }
}
