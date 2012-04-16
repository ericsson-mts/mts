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
import com.devoteam.srit.xmlloader.core.exception.ParameterException;
import com.devoteam.srit.xmlloader.core.pluggable.PluggableName;

import gp.utils.arrays.Array;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Collections;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author gpasquiers
 */
public class PluggableParameterOperatorList extends AbstractPluggableParameterOperator
{
    final private String NAME_ISEMPTY = "isEmpty"; 
    final private String NAME_SIZE = "size"; 
    final private String NAME_ADDFIRST = "addFirst"; 
    final private String NAME_ADDLAST = "addLast";
    final private String NAME_APPEND = "append";
    final private String NAME_GETFIRST = "getFirst"; 
    final private String NAME_GETLAST = "getLast"; 
    final private String NAME_REMOVEFIRST = "removeFirst"; 
    final private String NAME_REMOVELAST = "removeLast"; 
    final private String NAME_REMOVEAT = "removeAt"; 

    final private String NAME_L_ISEMPTY = "list.isEmpty";
    final private String NAME_L_SIZE = "list.size";
    final private String NAME_L_ADDFIRST = "list.addFirst";
    final private String NAME_L_ADDLAST = "list.addLast";
    final private String NAME_L_GETFIRST = "list.getFirst";
    final private String NAME_L_GETLAST = "list.getLast";
    final private String NAME_L_REMOVEFIRST = "list.removeFirst";
    final private String NAME_L_REMOVELAST = "list.removeLast";
    final private String NAME_L_REMOVEAT = "list.removeAt";
    final private String NAME_L_REVERT = "list.revert";
    final private String NAME_L_MIXCOMPLETE = "list.mixComplete";
    final private String NAME_L_MIX = "list.Mix";
    final private String NAME_L_STATMAX = "list.statMax";
    final private String NAME_L_STATMIN = "list.statMin";
    final private String NAME_L_STATAVERAGE = "list.statAverage";
    final private String NAME_L_STATDEVIATION = "list.statDeviation";
    final private String NAME_L_STATVARIANCE = "list.statVariance";
    final private String NAME_L_SORTNUMBER = "list.sortNumber";
    final private String NAME_L_STRINGSORT = "list.sortString";    
    final private String NAME_L_ENQUEUE = "list.enqueue";
    final private String NAME_L_DEQUEUE = "list.dequeue";

    final private String NAME_QUEUE_ENQUEUE = "utils.queue.enqueue";
    final private String NAME_QUEUE_DEQUEUE = "utils.queue.dequeue";

    final private static HashMap<String, LinkedBlockingQueue<Parameter>> _queues = new HashMap();
    
    public PluggableParameterOperatorList()
    {
        this.addPluggableName(new PluggableName(NAME_ISEMPTY,NAME_L_ISEMPTY));
        this.addPluggableName(new PluggableName(NAME_SIZE,NAME_L_SIZE));
        this.addPluggableName(new PluggableName(NAME_ADDFIRST,NAME_L_ADDFIRST));
        this.addPluggableName(new PluggableName(NAME_ADDLAST,NAME_L_ADDLAST));
        this.addPluggableName(new PluggableName(NAME_APPEND,NAME_L_ADDLAST));
        this.addPluggableName(new PluggableName(NAME_GETFIRST,NAME_L_GETFIRST));
        this.addPluggableName(new PluggableName(NAME_GETLAST,NAME_L_GETLAST));
        this.addPluggableName(new PluggableName(NAME_REMOVEFIRST,NAME_L_REMOVEFIRST));
        this.addPluggableName(new PluggableName(NAME_REMOVELAST,NAME_L_REMOVELAST));
        this.addPluggableName(new PluggableName(NAME_REMOVEAT,NAME_L_REMOVEAT));
        this.addPluggableName(new PluggableName(NAME_QUEUE_ENQUEUE, NAME_L_ENQUEUE));
        this.addPluggableName(new PluggableName(NAME_QUEUE_DEQUEUE, NAME_L_DEQUEUE));
        
        this.addPluggableName(new PluggableName(NAME_L_ISEMPTY));
        this.addPluggableName(new PluggableName(NAME_L_SIZE));
        this.addPluggableName(new PluggableName(NAME_L_ADDFIRST));
        this.addPluggableName(new PluggableName(NAME_L_ADDLAST));
        this.addPluggableName(new PluggableName(NAME_L_GETFIRST));
        this.addPluggableName(new PluggableName(NAME_L_GETLAST));
        this.addPluggableName(new PluggableName(NAME_L_REMOVEFIRST));
        this.addPluggableName(new PluggableName(NAME_L_REMOVELAST));
        this.addPluggableName(new PluggableName(NAME_L_REMOVEAT));
        this.addPluggableName(new PluggableName(NAME_L_REVERT));
        this.addPluggableName(new PluggableName(NAME_L_MIXCOMPLETE));
        this.addPluggableName(new PluggableName(NAME_L_MIX));
        this.addPluggableName(new PluggableName(NAME_L_STATMAX));
        this.addPluggableName(new PluggableName(NAME_L_STATMIN));
        this.addPluggableName(new PluggableName(NAME_L_STATAVERAGE));
        this.addPluggableName(new PluggableName(NAME_L_STATDEVIATION));
        this.addPluggableName(new PluggableName(NAME_L_STATVARIANCE));
        this.addPluggableName(new PluggableName(NAME_L_SORTNUMBER));
        this.addPluggableName(new PluggableName(NAME_L_STRINGSORT));
        this.addPluggableName(new PluggableName(NAME_L_ENQUEUE));
        this.addPluggableName(new PluggableName(NAME_L_DEQUEUE));
    }
    
    @SuppressWarnings("unchecked")
	@Override
    public Parameter operate(Runner runner, Map<String, Parameter> operands, String name, String resultant) throws Exception
    {
        if(name.equalsIgnoreCase(NAME_ISEMPTY) || name.equalsIgnoreCase(NAME_L_ISEMPTY))
        {
            Parameter result = new Parameter();
            result.add(String.valueOf(PluggableParameterOperatorList.assertAndGetParameter(operands, "value").length() == 0));
            return result;
        }
        else if(name.equalsIgnoreCase(NAME_SIZE) || name.equalsIgnoreCase(NAME_L_SIZE))
        {
            Parameter result = new Parameter();
            Parameter list1 = PluggableParameterOperatorList.assertAndGetParameter(operands, "value");
            result.add(String.valueOf(list1.length()));
            return result;
        }
        else if(name.equalsIgnoreCase(NAME_ADDFIRST) || name.equalsIgnoreCase(NAME_L_ADDFIRST))
        {
            Parameter result = new Parameter();
            Parameter list1 = PluggableParameterOperatorList.assertAndGetParameter(operands, "value");
            Parameter list2 = PluggableParameterOperatorList.assertAndGetParameter(operands, "value2");
            int len = list2.length();
            for(int i=0; i<len; i++) result.add(list2.get(i));
            len = list1.length();
            for(int i=0; i<len; i++) result.add(list1.get(i));
            return result;
        }
        else if(name.equalsIgnoreCase(NAME_APPEND) || name.equalsIgnoreCase(NAME_ADDLAST) || name.equalsIgnoreCase(NAME_L_ADDLAST))
        {
            Parameter result = new Parameter();
            Parameter list1 = PluggableParameterOperatorList.assertAndGetParameter(operands, "value");
            Parameter list2 = PluggableParameterOperatorList.assertAndGetParameter(operands, "value2");
            int len = list1.length();
            for(int i=0; i<len; i++) result.add(list1.get(i));
            len = list2.length();
            for(int i=0; i<len; i++) result.add(list2.get(i));
            return result;        }        
        else if(name.equalsIgnoreCase(NAME_GETFIRST) || name.equalsIgnoreCase(NAME_L_GETFIRST))
        {
            Parameter result = new Parameter();
            Parameter list1 = PluggableParameterOperatorList.assertAndGetParameter(operands, "value");
            result.add(list1.get(0));
            return result;
        }
        else if(name.equalsIgnoreCase(NAME_GETLAST) || name.equalsIgnoreCase(NAME_L_GETLAST))
        {
            Parameter result = new Parameter();
            Parameter list1 = PluggableParameterOperatorList.assertAndGetParameter(operands, "value");
            result.add(list1.get(list1.length() - 1));
            return result;
        }
        else if(name.equalsIgnoreCase(NAME_REMOVEFIRST) || name.equalsIgnoreCase(NAME_L_REMOVEFIRST))
        {
            Parameter result = new Parameter();
            Parameter list1 = PluggableParameterOperatorList.assertAndGetParameter(operands, "value");
            int len = list1.length();
            for(int i=0; i<len; i++) result.add(list1.get(i));
            if(result.length() > 0) result.remove(0);
            return result;
        }
        else if(name.equalsIgnoreCase(NAME_REMOVELAST) || name.equalsIgnoreCase(NAME_L_REMOVELAST))
        {
            Parameter result = new Parameter();
            Parameter list1 = PluggableParameterOperatorList.assertAndGetParameter(operands, "value");
            int len = list1.length();
            for(int i=0; i<len; i++) result.add(list1.get(i));
            if(result.length() > 0) result.remove(list1.length() - 1);
            return result;
        }
        else if(name.equalsIgnoreCase(NAME_REMOVEAT) || name.equalsIgnoreCase(NAME_L_REMOVEAT))
        {
            Parameter result = new Parameter();
            Parameter list1 = PluggableParameterOperatorList.assertAndGetParameter(operands, "value");
            Parameter indexesParam = PluggableParameterOperatorList.assertAndGetParameter(operands, "value2");

            int len = list1.length();
            for(int i=0; i<len; i++) result.add(list1.get(i));

            int[] indexes = new int[indexesParam.length()];
            for(int i=0; i<indexes.length; i++)
            {
                indexes[i] = Integer.valueOf(indexesParam.get(i).toString());
            }
            
            for(int i=0; i<indexes.length; i++)
            {
                int index = indexes[i];
                
                result.remove(index);
                
                for(int j=i; j<indexes.length; j++)
                {
                    if(indexes[j] > index) indexes[j]--;
                }
            }
            return result;
        }
        else if(name.equalsIgnoreCase(NAME_L_REVERT)){
        	
        	Parameter result = new Parameter();
            Parameter list1 = PluggableParameterOperatorList.assertAndGetParameter(operands, "value");
            
            int len = list1.length();
            for(int i=0; i<len; i++)
            {
                result.add(list1.get(len-i-1));
            }            
        	return result;
        }
        else if(name.equalsIgnoreCase(NAME_L_MIXCOMPLETE)){
        	
        	Parameter result = new Parameter();
            Parameter list1 = PluggableParameterOperatorList.assertAndGetParameter(operands, "value");
            
            int len = list1.length();
            int len2=len;
            double d;
            int nb;
            int nb2;
            
            for (int i = 0; i < len; i++) {
                d = Math.random();
                nb = (int) (d*len*1000000);
                nb2 = nb % len2;
				result.add(list1.get(nb2));
            	list1.remove(nb2);
            	len2=list1.length();
			}
            
        	return result;
        }
        else if(name.equalsIgnoreCase(NAME_L_MIX)){
        	
            Parameter result = PluggableParameterOperatorList.assertAndGetParameter(operands, "value");
            Parameter list2 = PluggableParameterOperatorList.assertAndGetParameter(operands, "value2");
            
            int len = result.length();
            int value = Integer.valueOf(list2.get(0).toString());
            
            int d1,d2;
            
            for (int i = 0; i < value; i++) {
            	d1 = (int)((Math.random())*len*100)%len;
            	d2 = (int)((Math.random())*len*100)%len;
            	Object plop = result.get(d1);
            	try {
            		result.set(d1, result.get(d2));
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					result.set(d2, plop);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
            
        	return result;
        }
        else if(name.equalsIgnoreCase(NAME_L_STATMAX)){
        	
        	Parameter result = new Parameter();
            Parameter list1 = PluggableParameterOperatorList.assertAndGetParameter(operands, "value");
            Vector<Object> list2 = list1.getArray();
            int len = list1.length();
            
            double res = Double.parseDouble(list2.get(0).toString());
            for (int i = 1; i < len; i++) {
				if (res<Double.parseDouble(list2.get(i).toString())) {
					res=Double.parseDouble(list2.get(i).toString());
				}
			}
            result.add(formatDouble(res));
            
            return result;
        }
        else if(name.equalsIgnoreCase(NAME_L_STATMIN)){
        	
        	Parameter result = new Parameter();
            Parameter list1 = PluggableParameterOperatorList.assertAndGetParameter(operands, "value");
            Vector<Object> list2 = list1.getArray();
            int len = list1.length();
            
            double res = Double.parseDouble(list2.get(0).toString());
            for (int i = 1; i < len; i++) {
				if (res>Double.parseDouble(list2.get(i).toString())) {
					res=Double.parseDouble(list2.get(i).toString());
				}
			}
            result.add(formatDouble(res));
            
            return result;
        }
        else if(name.equalsIgnoreCase(NAME_L_STATAVERAGE)){
        	
        	Parameter result = new Parameter();
            Parameter list1 = PluggableParameterOperatorList.assertAndGetParameter(operands, "value");
            Vector<Object> list2 = list1.getArray();
            double len = list1.length();
            
            double res = Double.parseDouble(list2.get(0).toString());
            for (int i = 1; i < len; i++) {
				res+=Double.parseDouble(list2.get(i).toString());
			}
            res=res/len;
            result.add(formatDouble(res));
            
            return result;
        }
        else if(name.equalsIgnoreCase(NAME_L_STATDEVIATION)){
        	
        	Parameter result = new Parameter();
            Parameter list1 = PluggableParameterOperatorList.assertAndGetParameter(operands, "value");
            Vector<Object> list2 = list1.getArray();
            double len = list1.length();
            
            double res = Double.parseDouble(list2.get(0).toString())*Double.parseDouble(list2.get(0).toString());
            for (int i = 1; i < len; i++) {
				res+=(Double.parseDouble(list2.get(i).toString()))*(Double.parseDouble(list2.get(i).toString()));
			}
            res=res/len;
            
            double moy = Double.parseDouble(list2.get(0).toString());
            for (int i = 1; i < len; i++) {
				moy+=Double.parseDouble(list2.get(i).toString());
			}
            moy=moy/len;
            moy=moy*moy;
            
            res = res - moy;
            res=Math.sqrt(res);
            result.add(formatDouble(res));
            return result;
        }
        else if(name.equalsIgnoreCase(NAME_L_STATVARIANCE)){ // sans biais
        	
        	Parameter result = new Parameter();
            Parameter list1 = PluggableParameterOperatorList.assertAndGetParameter(operands, "value");
            int len = list1.length();
            Vector<Object> list2 = list1.getArray();
            
            double res = Double.parseDouble(list2.get(0).toString())*Double.parseDouble(list2.get(0).toString());
            for (int i = 1; i < len; i++) {
				res+=(Double.parseDouble(list2.get(i).toString()))*(Double.parseDouble(list2.get(i).toString()));
			}
            res = res / len;
            
            double moy = Double.parseDouble(list2.get(0).toString());
            for (int i = 1; i < len; i++) {
				moy+=Double.parseDouble(list2.get(i).toString());
			}
            moy=moy/len;
            moy=moy*moy;
            
            res = res-moy;
            
            result.add(formatDouble(res));
            
            return result;
        }
        else if(name.equalsIgnoreCase(NAME_L_SORTNUMBER)){
        	
        	Parameter result = new Parameter();
            Parameter list1 = PluggableParameterOperatorList.assertAndGetParameter(operands, "value");

        	int len=list1.length();
        	Vector<Object> list2 = list1.getArray();
        	ArrayList<Integer> list3 = new ArrayList<Integer>();
            for (int i = 0; i < len; i++) {
            	list3.add(Integer.valueOf(list2.get(i).toString()));
			}
            Collections.sort(list3);
            for (int j = 0; j < len; j++) {
				result.add(String.valueOf(list3.get(j)));
			}
        	 
        	return result;
        }
        else if(name.equalsIgnoreCase(NAME_L_STRINGSORT)){
        	Parameter result = new Parameter();
            Parameter list1 = PluggableParameterOperatorList.assertAndGetParameter(operands, "value");

        	int len=list1.length();
            Vector<Object> list2 = list1.getArray();
            ArrayList<String> list3 = new ArrayList<String>();
            for (int i = 0; i < len; i++) {
            	list3.add(list2.get(i).toString());
			}
            Collections.sort(list3);
            for (int j = 0; j < len; j++) {
				result.add(list3.get(j));
			}
            
        	return result;
        }
        else if (NAME_L_ENQUEUE.equalsIgnoreCase(name) || NAME_QUEUE_ENQUEUE.equalsIgnoreCase(name)) {
        	Parameter result = new Parameter();        	
            Parameter param1 = operands.get("value");
            Parameter param2 = operands.get("value2");
            String queueName = (String) param1.get(0);
            LinkedBlockingQueue<Parameter> queue;
            synchronized (_queues) {
                queue = _queues.get(queueName);
                if (null == queue) {
                    queue = new LinkedBlockingQueue<Parameter>();
                    _queues.put(queueName, queue);
                }
            }
            queue.put(param2);
            return result;
        }
        else if (NAME_L_DEQUEUE.equalsIgnoreCase(name) || NAME_QUEUE_DEQUEUE.equalsIgnoreCase(name)) {
        	Parameter result = new Parameter();
            Parameter param1 = operands.get("value");
            Parameter param2 = operands.get("value2");	            
            String queueName = (String) param1.get(0);	            
            LinkedBlockingQueue<Parameter> queue;
            synchronized (_queues) {
                queue = _queues.get(queueName);
                if (null == queue) {
                    queue = new LinkedBlockingQueue<Parameter>();
                    _queues.put(queueName, queue);
                }
            }
            if(null != param2){
                String timeoutStr = (String) param2.get(0);
                int timeoutInt = Integer.parseInt(timeoutStr);
                result = queue.poll(timeoutInt, TimeUnit.SECONDS);
                if(null == result){
                    result = new Parameter();
                }
            }
            else{
                result = queue.take();
            }
            return result;
        }
        else throw new RuntimeException("unsupported operation " + name);
    }

}
