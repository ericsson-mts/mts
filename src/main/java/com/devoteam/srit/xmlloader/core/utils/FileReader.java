package com.devoteam.srit.xmlloader.core.utils;

import java.io.File;

public class FileReader {

    public static String checkFileExist(String path){

        // On test l'existance du fichier dans le classpath
        if(new File("../conf/"+path).exists()){
            path = "../conf/" + path;
        }
        //On test l'existance du fichier dans le jar (repertoire resource)
        else if(!FileReader.class.getClassLoader().getResource(path).getFile().isEmpty()){
            path = FileReader.class.getClassLoader().getResource(path).getPath();
        }
        return path;
    }
}
