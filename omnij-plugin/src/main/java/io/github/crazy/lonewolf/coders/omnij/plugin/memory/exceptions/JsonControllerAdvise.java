package io.github.crazy.lonewolf.coders.omnij.plugin.memory.exceptions;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.beans.Resolution;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.beans.Template;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.beans.TemplateKey;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.ITemplateContainer;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.parser.ITemplateGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;

@ControllerAdvice
@Slf4j
public class JsonControllerAdvise {
    private final ITemplateContainer container;

    private final  ITemplateGenerator jsonTemplateGenerator;

    public JsonControllerAdvise (ITemplateContainer container, @Qualifier("JsonGenerator") ITemplateGenerator jsonTemplateGen ){
        this.container = container;
        this.jsonTemplateGenerator = jsonTemplateGen;
    }


    @ExceptionHandler(JsonTemplateProcessingException.class)
    @Order(1)
    public void handleTemplateProcessingException(JsonTemplateProcessingException ex) throws JsonMappingException {
      log.error("Recieved Error when trying to process template data ", ex);
      log.info("Adding the failed json to the resolution failure queue");
      Template t = ex.getTemplateData();
      String className = t.clazzName();
      TemplateKey info = new TemplateKey();
      info.setClazz(className);
      container.recordFailure(info,t);
        throw new JsonMappingException(
                "Template processing failed for " + className
        );


    }

    @ExceptionHandler(JsonTemplateNotFoundException.class)
    @Order(1)
    public void handleTemplateNotFoundException(JsonTemplateNotFoundException ex) throws JsonMappingException {
        log.error("Recieved Error when trying to process template data ", ex);
        log.info("Adding the failed json to the resolution failure queue");
        Template template = null;
try {
     String t = ((ObjectNode)jsonTemplateGenerator.generateTemplate(ex.getInstance())).toString();
     template = new Template(ex.getInstance().getName(),t, Instant.now(),false,"","", Resolution.ENGINE,"","");

}catch (Exception e){
    throw new JsonMappingException(
            "Template processing failed for " + ex.getInstance().getName()
    );
}finally {
    TemplateKey info = new TemplateKey();
    info.setClazz(ex.getInstance().getName());
    container.recordFailure(info,template);
}

        throw new JsonMappingException(
                "Template processing failed for " + ex.getInstance().getName()
        );


    }
}
