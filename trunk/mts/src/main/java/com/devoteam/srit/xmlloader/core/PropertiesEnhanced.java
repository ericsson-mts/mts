/* 
 * Copyright 2012 Devoteam http://www.devoteam.com
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * 
 * 
 * This file is part of Multi-Protocol Test Suite (MTS).
 * 
 * Multi-Protocol Test Suite (MTS) is free software: you can redistribute
 * it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of the
 * License.
 * 
 * Multi-Protocol Test Suite (MTS) is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Multi-Protocol Test Suite (MTS).
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package com.devoteam.srit.xmlloader.core;

import com.devoteam.srit.xmlloader.core.exception.ParsingInputStreamException;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.filesystem.SingletonFSInterface;
import java.util.LinkedHashMap;
import java.util.Vector;
import java.io.File;
import java.util.Iterator;
import java.net.URI;
import java.io.InputStream;
import java.io.OutputStream;

import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;

/**
 *
 * @author jbor
 */
public class PropertiesEnhanced{
    
    //attribut
    private LinkedHashMap<String, PropertiesParametersStructure> configFile;

    //constructeur
    public PropertiesEnhanced (){
        configFile = new LinkedHashMap<String, PropertiesParametersStructure>();
    }

    //accesseurs
    public PropertiesParametersStructure getPropertiesParametersStructure(String name){
        return configFile.get(name);
    }
    public String getDefaultValue (String name){
        if (this.isPresent(name)){
            return configFile.get(name).getDefaultValue();
        }
        else{
            return null;
        }
    }
    public String getlocaleValue (String name){
        if (this.isPresent(name)){
            return configFile.get(name).getLocaleValue();
        }
        else{
            return null;
        }
    }
    public String getType (String name){
        if (this.isPresent(name)){
            return configFile.get(name).getType();
        }
        else{
            return null;
        }
    }
    public LinkedHashMap<String, PropertiesParametersStructure> getConfig(){
        return configFile;
    }
    public int getNumberOfParameters(){
        return configFile.size();
    }
    public Vector<String> getNameOfAllParameters(){
        Vector<String> listOfName = new Vector<String>();
        String currentName = "";
        Iterator<String> iterator = configFile.keySet().iterator();
        while (iterator.hasNext()){
            currentName = iterator.next();
            listOfName.add(currentName);
        }
        return listOfName;
    }

    //methodes
    public void addPropertiesEnhancedComplete (String name, String defaultValue){
        configFile.put(name, new PropertiesParametersStructure(name, defaultValue));
    }
    public void addPropertiesEnhancedComplete (String name, String type, String defaultValue, String localeValue){
        configFile.put(name, new PropertiesParametersStructure(name, type, defaultValue, localeValue));
    }
    public void addPropertiesEnhancedComplete (String name, String description, String type, String defaultValue, String localeValue, boolean restart, Vector<String> possibilities){
        configFile.put(name, new PropertiesParametersStructure(name, description, type, defaultValue, localeValue, restart, possibilities));
    }
    public boolean isPresent (String name){
        return configFile.containsKey(name);
    }
    public void setLocaleValue (String name, String value){
        getPropertiesParametersStructure(name).setLocaleValue(value);
    }
    public void setDefaultValue (String name, String value){
        if(!isPresent(name))
            addPropertiesEnhancedComplete(name, value);
        else
            getPropertiesParametersStructure(name).setDefaultValue(value);
    }
    public void setConfig (LinkedHashMap<String, PropertiesParametersStructure> config){
        this.configFile = config;
    }
    public Vector<String> isLocalPresent(){
        Vector<String> nameOfLocal = null;
        Vector<String> listOfName = this.getNameOfAllParameters();
        for (int i = 0; i < this.getNumberOfParameters()-1; i++){
            if (this.getlocaleValue(listOfName.elementAt(i)) != null ){
                nameOfLocal.add(listOfName.elementAt(i));
            }
        }
        return nameOfLocal;
    }

    /**
     * Methode qui permet de parser un fichier existant sur le disque
     * Le resultat sera dans une nouvelle hashmap, rangee correctement
     * @return: la structure contenant toutes les donnees concernant un test
     */
    public void parse(InputStream inputStream, boolean newHashMap, boolean isLocale){
        try{
            String line;
            Vector<String> temp = new Vector();
            boolean restart                  = false;
            Vector<String> possibilities     = null;
            StringBuilder name               = new StringBuilder();
            StringBuilder description        = new StringBuilder();
            StringBuilder type               = new StringBuilder();
            StringBuilder value              = new StringBuilder();
            String lastLine;
            //boolean qui permet de savoir de on a fini de lire dans le stream, car en fin de stream, une exception est envoyee
            boolean exception               = false;
            // Lecture du fichier ligne par ligne
            while (!exception) {
                try{
                    line = Utils.readLineFromInputStream(inputStream);
                    if (line != null){
                        //suppression des '\n' et '\r' si presents
                        if (line.length()>0 && line.charAt(line.length()-1) == '\n'){
                            line = line.substring(0, line.length()-1);
                        }
                        if (line.length()>0 && line.charAt(line.length()-1) == '\r'){
                            line = line.substring(0, line.length()-1);
                        }
                        //new afin d'eviter un probleme de reference
                        possibilities = new Vector<String>();
                        name.delete(0, name.length());
                        description.delete(0, description.length());
                        type.delete(0, type.length());
                        value.delete(0, value.length());
                        if (possibilities.size() > 0){
                            possibilities.removeAllElements();
                        }
                        restart = false;

                        if (line.length() == 0){
                            //la ligne est vide donc ce qui precedait n'etait pas un commentaire, on le supprime
                            temp.clear();
                        }
                        else if (line.charAt(0) != '#' && temp.isEmpty()
                                || line.charAt(0) != '#' && temp.elementAt(0).equals("")){
                            /**
                             * on a un parametre sans description ni type, on le prend quand meme en compte avec une fonction speciale car
                             */
                            addParameter(line, newHashMap, isLocale);
                        }
                        else if (line.charAt(0) == '#'){
                            if (line.length() >= 2){
                                int indexDepart = line.lastIndexOf('#')+1;
                                if (indexDepart+1 < line.length()){
                                    temp.add(line.substring(indexDepart+1, line.length()));
                                }
                            }
                        }
                        else {
                            /**
                             * ici, on est sur que la ligne correspond au nom et valeur
                             * et que temp contient la description et eventuellement le type, possibilites, restart
                             */

                            //on recupere la description
                            int indexFinDescription = 0;
                            if (temp.lastElement().charAt(0) == '[' || temp.lastElement().contains("(restart)")){
                                //la derniere ligne correspond au type ou a la balise restart
                                indexFinDescription = temp.size()-2;
                            }
                            else {
                                //la derniere ligne est un commentaire, il n'y a pas de type
                                indexFinDescription = temp.size()-1;
                            } 
                            for (int i = 0; i <= indexFinDescription; i++){
                                    if (temp.elementAt(i).length() > 0){
                                        description.append(temp.elementAt(i)).append("\n");
                                    }
                            }

                            lastLine = temp.lastElement();

                            //on cherche ou est le type, si il n'est pas present, on met string par defaut
                            if (lastLine.charAt(0) == '['){
                                    type.append(lastLine.substring(1, lastLine.indexOf(']')));

                                    //on recupere les possibilite si il y en a
                                    int indexFinType = lastLine.indexOf(']');
                                    if (lastLine.contains("|")){
                                        //il y a des possibilites, on les recupere
                                        StringBuilder str = new StringBuilder();
                                        int i = indexFinType+2;
                                        while ( i < lastLine.length() && lastLine.charAt(i) != ' ' && lastLine.charAt(i) != '\n'){
                                            if (lastLine.charAt(i) != '|'){
                                                    str.append(lastLine.charAt(i));
                                            }
                                            else {
                                                    possibilities.add(str.toString());
                                                    str.delete(0, str.length());
                                            }
                                            i++;
                                        }
                                        possibilities.add(str.toString());
                                        str.delete(0, str.length());
                                    }
                                }
                            else {
                                type.append("string");
                            }

                            //on recupere le restart si il est present
                            if (lastLine.contains("(restart)")){
                                restart = true;
                            }

                            /**
                             * ici, on est sur que la ligne correspond au nom et a la valeur du parametre
                             */

                            //on recupere le nom
                            name.append(line.substring(0, line.indexOf('=')));
                            //on recupere la valeur (si elle existe)
                            value.append(line.substring(line.indexOf('=')+1, line.length()));
                            String resultedValue = value.toString().trim();
                            if(resultedValue.contains("\\"))//part to be compliant with real properties file for value
                            {
                                resultedValue = Utils.replaceNoRegex(resultedValue, "\\\\", "\\");
                                resultedValue = Utils.replaceNoRegex(resultedValue, "\\ ", " ");
                                resultedValue = Utils.replaceNoRegex(resultedValue, "\\:", ":");
                                resultedValue = Utils.replaceNoRegex(resultedValue, "\\=", "=");
                            }
                            //on ajoute notre nouveau parametre si on parse la conf, sinon, on ajoute la valeur locale
                            if(newHashMap){
                                if (!isLocale){
                                    this.addPropertiesEnhancedComplete(name.toString().trim(), description.toString().trim(), type.toString().trim(), resultedValue, null, restart, possibilities);
                                }
                                else{
                                    this.addPropertiesEnhancedComplete(name.toString().trim(), description.toString().trim(), type.toString().trim(), null, resultedValue, restart, possibilities);
                                }

                                
                            }
                            else{
                                if (this.getConfig().containsKey(name.toString().trim())){
                                    this.setLocaleValue(name.toString().trim(), resultedValue);
                                }
                                else{
                                    // on a un parametre non present en global, on l'ajoute avec uniquement une valeur locale
                                    this.addPropertiesEnhancedComplete(name.toString().trim(), description.toString().trim(), type.toString().trim(), null, resultedValue, restart, possibilities);
                                }
                            }
                        }
                    }
                }
                catch (ParsingInputStreamException e){
                    //on est a la fin du document, on verifie si il y a une propertie dans la derniere ligne et on l'ajoute si oui.
                    //cette dernière ligne n'est pas pris en compte si ce n'est pas une propertie ie, si elle commence par '#'
                    exception = true;
                    String bufferLastLine = e.getBuffer();
                    if ((bufferLastLine.length() > 0) && bufferLastLine.charAt(0) != '#') {
                        addParameter(bufferLastLine, newHashMap, isLocale);
                    }
                }
            }
            inputStream.close();
        }
        catch (Exception e) {
            GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.CORE, e, "Error while parsing config");
        }
    }

    public void addParameter(String line, boolean newHashMap, boolean isLocale){
        StringBuilder name = new StringBuilder();
        StringBuilder value = new StringBuilder();

        name.append(line.substring(0, line.indexOf('=')));
        value.append(line.substring(line.indexOf('=')+1, line.length()));
        String nameStr = name.toString().trim();
        String valueStr = value.toString().trim();

        if (newHashMap){
            //si on parse la conf, on ajoute notre valeur
            if (!isLocale){
                this.addPropertiesEnhancedComplete(nameStr, "string", valueStr, null);
            }
            else{
                this.addPropertiesEnhancedComplete(nameStr, "string", null, valueStr);
            }
        }
        else{
            //si on parse le local, on ajoute la valeur local si le parametre existe deja, sinon, on en tient pas compte
            if (this.getConfig().containsKey(nameStr)){
                //si quand on parse la conf locale, un parametre n'a pas de valeur, il faut le prendre en compte
                this.setLocaleValue(nameStr, valueStr);
            }
            else{
                // on a un parametre non present en global, on l'ajoute avec uniquement une valeur locale
                this.addPropertiesEnhancedComplete(nameStr, "", "string", null, valueStr, false, null);
            }
        }
    }

    /**
     * Methode qui permet de parser un fichier existant sur le disque
     * en prenant en compte la conf gloabale et locale
     * Le resultat sera dans une nouvelle hashmap, rangee correctement
     * @return: la structure contenant toutes les donnees concernant un test
     */    
    public PropertiesEnhanced parse(InputStream inputStreamConf, InputStream inputStreamLoc, boolean isLocale){
        this.parse(inputStreamConf, true, isLocale);
        this.parse(inputStreamLoc, false, isLocale);
        return this;
    }    

    /**
     * Methode qui permet de sauvegarder une string sur le disque
     */    
    public void saveFile (URI filePath) {
        String data = makeData(this.configFile);
        File myFile= new File(filePath);
        if (myFile.exists()){
            myFile.delete();
        }
        if (data.length() > 0){
            try{
                OutputStream out = SingletonFSInterface.instance().getOutputStream(filePath);
                out.write(data.getBytes());
                out.close();
            }
            catch(Exception e){
                GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.CORE, e, "Cannot save file", filePath);
            }
        }
    }

    /**
     * Methode qui permet de recuperer tous les blocs de donnee des parametre dont la valeur a change
     * (soit via l'interface graphique soit en local)
     * @return: les donnees correctement formatee dont les valeurs ont change
     */
    public String makeData (LinkedHashMap<String, PropertiesParametersStructure> configFile){
        StringBuilder data = new StringBuilder();
        String currentName = "";
        Iterator<String> iterator = configFile.keySet().iterator();        
        while (iterator.hasNext()){
            currentName = iterator.next();
            data.append(getADataBlock(configFile, currentName));
        }
        return data.toString();
    }

    /**
     * Methode qui permet de "concatener" chaque bloc d'information d'un parametre (description,
     * type, possibilites, nom, valeur)
     * On fait attention a recuperer la bonne valeur (priorite a celle provenant de l'interface graphique
     * puis a la locale si une des 2 a change)
     * @return: le bloc qui comprends toutes les informations d'un parametre
     */
    public String getADataBlock (LinkedHashMap<String, PropertiesParametersStructure> configFile, String name){
        StringBuilder dataBlock = new StringBuilder();
        String description;
        String type = null;
        String value = null;
        String possibilities;
        String parameterName;

        if (configFile.containsKey(name)){
            parameterName = name;
            if (!configFile.get(name).getDescription().equals("")){
                description = "# " + configFile.get(name).getDescription().replaceAll("\n", "\n# ") + "\n";
            }
            else {
                description = null;
            }
            if (configFile.get(name).getType() != null && !configFile.get(name).getType().equals("")){
               type = "# [" + configFile.get(name).getType() + "]" + " ";
            }
            possibilities = formatPossibilities(configFile, name);
            try {
                if (configFile.get(name).getLocaleValue() != null){
                    value = configFile.get(name).getLocaleValue();
                }
            
            }
            catch (Exception e){
                GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.CORE, e, "Error while saving");
            }
            if (value != null){
                if(value.contains(":") || value.contains("="))//so need to escape these character with \
                {
                    value = Utils.replaceNoRegex(value, ":", "\\:");
                    value = Utils.replaceNoRegex(value, "=", "\\=");
                }
                if (description != null){
                    dataBlock.append(description);
                }
                if (type != null){
                    dataBlock.append(type);
                }
                if (possibilities != null){
                    dataBlock.append(possibilities);
                }
                if (configFile.get(name).getRestart()){
                    dataBlock.append("(restart)").append("\n");
                }
                else{
                    dataBlock.append("\n");
                }
                dataBlock.append(parameterName).append(" = ").append(value).append("\n\n");
            }            
        }
        return dataBlock.toString();
    }

    /**
     * Methode permettant de formatter les differentes possibilites d'un parametre (utile
     * dans le cas de l'enumeration) a partir d'une "longue" string
     * @return: le bloc contenant les differentes possibilites
     */
    public String formatPossibilities (LinkedHashMap<String, PropertiesParametersStructure> configFile, String name){
        String possibilitiesBlock = null;
        if (configFile.get(name).getPossibilities() != null){
            int nbPossibilities = configFile.get(name).getPossibilities().size();
            StringBuilder possibilities = new StringBuilder();
            for (int i = 0; i <= nbPossibilities-1; i++){
                possibilities.append(configFile.get(name).getPossibilities().get(i)).append("|");
            }
            if (possibilities.length() > 0){
                possibilities.deleteCharAt(possibilities.lastIndexOf("|"));
                possibilities.append(" ");
            }
            possibilitiesBlock = possibilities.toString();
        }
        return possibilitiesBlock;
    }
}