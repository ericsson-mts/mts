package com.devoteam.srit.xmlloader.pcp;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.portal.pcm.EBufException;
import com.portal.pcm.FList;
import com.portal.pcm.Field;
import com.portal.pcm.SparseArray;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Enumeration;

public class MsgPcp extends Msg
{	
	private FList flist;
    private FList flistSearch = null;
    private String type = null;
    private int opcode = 1;
    private String opcodeStr = null;
    
    // --- constructor --- //
    public MsgPcp(FList flist) throws Exception {
        this.flist = flist;
	}
 
    // --- heritage methods --- //
    public String getProtocol(){
        return StackFactory.PROTOCOL_PCP;
    }

    public String getType() {
        if(null == type)
        {
            //TODO
        }
        return type;
	}

    public String getResult(){
        if (!isRequest()){        	
            //TODO
        	return "OK";
        }
        return null;
    }
    public boolean isRequest() {
        //TODO
    	return true;
	}
		
	// --- get Parameters --- //
    @Override
	public synchronized Parameter getParameter(String path) throws Exception 
	{
		Parameter var = super.getParameter(path);	
		if (var != null) {
			return var;
		}

		var = new Parameter();
        String[] params = Utils.splitPath(path);

        //---------------------------------------------------------------------- data -
        if (params.length >= 1 && params[0].equalsIgnoreCase("data"))
		{
            var.add(flist.asString());
		}
        //---------------------------------------------------------------------- param1=x:param2=y:...-
        else if (params.length >= 1)
		{
            searchAndAddParameter(var, params, 0);
		}
        else
        {
        	Parameter.throwBadPathKeywordException(path);
        }

		return var;
	}

    public void searchAndAddParameter(Parameter var, String[] paths, int depth) throws EBufException
    {
        String[] equals = Utils.splitNoRegex(paths[depth], "=");
        Object obj = null;
        if(flistSearch == null)
            flistSearch = flist;

        Enumeration list = flistSearch.getFields();
        Field field = null;
        while(list.hasMoreElements())
        {
            field = (Field)list.nextElement();
            if(field.getNameString().contains(equals[0].toUpperCase()))
            {
                obj = flistSearch.getField(field);
                if(equals.length > 1)//if there is a value specified for a parameter
                {
                    if(field.getTypeID() == 9) {
                        flistSearch = ((SparseArray)obj).elementAt(Integer.parseInt(equals[1]));
                        obj = flistSearch;
                    }
                    else if(field.getTypeID() == 10)
                        flistSearch = (FList)obj;
                }
            }
        }

        if(paths.length > depth + 1){
            searchAndAddParameter(var, paths, depth + 1);
        }
        else {
            var.add(obj);
            flistSearch = null;
        }
    }

    public FList getFlist() {
        return flist;
    }

    public int getOpCode() {
        return opcode;
    }

    /** Get the data (as binary) of this message */
    @Override
    public byte[] getBytesData(){
        ObjectOutputStream obj;
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        try {
            obj = new ObjectOutputStream(byteArray);
            this.flist.writeExternal(obj);
            obj.flush();
            obj.close();
        } catch (IOException e) {
        }
        return byteArray.toByteArray();
    }

    /** Returns a short description of the message. Used for logging as INFO level */
    /** This methods HAS TO be quick to execute for performance reason */
    @Override
	public String toShortString() throws Exception {
    	String ret = super.toShortString();
        ret += this.flist.asString();
        return ret;
	}

    /** Get the XML representation of the message; for the genscript module. */
    @Override
    public String toXml() throws Exception {
    	return flist.asString();
    }
}