package org.ycz.servlet;

import org.ycz.annotation.Autowired;
import org.ycz.annotation.Controller;
import org.ycz.annotation.RequestMapping;
import org.ycz.annotation.Service;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DispatchServlet extends javax.servlet.http.HttpServlet {

    List<String> classes = new ArrayList<String>();

    Map<String,Object> beans = new HashMap<>();

    Map<String,Method> handlerMapping = new HashMap<>();



    @Override
    public void init() throws ServletException {
        super.init();

        //扫描包
        scanPackage("org.ycz");
        //实例化
        try {
            instanceBeans();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

        //DI依赖注入
        try {
            injection();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
         //handlemapping
        instHandlerMapping();

    }

    private void instHandlerMapping() {
        for (Map.Entry<String,Object> entry: beans.entrySet()) {
            Object instance = entry.getValue();
            if(instance.getClass().isAnnotationPresent(RequestMapping.class)){
                RequestMapping r1 = (RequestMapping) instance.getClass().getAnnotation(RequestMapping.class);
                String u1 = r1.value();
                for (Method method :instance.getClass().getMethods()){
                    if(method.isAnnotationPresent(RequestMapping.class)){
                      RequestMapping r2 = method.getAnnotation(RequestMapping.class);
                      String u2 = r2.value();
                        handlerMapping.put(u1+u2,method);
                    }
                }
            }
        }
    }

    private void injection() throws IllegalAccessException {
        for (Map.Entry<String,Object> entry: beans.entrySet()) {
            Object instance = entry.getValue();
            if(instance.getClass().isAnnotationPresent(Controller.class)){
                for (Field field :instance.getClass().getDeclaredFields()){
                    if(field.isAnnotationPresent(Autowired.class)){
                       Object service =  beans.get(field.getName());
                       field.setAccessible(true);
                       field.set(instance,service);
                    }
                }
            }
        }
    }

    private void instanceBeans() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        for(String clazz:classes){
            Class<?> instance = Class.forName(clazz.replace(".class",""));
            if(instance.isAnnotationPresent(Controller.class)){
                Object object = instance.newInstance();
                RequestMapping requestMapping = instance.getAnnotation(RequestMapping.class);
                beans.put(requestMapping.value().replace("/",""),object);
            }
            if(instance.isAnnotationPresent(Service.class)){
                Object object = instance.newInstance();
                Service service = instance.getAnnotation(Service.class);
                beans.put(service.value(),object);
            }

        }
    }

    private void scanPackage(String s)  {
        String name = s;
        if(!name.endsWith(".class")) {
            name = name.replace(".", "/");
            String path =  getClass().getClassLoader().getResource(name).getPath();
            File file = new File(path);
            if(file.isDirectory()){
                File[] files = file.listFiles();
                for (File file2: files) {
                    scanPackage(s + "."+file2.getName());
                }
            }else{
                classes.add(s + "."+file.getName());
            }
        }else{
            classes.add(s);
        }

    }

    protected void doPost(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws javax.servlet.ServletException, IOException {
            String uri = request.getRequestURI();
            String context = request.getContextPath();
            uri = uri.replace(context,"");
            Method method = handlerMapping.get(uri);
            Object instance = beans.get(uri.split("/")[1]);
        try {
            method.invoke(instance);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws javax.servlet.ServletException, IOException {
        doPost(request,response);
    }
}
