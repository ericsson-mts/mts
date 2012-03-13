/*
 * Created on 26 sept. 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.devoteam.srit.xmlloader.rtp.jmf;

/**
 * @author ma007141
 *
 */
import java.util.HashSet;
import java.util.Iterator;

public class OptionList {
    private HashSet list;

    public OptionList() {
        list = new HashSet();
    }

    public void add(Option o) {
        list.add(o);
    }

    public Iterator iterator() {
        return list.iterator();
    }

    public int size() {
        return list.size();
    }
}
