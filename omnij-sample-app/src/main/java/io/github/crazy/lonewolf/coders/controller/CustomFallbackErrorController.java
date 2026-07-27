package io.github.crazy.lonewolf.coders.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.Nullable;
import org.springframework.core.Ordered;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.JacksonJsonView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class CustomFallbackErrorController implements HandlerExceptionResolver, Ordered {

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE; // Runs after all controller advices fail
    }


    @Override
    public @Nullable ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, @Nullable Object handler, Exception ex) {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        // Use Jackson view to directly write JSON to the response body
        ModelAndView modelAndView = new ModelAndView(new JacksonJsonView());

        Map<String, Object> errorDetails = new LinkedHashMap<>();
        errorDetails.put("status", 500);
        errorDetails.put("error", "Internal Pipeline Failure");
        errorDetails.put("message", ex.getMessage());

        modelAndView.addAllObjects(errorDetails);
        return modelAndView;
    }
}