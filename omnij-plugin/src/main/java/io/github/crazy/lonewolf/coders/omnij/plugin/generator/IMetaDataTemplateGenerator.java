package io.github.crazy.lonewolf.coders.omnij.plugin.generator;

import io.github.crazy.lonewolf.coders.omnij.plugin.config.OmniJConfig;
import io.github.crazy.lonewolf.coders.omnij.plugin.parser.ITemplateEngine;

public interface IMetaDataTemplateGenerator {
    public void generateMetaData(String metaDataLocation, String scanbasepackages);
}
