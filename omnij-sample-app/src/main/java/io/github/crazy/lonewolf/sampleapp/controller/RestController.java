package io.github.crazy.lonewolf.sampleapp.controller;

import io.github.crazy.lonewolf.sampleapp.model.CostAnalysis;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.annotation.OmniJsonResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

@org.springframework.web.bind.annotation.RestController
@RequestMapping("/api/sample")
@RequiredArgsConstructor
@Slf4j
public class RestController {


    @PostMapping(path = "/test", consumes = MediaType.APPLICATION_JSON_VALUE)
    public String testModelMapping(@OmniJsonResolver CostAnalysis costAnalysis) throws IOException {

        try {
         return    costAnalysis.toString();
        }catch (Throwable e){
            return e.getLocalizedMessage();
        }

    }

}
