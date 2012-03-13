/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.devoteam.srit.xmlloader.stun;

import com.devoteam.srit.xmlloader.core.Parameter;
import gp.utils.arrays.*;


/**
 *
 * @author indiaye
 */
public class HeaderStun {

    private Array header;
    private Integer16Array type;
    private Integer16Array length;
    private Array transactionId;
    private Array magicCookie;

    public HeaderStun() {
        this.header = new DefaultArray(20);
        this.transactionId = this.header.subArray(8,12);
        this.type = new Integer16Array(this.header.subArray(0,2));
        this.length = new Integer16Array(this.header.subArray(2,2));
        this.magicCookie = this.header.subArray(4,4);
        this.magicCookie =Array.fromHexString("2112A442");
    }

    public HeaderStun(Array data) {
        this.header = data.subArray(0,20);
        this.transactionId = this.header.subArray(8,12);
        this.type = new Integer16Array(this.header.subArray(0, 2));
        this.length = new Integer16Array(this.header.subArray(2, 2));
        this.magicCookie = this.header.subArray(4,4);
    }

    public int getLength() {
        return length.getValue();
    }

    public void setLength(int length) {
        this.length.setValue(length);
    }

    public Array getHeader() {
        return header;
    }

    public String getTransactionId() {
        return Array.toHexString(this.transactionId);
    }

    public void setTransactionId(Array transactionId) {
        if (transactionId.length != this.transactionId.length) {
            throw new ArrayIndexOutOfBoundsException("the size " + transactionId.length + " of transactionId is not adequate to" + this.transactionId.length);
        }
        this.transactionId = transactionId;
    }

    public int getType() {
        return type.getValue();
    }

    public void setType(int type) {
        this.type.setValue(type);
    }

    public Array getValue() {
        SupArray array = new SupArray();
        array.addLast(type);
        array.addLast(length);
        array.addLast(magicCookie);
        array.addLast(transactionId);
        return array;

    }

    @Override
    public String toString() {
        StringBuilder headerString = new StringBuilder();
        headerString.append("  <header ");
        headerString.append("type=\"" + this.getType() + "\" ,");
        headerString.append(" transactionID=\"" + this.getTransactionId() + "\" ,");
        headerString.append(" length=\"" + this.getLength() + "\" />");
        headerString.append("\n");

        return headerString.toString();


    }

    public Parameter getParameterHeader(String param) {
        Parameter var = new Parameter();
        if (param.equalsIgnoreCase("type")) {
            var.add(this.type.getValue());
        } else if (param.equalsIgnoreCase("transactionID")) {
            var.add(Array.toHexString(this.transactionId));
        } else if (param.equalsIgnoreCase("length")) {
            var.add(this.length.getValue());
        }
        return var;
    }
}
