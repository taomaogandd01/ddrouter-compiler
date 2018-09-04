package com.hk01.uikit.ddrouter_compiler;

import com.google.auto.service.AutoService;
import com.hk01.uikit.ddrouter_annotation.RouteClass;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

@AutoService(Processor.class)
public class AnnotationProcessor extends AbstractProcessor {
    private static final String PROCESSOR_NAME = "DDRouter$$Processor";
    private static final String IMPORT_PATH = "com.hk01.uikit";

    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        filer = processingEnvironment.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(RouteClass.class);
        StringBuilder importBuilder = new StringBuilder();
        StringBuilder dataBuilder = new StringBuilder();
        importBuilder.append("package " + IMPORT_PATH + ";\n\n");
        for (Element element : elements) {
            TypeElement typeElement = (TypeElement) element;
            String typeName = typeElement.getQualifiedName().toString();
            RouteClass routeClass = element.getAnnotation(RouteClass.class);
            dataBuilder.append("        routeMetaMap.put(\"" + routeClass.name() + "\", ")
                    .append("new RouteMeta(RouteType.ACTIVITY, ")
                    .append(typeElement.getSimpleName().toString() + ".class, ")
                    .append("\"" + routeClass.name() + "\"));\n");
            importBuilder.append("import " + typeName + ";\n");

        }
        importBuilder.append("import com.hk01.uikit.ddrouter.IRouteGroup;\n")
                .append("import com.hk01.uikit.ddrouter.RouteMeta;\n")
                .append("import com.hk01.uikit.ddrouter.RouteType;\n")
                .append("import java.util.Map;\n\n")
                .append("public class ").append(PROCESSOR_NAME).append(" implements ").append("IRouteGroup {\n\n")
                .append("    @Override\n")
                .append("    public void loadInto(Map<String, RouteMeta> routeMetaMap) {\n").toString();

        Writer writer = null;
        try {
            JavaFileObject javaFileObject = filer.createSourceFile(PROCESSOR_NAME);
            writer = javaFileObject.openWriter();
            writer.write(importBuilder.toString());
            writer.write(dataBuilder.toString());
            writer.write(generateRouteLast());
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    private String generateRouteLast() {
        return new StringBuilder().append("    }\n")
                .append("}").toString();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new HashSet<>();
        set.add(RouteClass.class.getCanonicalName());
        return set;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
