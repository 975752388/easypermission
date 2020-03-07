package com.zky.compiler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.zky.annotation.annotation.PermissionCancel;
import com.zky.annotation.annotation.PermissionDenied;
import com.zky.annotation.annotation.PermissionGranted;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

@AutoService(Processor.class)
//@SupportedAnnotationTypes({"com.zky.annotation.annotation.PermissionGranted"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class PermissionProccessor extends AbstractProcessor {
    private Elements elementUtils;
    private Filer filer;
    private Types typeUtils;
    private Messager messager;
    private static final String CLASS_SUFIX="$Permission";
    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        elementUtils = processingEnvironment.getElementUtils();
        filer = processingEnvironment.getFiler();
        typeUtils = processingEnvironment.getTypeUtils();
        messager = processingEnvironment.getMessager();
        System.out.println("apt init");
        messager.printMessage(Diagnostic.Kind.NOTE,"apt init");
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new LinkedHashSet<>();
        set.add(PermissionGranted.class.getCanonicalName());
        return set;
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        Set<? extends Element> needPermissionElements = roundEnvironment.getElementsAnnotatedWith(PermissionGranted.class);
        Map<String, List<ExecutableElement>> needPermissionMap = new HashMap<>();

        for (Element element : needPermissionElements) {
            messager.printMessage(Diagnostic.Kind.NOTE,"被注解的方法有："+element.getSimpleName().toString());
            ExecutableElement executableElement = (ExecutableElement) element;
            String activityName = getActivityName(executableElement);
            List<ExecutableElement> executableElements = needPermissionMap.get(activityName);
            if (executableElements==null){
                executableElements = new ArrayList<>();
                needPermissionMap.put(activityName,executableElements);
            }
            executableElements.add(executableElement);
            messager.printMessage(Diagnostic.Kind.NOTE,"被注解的类有："+executableElement.getSimpleName().toString());
        }




        Set<? extends Element> permissionDeniedElements = roundEnvironment.getElementsAnnotatedWith(PermissionDenied.class);
        Map<String, List<ExecutableElement>> permissionDeniedMap = new HashMap<>();

        for (Element element : permissionDeniedElements) {
            ExecutableElement executableElement = (ExecutableElement) element;
            String activityName = getActivityName(executableElement);
            List<ExecutableElement> executableElements = permissionDeniedMap.get(activityName);
            if (executableElements==null){
                executableElements = new ArrayList<>();
                permissionDeniedMap.put(activityName,executableElements);
            }
            executableElements.add(executableElement);
          //  messager.printMessage(Diagnostic.Kind.NOTE,"被注解的类有："+executableElement.getSimpleName().toString());
        }

        Set<? extends Element> permissionCancelElements = roundEnvironment.getElementsAnnotatedWith(PermissionCancel.class);
        Map<String, List<ExecutableElement>> permissionCancelMap = new HashMap<>();

        for (Element element : permissionCancelElements) {
            ExecutableElement executableElement = (ExecutableElement) element;
            String activityName = getActivityName(executableElement);
            List<ExecutableElement> executableElements = permissionCancelMap.get(activityName);
            if (executableElements==null){
                executableElements = new ArrayList<>();
                permissionCancelMap.put(activityName,executableElements);
            }
            executableElements.add(executableElement);
            //  messager.printMessage(Diagnostic.Kind.NOTE,"被注解的类有："+executableElement.getSimpleName().toString());
        }


        for(String activityName:needPermissionMap.keySet()){
            List<ExecutableElement> needPermissions= needPermissionMap.get(activityName);
            List<ExecutableElement> permissionDenieds= permissionDeniedMap.get(activityName);
            List<ExecutableElement> permissionCancels= permissionCancelMap.get(activityName);

            try {
               // conditionalWrite(activityName,needPermissions,permissionCancels,permissionDenieds);
                javaPoetWrite(activityName,needPermissions,permissionCancels,permissionDenieds);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        return false;
    }

    private void javaPoetWrite(String activityName,List<ExecutableElement> needPermissions,List<ExecutableElement> cancelPermissions,List<ExecutableElement> deniedPermissions) throws IOException {
        ParameterSpec object = ParameterSpec.builder(Object.class, "object", Modifier.FINAL).build();
        ParameterSpec string = ParameterSpec.builder(String[].class,"permissions").build();
        ParameterSpec intParameter = ParameterSpec.builder(int.class,"requestCode",Modifier.FINAL).build();
        List<ParameterSpec> parameterSpecs = new ArrayList<>();
        parameterSpecs.add(object);
        parameterSpecs.add(string);
        parameterSpecs.add(intParameter);
        TypeElement contextElement = elementUtils.getTypeElement("android.content.Context");
        TypeElement interfaceElement =  elementUtils.getTypeElement("com.zky.easypermission.core.IPermissionRequest");
        TypeElement fragmentElement=elementUtils.getTypeElement("androidx.fragment.app.Fragment");
        TypeElement permissionActivityEl = elementUtils.getTypeElement("com.zky.easypermission.core.PermissionActivity");
        TypeElement permissionEl = elementUtils.getTypeElement("com.zky.easypermission.core.IPermisson");
        String simpleName = needPermissions.get(0).getEnclosingElement().getSimpleName().toString();

        StringBuilder successBuilder = new StringBuilder();
        for (ExecutableElement needPermission : needPermissions) {
            PermissionGranted annotation = needPermission.getAnnotation(PermissionGranted.class);
            int requestCode = annotation.requestCode();
            String methodName = needPermission.getSimpleName().toString();
            successBuilder.append("if (requestCode=="+requestCode+") {\n")
                    .append(" (("+simpleName+") object)."+methodName+"();\n")
                    .append("}\n");
        }

        StringBuilder cancelBuilder = new StringBuilder();
        if (cancelPermissions!=null) {
            for (ExecutableElement needPermission : cancelPermissions) {
                PermissionCancel annotation = needPermission.getAnnotation(PermissionCancel.class);
                int requestCode = annotation.requestCode();
                String methodName = needPermission.getSimpleName().toString();
                cancelBuilder.append("if (requestCode==" + requestCode + ") {\n")
                        .append(" ((" + simpleName + ") object)." + methodName + "();\n")
                        .append("}\n");
            }
        }
        StringBuilder deniedBuilder = new StringBuilder();
        if (deniedPermissions!=null) {
            for (ExecutableElement needPermission : deniedPermissions) {
                PermissionDenied annotation = needPermission.getAnnotation(PermissionDenied.class);
                int requestCode = annotation.requestCode();
                String methodName = needPermission.getSimpleName().toString();
                deniedBuilder.append("if (requestCode==" + requestCode + ") {\n")
                        .append(" ((" + simpleName + ") object)." + methodName + "();\n")
                        .append("}\n");
            }
        }


        MethodSpec methodSpec = MethodSpec.methodBuilder("requestPermission")
                .addAnnotation(Override.class).addModifiers(Modifier.PUBLIC)
                .returns(TypeName.VOID)
               .addParameters(parameterSpecs)
                .addStatement("$T context=null",contextElement)
                .beginControlFlow("if($N instanceof $T)","object",contextElement)
                .addStatement("$N =($T)$N","context",contextElement,"object")
                .endControlFlow()
                .beginControlFlow("else if($N instanceof $T)","object",fragmentElement)
                .addStatement("$N = (($T) $N).getActivity()","context",fragmentElement,"object")
                .endControlFlow()
                .beginControlFlow("if($N==null)","context")
                .addStatement("throw new $T($S)",IllegalArgumentException.class,"只能在Activity或Fragment中申请权限")
             .endControlFlow()
                //.addCode("PermissionActivity")
                .beginControlFlow("$T.Companion.start($N,$N,$N, new $T() ",permissionActivityEl,"context","permissions","requestCode",permissionEl)
                //.addStatement("$T.Companion.start(context,permissions,requestCode, new $T()\n{ ",,permissionEl)
               // .addStatement("PermissionActivity.Companion.start(context,permissions,requestCode, new IPermission(){ ")

                .beginControlFlow("public void granted() ")
                .addCode(successBuilder.toString())
                .endControlFlow()
                .beginControlFlow("public void cancel() ")
                .addCode(cancelBuilder.toString())
                .endControlFlow()
                .beginControlFlow("public void denied()")
                .addCode(deniedBuilder.toString())
                .endControlFlow()
                .addCode("}")
                .addCode(");\n")
                .build();
        TypeSpec build = TypeSpec.classBuilder(needPermissions.get(0).getEnclosingElement().getSimpleName().toString() + CLASS_SUFIX)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(ClassName.get(interfaceElement))
                .addMethod(methodSpec).build();
        TypeElement activityElement = elementUtils.getTypeElement(activityName);
        ClassName className = ClassName.get(activityElement);
        JavaFile.builder(className.packageName(),build).build().writeTo(filer);
    }

    private void conditionalWrite(String activityName,List<ExecutableElement> executableElements,List<ExecutableElement> permissionCancels,List<ExecutableElement> permissionDenieds) throws Exception{
        JavaFileObject sourceFile = filer.createSourceFile(activityName + CLASS_SUFIX);
        String packageName = getPackageName(executableElements.get(0));
        String activitySimpleName = executableElements.get(0).getEnclosingElement().getSimpleName().toString();
        messager.printMessage(Diagnostic.Kind.NOTE,"生成的类有："+activitySimpleName);

        Writer writer = sourceFile.openWriter();

        writer.write("package "+packageName+";\n");

        writer.write("import com.zky.annotation.annotation.PermissionCancel;\n");
        writer.write("import com.zky.annotation.annotation.PermissionDenied;\n");
        writer.write("import com.zky.annotation.annotation.PermissionGranted;\n");
        writer.write("import com.zky.easypermission.core.IPermissionRequest;\n");
        writer.write("import com.zky.easypermission.core.IPermisson;\n");
        writer.write("import com.zky.easypermission.core.PermissionActivity;\n");
        writer.write("import com.zky.easypermission.core.util.PermissionUtils;\n");
        writer.write("import android.content.Context;\n");
        writer.write("import androidx.fragment.app.Fragment;\n");

        writer.write("public class "+activitySimpleName+CLASS_SUFIX+" implements IPermissionRequest"+"{\n");//+activitySimpleName+">

        writer.write("public void requestPermission(final "+"Object"+" object,String[] permissions,final int requestCode) {\n");
        writer.write("Context context=null;\n");
        writer.write("if (object instanceof Context){\n");
        writer.write("context = (Context) object;\n");
        writer.write(" }else if (object instanceof Fragment){\n");
        writer.write("   context = ((Fragment)object).getActivity();\n");
        writer.write(" }\n");
        writer.write("  if (context==null){\n");
        writer.write("  throw new IllegalArgumentException(\"只能在Activity或Fragment中申请权限\");\n");
        writer.write("   }\n");

        writer.write(" PermissionActivity.Companion.start(context,permissions,requestCode, new IPermisson() {\n");
        writer.write("public void granted() {\n");
        for (ExecutableElement executableElement : executableElements) {
           String methodName= executableElement.getSimpleName().toString();
            PermissionGranted annotation = executableElement.getAnnotation(PermissionGranted.class);
            int requestCode = annotation.requestCode();
            writer.write("if (requestCode=="+requestCode+") {\n");
            writer.write(" (("+activitySimpleName+") object)."+methodName+"();\n");
            writer.write(" }\n");


        }
        writer.write(" }\n");
        writer.write("public void cancel() {\n");
        if(permissionCancels!=null) {
            for (ExecutableElement executableElement : permissionCancels) {
                String methodName = executableElement.getSimpleName().toString();
                PermissionCancel annotation = executableElement.getAnnotation(PermissionCancel.class);
                int requestCode = annotation.requestCode();
                writer.write("if (requestCode==" + requestCode + ") {\n");
                writer.write(" ((" + activitySimpleName + ") object)." + methodName + "();\n");
                writer.write(" }\n");


            }
        }
        writer.write("}\n");
        writer.write(" public void denied() {\n");
        if(permissionDenieds!=null) {
            for (ExecutableElement executableElement : permissionDenieds) {
                String methodName = executableElement.getSimpleName().toString();
                PermissionDenied annotation = executableElement.getAnnotation(PermissionDenied.class);
                int requestCode = annotation.requestCode();
                writer.write("if (requestCode==" + requestCode + ") {\n");
                writer.write(" ((" + activitySimpleName + ") object)." + methodName + "();\n");
                writer.write(" }\n");


            }
        }
        writer.write("}\n");
        writer.write(" });\n");

        writer.write("}\n");

        writer.write("}");

        writer.close();

    }

    private String getActivityName(ExecutableElement executableElement){
        String packageName = getPackageName(executableElement);
        TypeElement typeElement = (TypeElement) executableElement.getEnclosingElement();
        return packageName+"."+typeElement.getSimpleName().toString();
    }

    private String getPackageName(ExecutableElement executableElement){
        TypeElement typeElement = (TypeElement) executableElement.getEnclosingElement();
        String packageName = elementUtils.getPackageOf(typeElement).getQualifiedName().toString();
        return packageName;
    }
}
