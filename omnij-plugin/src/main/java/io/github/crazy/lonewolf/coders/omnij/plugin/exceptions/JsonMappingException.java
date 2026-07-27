package io.github.crazy.lonewolf.coders.omnij.plugin.exceptions;

public class JsonMappingException extends Exception{
    public JsonMappingException(String s) {
        super(s);
    }
    public JsonMappingException(Exception e){
        super(e);
    }
}
