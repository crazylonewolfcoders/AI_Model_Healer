package io.github.crazy.lonewolf.coders.omnij.plugin.memory.generator;

import io.github.crazy.lonewolf.coders.omnij.datasource.db.OmniJHibernateBuildManager;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.beans.Template;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.parser.json.JsonTemplateGenerator;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;


public class MavenDBDataGenerator implements  IMetaDataTemplateGenerator{
    private String url;
    private String username;
    private String password;
    private String appName;

    ClassLoader classLoader;
    MavenDBDataGenerator(String url, String username, String password, String appName, File outputDirectory) throws MalformedURLException {
        this.url = url;
        this.username = username;
        this.password = password;
        this.appName = appName;
        URL classesUrl = outputDirectory.toURI().toURL();
        ClassLoader  pluginClassLoader = Thread.currentThread().getContextClassLoader();

        // 2. Create a isolated ClassLoader targeting only the host app's classes
        URLClassLoader appClassLoader = new URLClassLoader(new URL[]{classesUrl},pluginClassLoader);
        this.classLoader = appClassLoader;
    }

    @Override
    public void generateMetaData(String metaDataLocation, String scanbasepackages) {


    }

    @Override
    public void generateMetaData(String scanbasepackages) {
        JsonTemplateGenerator templateGenerator = new JsonTemplateGenerator();
        try{
            List<Template> templateList = templateGenerator.generateTemplates(classLoader,scanbasepackages);
            OmniJHibernateBuildManager bm = new OmniJHibernateBuildManager(url, username, password);
            for (Template template : templateList) {
              bm.saveOrUpdateTemplate(template.clazzName(),appName,template.template());
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }



}
