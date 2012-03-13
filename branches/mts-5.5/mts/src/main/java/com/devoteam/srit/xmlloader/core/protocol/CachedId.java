/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.devoteam.srit.xmlloader.core.protocol;

/**
 *
 * @author gpasquiers
 */
public class CachedId {

    private String value;
    private int hashCode;
    private boolean hashCodeCached;

    public CachedId(String value) {
        if (null == value) {
            throw new RuntimeException("A " + getClass().getName() + " MUST NOT have a null value.");
        }

        this.value = value;
        this.hashCodeCached = false;
    }

    @Override
    public String toString() {
        return this.value;
    }

    @Override
    public int hashCode() {
        if (!this.hashCodeCached) {
            this.hashCode = this.value.hashCode();
            this.hashCodeCached = true;
        }

        return this.hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CachedId other = (CachedId) obj;
        if (this.hashCode() != other.hashCode()) {
            return false;
        }
        if (other.value.length() != value.length()) {
            return false;
        }
        return other.value.equals(value);
    }
}
