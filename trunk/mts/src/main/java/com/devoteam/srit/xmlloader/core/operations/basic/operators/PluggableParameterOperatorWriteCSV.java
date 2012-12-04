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

package com.devoteam.srit.xmlloader.core.operations.basic.operators;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.pluggable.PluggableName;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.URIRegistry;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URI;
import java.util.Map;

/**
 *
 * @author rbarbot
 */
public class PluggableParameterOperatorWriteCSV  extends AbstractPluggableParameterOperator
{
    private final String S_WRITECSVRAW = "file.writeCsvRaw";
    private final String S_WRITECSVCOL = "file.writeCsvCol";

    public PluggableParameterOperatorWriteCSV()
    {
        this.addPluggableName(new PluggableName(S_WRITECSVRAW));
        this.addPluggableName(new PluggableName(S_WRITECSVCOL));
    }

    @Override
    public Parameter operate(Runner runner, Map<String, Parameter> operands, String name, String resultant) throws Exception 
    {
        normalizeParameters(operands);
        Parameter filePath = PluggableParameterOperatorList.assertAndGetParameter(operands, "value");
        Parameter paramData = PluggableParameterOperatorList.assertAndGetParameter(operands, "value2");

        String separator = Config.getConfigByName("tester.properties").getString("operations.CSV_SEPARATOR_CHAR", ";");
        URI csvPath = URIRegistry.MTS_TEST_HOME.resolve(filePath.get(0).toString().trim());

        if (name.equalsIgnoreCase(S_WRITECSVCOL)) {
            File file = new File(csvPath);
            if (!file.exists()) file.createNewFile();
            String pathTmp = file.getParentFile().getAbsolutePath();
            File fileTmp = new File(pathTmp+"\\writecsvTmp.csv");
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            if (line != null) {
                int i=0;
                fileTmp.createNewFile();
                FileWriter writer  = new FileWriter(fileTmp);
                String lineModel = "";
                int fromIndex = line.indexOf(separator);
		while (fromIndex>=0) {
			lineModel += separator;
			fromIndex = line.indexOf(separator, fromIndex+1);
		}
		lineModel += separator;
                while (line != null) {
                    line += separator + paramData.get(i).toString().trim();
                    writer.write(line+System.getProperty("line.separator"));
                    i++;
                    line = reader.readLine();
                }
                reader.close();
                if (i<paramData.length()) {
                    for (int max=paramData.length(); i<max; i++) {
                        writer.write(lineModel+paramData.get(i).toString().trim()+System.getProperty("line.separator"));
                    }
                }
                writer.close();
                file.delete();
                fileTmp.renameTo(file);
            }
            else {
                reader.close();
                FileWriter writer  = new FileWriter(new File(csvPath));
                for (int i=0,max=paramData.length(); i<max; i++) {
                    writer.write(paramData.get(i).toString().trim()+System.getProperty("line.separator"));
                }
                writer.close();
            }

        }
        else if (name.equalsIgnoreCase(S_WRITECSVRAW)) {
            String data = "";
            for (int i=0,max=paramData.length(); i<max; i++)
                data += paramData.get(i).toString() + separator;
            data = data.substring(0, data.length()-separator.length());

            boolean isEmpty = false;
            File file = new File(csvPath);
            if (!file.exists()) file.createNewFile();
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            if (line == null)
                isEmpty = true;
            reader.close();

            FileWriter writer  = new FileWriter(file, true);
            if (!isEmpty)
                writer.write(System.getProperty("line.separator")+data);
            else
                writer.write(data);
            writer.close();
        }
        else {
        	throw new RuntimeException("unsupported operation " + name);
        }

        return null;
    }

}
