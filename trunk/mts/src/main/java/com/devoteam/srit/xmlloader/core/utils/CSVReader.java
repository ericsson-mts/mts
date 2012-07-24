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

package com.devoteam.srit.xmlloader.core.utils;

import com.devoteam.srit.xmlloader.core.exception.ParameterException;
import com.devoteam.srit.xmlloader.core.utils.filesystem.SingletonFSInterface;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.rmi.RemoteException;
import java.util.LinkedList;

/**
 * Class for parsing CSV files.
 * It manages quotes and commentaries.
 *
 * @author rbarbot
 */
public class CSVReader {

    private String separator;
    private String comment;
    private String quote;
    private LinkedList<String[]> csvData;
    private String fileName;

    /**
     *
     * @param fileName
     * @param comment - comment character - can be null
     * @param quote - quote character - can be null
     * @throws RemoteException
     * @throws ParameterException
     * @throws IOException
     */
    public CSVReader(String fileName, String comment, String quote) throws RemoteException, ParameterException, IOException {
        this.separator = Config.getConfigByName("tester.properties").getString("operations.CSV_SEPARATOR", ";");
        this.comment = comment;
        this.quote = quote;
        this.fileName = fileName;
        parseCSVFile(fileName);
    }

    private void parseCSVFile(String fileName) throws RemoteException, ParameterException, IOException {
        URI uri = URIFactory.resolve(URIRegistry.IMSLOADER_RESOURCES_HOME, fileName);

        if(!SingletonFSInterface.instance().exists(uri)
           || !SingletonFSInterface.instance().isFile(uri))
                throw new ParameterException("CSV file " + uri + " does not exist");

        csvData = new LinkedList<String[]>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(SingletonFSInterface.instance().getInputStream(uri)));
        String line = "";
        int colCount = 0;
        while (line != null) {
        	line = reader.readLine();
        	// line is null => end of file
        	if (line == null) {
        		break;
        	}
        	// blank line
        	line = line.trim();
            if ("".equals(line)) {
                continue;
            }
            // comment line
            if (line.startsWith(comment)) {
                continue;
            }
            int nb = countColumn(line);
            if (nb != colCount && colCount > 0) {
            	reader.close();
                throw new ParameterException("CSV file " + uri + " : bad format (number of column not equal)");
            }
            else
                colCount = nb;
            String[] data = new String[nb+1];
            int index = 0;
            for (int i=0; i<nb+1; i++) {
                if (i == nb) {
                    data[i] = line;
                }
                else {
                    boolean loop = false;
                    index = line.indexOf(separator);
                    if (quote != null) {
                        data[i] = "";
                        while (line.substring(0, index).contains(quote)) {
                            loop = true;

                            index = line.substring(line.indexOf(quote)+1).indexOf(quote);
                            index +=2; // aller juste après la quote
                            if (!line.startsWith(quote))
                                index += line.indexOf(quote);
                            if (line.substring(index).indexOf(separator) < line.substring(index).indexOf(quote)) {
                                data[i] += line.substring(0, index+line.substring(index).indexOf(separator));
                                break;
                            }
                            data[i] += line.substring(0, index);
                            line = line.substring(index);
                            if (line.startsWith(separator))
                                line = line.substring(1);
                            if (line.contains(separator))
                                index = line.length();
                            else
                                break;
                        }
                    }
                    else
                        data[i] = line.substring(0, index);
                    if (!loop) {
                        if (quote != null)
                            data[i] = line.substring(0, index);
                        line = line.substring(index+1);
                    }
                }
            }
            csvData.add(data);
        }
        reader.close();
    }

    private int countColumn(String text) throws ParameterException {
        int count = 0;
        int index = 0;
        while (text.contains(separator)) {
            index = text.indexOf(separator);
            if (quote != null) {
                boolean loop = false;
                while (text.substring(0, index).contains(quote)) {
                    loop = true;
                    index = text.substring(text.indexOf(quote)+1).indexOf(quote);
                    if (index == -1)
                        throw new ParameterException("CSV file " + fileName + " : bad format (a single quote - with no ending - has been detected)");
                    index +=2; // aller juste après la quote
                    if (!text.startsWith(quote))
                        index += text.indexOf(quote);
                    if (text.substring(index).indexOf(separator) < text.substring(index).indexOf(quote))
                        break;
                    text = text.substring(index);
                    if (!text.contains(separator))
                        text = "";
                    if (text.startsWith(separator))
                        text = text.substring(1);
                    else if (text.contains(separator))
                        text = text.substring(text.indexOf(separator)+1);
                    index = text.length();
                }
                if (!text.substring(0, index).contains(quote) && !loop)
                    text = text.substring(index+ text.substring(index).indexOf(separator)+1);
                else if(text.substring(0, index).contains(quote) && loop)
                    text = text.substring(index+ text.substring(index).indexOf(separator)+1);
            }
            else
                text = text.substring(index+1);
            count ++;
        }
        return count;
    }

    public LinkedList<String[]> getData() {
        return csvData;
    }
}
