package com.lazycoder;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.lang.model.util.Elements;

@SupportedAnnotationTypes("val")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class Processor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith((Class<? extends Annotation>) val.class)) {
            if (element.getKind() != ElementKind.LOCAL_VARIABLE) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "@Val can only be applied to local variables.");
                return true;
            }

            VariableElement varElement = (VariableElement) element;
            TypeMirror varType = varElement.asType();
            Elements elementUtils = processingEnv.getElementUtils();
            String typeName = varType.toString();
            System.out.println("Processing");
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Variable " + varElement.getSimpleName() + " is inferred as type " + typeName);
        }
        return true;
    }
    
}
