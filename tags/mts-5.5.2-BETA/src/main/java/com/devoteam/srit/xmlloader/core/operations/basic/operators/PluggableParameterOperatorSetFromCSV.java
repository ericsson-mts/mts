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

import au.com.bytecode.opencsv.CSVReader;
import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.exception.ParameterException;
import com.devoteam.srit.xmlloader.core.pluggable.PluggableName;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.URIFactory;
import com.devoteam.srit.xmlloader.core.utils.URIRegistry;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.filesystem.SingletonFSInterface;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 *
 * @author gpasquiers
 */
public class PluggableParameterOperatorSetFromCSV extends AbstractPluggableParameterOperator
{

    public PluggableParameterOperatorSetFromCSV()
    {

        this.addPluggableName(new PluggableName("setFromCSV", "file.readcsv"));
    }

    @Override
    public Parameter operate(Runner runner, Map<String, Parameter> operands, String name, String resultant) throws ParameterException
    {
        this.normalizeParameters(operands);
        Parameter csvPath = PluggableParameterOperatorList.assertAndGetParameter(operands, "value");
        Parameter csvCol = PluggableParameterOperatorList.assertAndGetParameter(operands, "value2");
        Parameter result = new Parameter();
        try
        {
            for (int i = 0; i < csvPath.length(); i++)
            {
                String var2 = csvCol.get(i).toString();
                int column = -1;

                if (Utils.isInteger(csvCol.get(i).toString()))
                {
                    column = Integer.parseInt(var2);
                }


                String separator = Config.getConfigByName("tester.properties").getString("operations.CSV_SEPARATOR", ";");

                URI uri = URIFactory.resolve(URIRegistry.IMSLOADER_RESOURCES_HOME, csvPath.get(i).toString());

                if(!SingletonFSInterface.instance().exists(uri)) throw new ParameterException("CSV file " + uri + " does not exist");
                        
                CSVReader csvReader = new CSVReader(new InputStreamReader(SingletonFSInterface.instance().getInputStream(uri)), separator.charAt(0));

                List<String[]> list = csvReader.readAll();

                for (String[] line : list)
                {
                    if (column == -1)
                    {
                        for (int j = 0; j < line.length; j++)
                        {
                            if (line[j].trim().equals(var2.trim()))
                            {
                                column = j;
                                break;
                            }
                        }

                        if (column == -1)
                        {
                            throw new ParameterException("invalid column index " + var2 + " (" + column + ")");
                        }

                        continue;
                    }

                    result.add(line[column].trim());
                }

                csvReader.close();
            }
        }
        catch(ParameterException e)
        {
            throw e;
        }
        catch(Exception e)
        {
            throw new ParameterException("Error in setFromCSV operator", e);
        }

        return result;
    }
}
