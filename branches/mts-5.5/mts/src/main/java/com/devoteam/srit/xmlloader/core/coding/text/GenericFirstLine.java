package com.devoteam.srit.xmlloader.core.coding.text;


/**
 *
 * @author indiaye
 */
//this class is empty and is used for textMessage Class
public class GenericFirstLine {




   protected String line="";
   protected String[] tabGeneric;

    public GenericFirstLine(String line) {
        if (line==null)
            line="";
       this.line=line;
      
        this.tabGeneric=line.split(" ");
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

  public String[] getTabGeneric() {
        return tabGeneric;
    }

    public void setTabGeneric(String[] tabGeneric) {
        this.tabGeneric = tabGeneric;
    }

}
