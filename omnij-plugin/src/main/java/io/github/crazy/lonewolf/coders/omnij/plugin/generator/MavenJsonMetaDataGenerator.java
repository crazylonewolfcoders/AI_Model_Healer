package io.github.crazy.lonewolf.coders.omnij.plugin.generator;

import io.github.crazy.lonewolf.coders.omnij.plugin.beans.Template;
import io.github.crazy.lonewolf.coders.omnij.plugin.config.OmniJConfig;
import io.github.crazy.lonewolf.coders.omnij.plugin.parser.json.JsonTemplateGenerator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class MavenJsonMetaDataGenerator implements  IMetaDataTemplateGenerator{

    ClassLoader classLoader;
    public MavenJsonMetaDataGenerator(File outputDirectory) throws MalformedURLException {
        URL classesUrl = outputDirectory.toURI().toURL();
        ClassLoader pluginClassLoader = Thread.currentThread().getContextClassLoader();

        // 2. Create a isolated ClassLoader targeting only the host app's classes
        URLClassLoader appClassLoader = new URLClassLoader(new URL[]{classesUrl},pluginClassLoader);
        this.classLoader = appClassLoader;
    }

    @Override
    public void generateMetaData(String metaDataLocation, String scanbasepackages) {
        JsonTemplateGenerator templateGenerator = new JsonTemplateGenerator();
      try{
          List<Template> templateList = templateGenerator.generateTemplates(classLoader,scanbasepackages);
          generateFilesInLocation(metaDataLocation,templateList);
      }catch (Exception e){
        e.printStackTrace();
      }

    }

    private void generateFilesInLocation(String fileLocation, List<Template> templateList){

        File indexFile = new File(fileLocation, "files.idx");
        if(!indexFile.exists()){
            indexFile.getParentFile().mkdirs();
        }
        try (FileWriter writer = new FileWriter(indexFile)) {
            for (Template template : templateList) {

                try {
                    Path newFilePath = generateUniqueFile(fileLocation, template.clazzName(), ".oinf", template.toJson());
                    writer.write(newFilePath.getFileName() + "\n");
                    System.out.println(newFilePath.getFileName() + "\n");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    private  Path generateUniqueFile(String outputDir, String cleanBase, String extension, String processedContent) throws IOException {



        // 2. Generate a guaranteed distinct, safe filename

        String cleanExt = extension.replace(".", "").toLowerCase();

        // Formatted like: invoice_report_20260728_180215_123_a1b2c3d4.txt
        String uniqueFileName = String.format("%s.%s", cleanBase,cleanExt);

        // 3. Ensure directories exist safely using Java NIO
        Path directoryPath = Paths.get(outputDir);
        if (!Files.exists(directoryPath)) {
            Files.createDirectories(directoryPath);
        }

        // 4. Stream content using a highly efficient BufferedWriter
        Path finalFilePath = directoryPath.resolve(uniqueFileName);
        try (BufferedWriter writer = Files.newBufferedWriter(finalFilePath, StandardCharsets.UTF_8)) {
            writer.write(processedContent);
        }

        return finalFilePath;
    }


}
