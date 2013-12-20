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

package com.devoteam.srit.xmlloader.diameter.dictionary;

import com.devoteam.srit.xmlloader.core.exception.ParsingException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.dom4j.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author gpasquiers
 */
public class Application
{
    private String  _name ;
    private int     _id ;
    
    private HashMap<String, CommandDef>   commandDefByName ;
    private HashMap<String, CommandDef>   commandDefByCode ;
    
    private HashMap<String, TypeDef>      typeDefByName ;
    
    private HashMap<String, VendorDef>    vendorDefByName ;
    private HashMap<String, VendorDef>    vendorDefByCode ;
    
    private HashMap<String, AvpDef>       avpDefByName ;
    private HashMap<String, AvpDef>       avpDefByCode ;
    
    
    
    /** Creates a new instance of Application */
    public Application(String name, int id) throws ParsingException
    {
        this._name = name ;
        this._id = id ;
        this.commandDefByName= new HashMap<String, CommandDef>();
        this.commandDefByCode= new HashMap<String, CommandDef>();
        this.typeDefByName   = new HashMap<String, TypeDef>();
        this.vendorDefByName = new HashMap<String, VendorDef>();
        this.vendorDefByCode = new HashMap<String, VendorDef>();
        this.avpDefByName    = new HashMap<String, AvpDef>();
        this.avpDefByCode    = new HashMap<String, AvpDef>();
    }
    
    
    public void parseApplication(Element root) throws ParsingException
    {
        List<Element> elements;
        
        elements = root.elements("vendor");
        for(Element element:elements)
        {
            parseVendor(element);
        }
        
        elements = root.elements("command");
        for(Element element:elements)
        {
            parseCommand(element);
        }
        
        elements = root.elements("typedefn");
        for(Element element:elements)
        {
            parseType(element);
        }
        
        
        elements = root.elements("avp");
        for(Element element:elements)
        {
            parseAvp(element);
        }
    }
    
    private void parseVendor(Element root) throws ParsingException
    {
        
        int code = -1 ;
        try
        {
            if(null!=root.attribute("code"))
            {
                code = Integer.parseInt(root.attributeValue("code"));
            }
            else
            {
                Dictionary.traceWarning("No vendor.code, skipping");
                return ;
            }
        }
        catch(Exception e)
        {
            Dictionary.traceWarning("Invalid vendor.code, skipping");
            return ;
        }
        
        String vendor_id = null ;
        if(null != root.attributeValue("vendor-id")) vendor_id = root.attributeValue("vendor-id");
        else
        {
            Dictionary.traceWarning("No vendor.vendor-id, skipping");
            return ;
        }
        
        String name = null ;
        if(null != root.attributeValue("name")) name = root.attributeValue("name");
        
        VendorDef vendorDef = new VendorDef(code, vendor_id, name);
        
        if(null != getVendorDefByCode(code)) Dictionary.traceWarning("VendorDef of code " + code + " already exists, overwriting");
        if(null != getVendorDefByName(vendor_id)) Dictionary.traceWarning("VendorDef of vendor-id " + vendor_id + " already exists, overwriting");
        
        vendorDefByName.put(vendor_id, vendorDef);
        vendorDefByCode.put(Integer.toString(code), vendorDef);
        
    }
    
    private void parseAvp(Element root) throws ParsingException
    {
        String name = null ;
        String description = null ;
        String may_encrypt = null ;
        String mandatory = null ;
        String protected_ = null ;
        String vendor_bit = null ;
        
        if(null != root.attributeValue("name")) name = root.attributeValue("name");
        else
        {
            Dictionary.traceWarning("Invalid avp.name, skipping");
            return;
        }
        if(null != root.attributeValue("description")) description = root.attributeValue("description");
        if(null != root.attributeValue("may-encrypt")) may_encrypt = root.attributeValue("may-encrypt");
        if(null != root.attributeValue("mandatory")) mandatory = root.attributeValue("mandatory");
        if(null != root.attributeValue("protected")) protected_ = root.attributeValue("protected");
        if(null != root.attributeValue("vendor-bit")) vendor_bit = root.attributeValue("vendor-bit");
        
        int code ;
        try
        {
            code = Integer.parseInt(root.attributeValue("code"));
        }
        catch(Exception e)
        {
            code = -1 ;
        }
        
        if(code == -1)
        {
            Dictionary.traceWarning("Missing avp.code, skipping");
            return;
        }
        
        boolean constrained = false ;
        if(null != root.attributeValue("constrained")) constrained = Boolean.parseBoolean(root.attributeValue("constrained"));
        
        VendorDef vendor_id = null ;
        if(null != root.attributeValue("vendor-id")) vendor_id = Dictionary.getInstance().getVendorDefByName(root.attributeValue("vendor-id"), _name);
        
        
        TypeDef type = null ;
        {
            List<Element> elements;
            elements = root.elements("type");
            for(Element element:elements)
            {
                String type_name = element.attributeValue("type-name");
                type = Dictionary.getInstance().getTypeDefByName(type_name, _name) ;
                if(type == null)
                {
                    Dictionary.traceWarning("Invalid avp.type (" + type_name + "), skipping");
                }
            }
        }
        
        AvpDef avpDef = new AvpDef(name, code, description, may_encrypt, protected_, vendor_bit, mandatory, constrained, type, vendor_id);
        
        // parse enums
        {
            List<Element> elements;
            elements = root.elements("enum");
            for(Element element:elements)
            {
                String enum_name = null ;
                if(null != element.attribute("name"))
                {
                    enum_name = element.attributeValue("name");
                }
                else
                {
                    Dictionary.traceWarning("No avp.enum.name in " + avpDef.get_name() + ", skipping this enum");
                    continue ;
                }
                
                int enum_code = -1 ;
                if(null != element.attribute("code"))
                {
                    try
                    {
                        enum_code = Integer.parseInt(element.attributeValue("code"));
                    }
                    catch(Exception e)
                    {
                        Dictionary.traceWarning("Invalid avp.enum.code in " + avpDef.get_name() + ", skipping this enum");
                        continue ;
                    }
                }
                else
                {
                    Dictionary.traceWarning("No avp.enum.code in " + avpDef.get_name() + ", skipping this enum");
                    continue ;
                } 
                
                avpDef.addEnum(enum_name, enum_code);
            }
        }
        
        // parse grouped
        {
            Element elementGrouped = root.element("grouped");
            if(null != elementGrouped)
            {
                List<Element> elements;
                elements = elementGrouped.elements("gavp");

                for(Element element:elements)
                {
                    String gavp_name = element.attributeValue("name");
                    avpDef.addGroupedAvpName(gavp_name);
                }
            }
        }
        
        if(null != getAvpDefByCode(avpDef.get_code())) Dictionary.traceWarning("AvpDef of code " + avpDef.get_code() + " already exists, overwriting");
        if(null != getAvpDefByName(avpDef.get_name())) Dictionary.traceWarning("AvpDef of name " + avpDef.get_name() + " already exists, overwriting");
        
        avpDefByName.put(avpDef.get_name(), avpDef);
        avpDefByCode.put(Integer.toString(avpDef.get_code()), avpDef);
    }
    
    private void parseCommand(Element root) throws ParsingException
    {
        int code = -1 ;
        try
        {
            if(null!=root.attribute("code")) code = Integer.parseInt(root.attributeValue("code"));
            else
            {
                Dictionary.traceWarning("No commands.code, skipping");
                return ;
            }
        }
        catch(Exception e)
        {
            Dictionary.traceWarning("Invalid command.code, skipping");
            return ;
        }
        
        VendorDef vendor_id = null ;
        if(null != root.attributeValue("vendor-id")) Dictionary.getInstance().getVendorDefByName(root.attributeValue("vendor-id"), _name);
        
        String name = null ;
        if( null != root.attributeValue("name")) name = root.attributeValue("name");
        else
        {
            Dictionary.traceWarning("Invalid command.name, skipping");
            return;
        }
        
        CommandDef commandDef = new CommandDef(code, name, vendor_id);
        
        if(null != getCommandDefByCode(code)) Dictionary.traceWarning("CommandDef of code " + code + " already exists, overwriting");
        if(null != getCommandDefByName(name)) Dictionary.traceWarning("CommandDef of name " + name + " already exists, overwriting");
        
        commandDefByName.put(name, commandDef);
        commandDefByCode.put(Integer.toString(code), commandDef);
    }
    
    private void parseType(Element root) throws ParsingException
    {
        TypeDef type_parent = null ;
        if(null != root.attributeValue("type-parent"))
        {
            type_parent = Dictionary.getInstance().getTypeDefByName(root.attributeValue("type-parent"), _name);
            if(type_parent == null)
            {
                Dictionary.traceWarning("Invalid typedefn.type-parent, skipping");
                return;
            }
        }
        
        String name = null ;
        
        if(null != root.attributeValue("type-name")) name = root.attributeValue("type-name");
        else
        {
            Dictionary.traceWarning("Invalid typedefn.name, skipping");
            return;
        }
        
        TypeDef typeDef = new TypeDef(name, type_parent);
        
        if(null != getTypeDefByName(name)) Dictionary.traceWarning("TypeDef of name " + name + " already exists, overwriting");
        
        typeDefByName.put(name, typeDef);
    }
    
    public void fillGroupedAvpsReferences() throws ParsingException
    {
        Collection<AvpDef> collection = avpDefByName.values() ;
        Iterator<AvpDef> iterator = collection.iterator();
        while(iterator.hasNext())
        {
            AvpDef avpDef = iterator.next();
            
            Iterator<String> gIterator = avpDef.getGroupedAvpNameList().iterator();
            while(gIterator.hasNext())
            {
                String gAvpDefName = gIterator.next();
                
                AvpDef gAvpDef = Dictionary.getInstance().getAvpDefByName(gAvpDefName, _name);
                if(null != gAvpDef) avpDef.addGroupedAvpDef(gAvpDef);
                else                Dictionary.traceWarning("no AvpDef found for gavp " + gAvpDefName + " of AvpDef " + avpDef.get_name());
            }
        }
    }
    
    public VendorDef getVendorDefByName(String name)
    {
        return vendorDefByName.get(name);
    }
    
    public VendorDef getVendorDefByCode(int code)
    {
        return vendorDefByCode.get(Integer.toString(code));
    }
    
    public TypeDef getTypeDefByName(String name)
    {
        return typeDefByName.get(name);
    }
    
    public CommandDef getCommandDefByName(String name)
    {
        return commandDefByName.get(name);
    }
    
    public CommandDef getCommandDefByCode(int code)
    {
        return commandDefByCode.get(Integer.toString(code));
    }
    
    public AvpDef getAvpDefByCode(int code)
    {
        return avpDefByCode.get(Integer.toString(code));
    }
    
    public AvpDef getAvpDefByName(String name)
    {
        return avpDefByName.get(name);
    }
    
    public int get_id()
    {
        return _id ;
    }
    
    public String get_name()
    {
        return _name ;
    }
}
