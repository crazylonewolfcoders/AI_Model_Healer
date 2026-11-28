package io.github.crazy.lonewolf.coders.omnij.datasource.service;

import io.github.crazy.lonewolf.coders.omnij.datasource.annotation.OmniJTransactional;
import io.github.crazy.lonewolf.coders.omnij.datasource.db.OmniJTemplateEnitty;
import io.github.crazy.lonewolf.coders.omnij.datasource.repository.TemplateRepository;
import org.springframework.stereotype.Service;

@Service
@OmniJTransactional
public class TemplateService {

    private final TemplateRepository templateRepository;
    public TemplateService(TemplateRepository  templateRepository) {
        this.templateRepository = templateRepository;
    }

    public OmniJTemplateEnitty getTemplate(String className){
         return   this.templateRepository.findById(className).orElseThrow(()->new IllegalArgumentException("template not found"));
    }
}
