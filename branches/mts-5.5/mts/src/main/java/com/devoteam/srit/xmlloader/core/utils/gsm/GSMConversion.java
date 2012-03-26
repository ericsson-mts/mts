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

package com.devoteam.srit.xmlloader.core.utils.gsm;

import gp.utils.arrays.DefaultArray;

/**
 * class to encode and decode in GSM alphabet GSM_03.38
 *
 * @author Bouvier Benjamin
 */
public final class GSMConversion {

    public static final char EXT_TABLE_PREFIX = 0x1B;

    /**
     * Default alphabet table according to GSM 03.38.
     */
    public static final char[] GSM_DEFAULT_ALPHABET_TABLE = {
        //'@', '£', '$', '¥', 'è', 'é', 'ù', 'ì', 'ò', 'ç', LF, 'Ø', 'ø', CR, 'Å', 'å',
        '@', '£', '$', 0xa5, 0xe8, 0xe9, 0xf9, 0xec, 0xf2, 0xe7, 10, 0xd8, 0xf8, 13, 0xc5, 0xe5,
        //'delta', '_', 'phi', 'gamma', 'lambda', 'omega', 'pi', 'psi', 'sigma', 'theta', 'xi', 'EXT_TABLE_PREFIX', 'Æ', 'æ', 'ß', 'É',
        0x394, '_', 0x3a6, 0x393, 0x39b, 0x3a9, 0x3a0, 0x3a8, 0x3a3, 0x398, 0x39e, 0xa0, 0xc6, 0xe6, 0xdf, 0xc9,
        //' ', '!', '"', '#', '¤', '%', '&', ''', '(', ')', '*', '+', ',', '-', '.', '/',
        ' ', '!', '"', '#', 0xa4, '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/',
        //'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ':', ';', '<', '=', '>', '?',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ':', ';', '<', '=', '>', '?',
        //'¡', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O',
        0xa1, 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O',
        //'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'Ä', 'Ö', 'Ñ', 'Ü', '§',
        'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 0xc4, 0xd6, 0xd1, 0xdc, 0xa7,
        //'¿', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o',
        0xbf, 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o',
        //'p', 'q', 'r', 's', 't', 'u', 'v', 'w',  'x', 'y', 'z', 'ä', 'ö', 'ñ', 'ü', 'à',
        'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 0xe4, 0xf6, 0xf1, 0xfc, 0xe0};
        
    /**
     * Extension table of the default 7 bits GSM alphabet
     *
     * NU means not used
     */
    public static final char[] GSM_DEFAULT_ALPHABET_EXTENSION_TABLE = {
        //'NU', 'NU', 'NU', 'NU', 'NU', 'NU', 'NU', 'NU', 'NU', 'NU', 'PAGE BREAK', 'NU', 'NU', 'NU', 'NU', 'NU',
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0x0c, 0, 0, 0, 0, 0,
        //'NU', 'NU', 'NU', 'NU', '^', 'NU', 'NU', 'NU', 'NU', 'NU', 'NU', 'EXT_TABLE_PREFIX', 'NU', 'NU', 'NU', 'NU',
        0, 0, 0, 0, 0, '^', 0, 0, 0, 0, 0, 0xa0, 0, 0, 0, 0,
        //'NU', 'NU', 'NU', 'NU', 'NU', 'NU', 'NU', 'NU', '{', '}', 'NU', 'NU', 'NU', 'NU', 'NU', '\',
        0, 0, 0, 0, 0, 0, 0, 0, '{', '}', 0, 0, 0, 0, 0, '\\',
        //'NU', 'NU', 'NU', 'NU', 'NU', 'NU', 'NU', 'NU', 'NU', 'NU', 'NU', 'NU', '[', '~', ']', 'NU',
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, '[', '~', ']', 0,
        //'|', 'NU', 'NU', 'NU', 'NU', 'NU', 'NU', 'NU', 'NU', 'NU', 'NU', 'NU', 'NU', 'NU', 'NU', 'NU',
        '|', 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        //'NU', 'NU', 'NU', 'NU', 'NU', 'Euro Symbol', 'NU', 'NU', 'NU', 'NU', 'NU', 'NU', 'NU', 'NU', 'NU', 'NU',
        0, 0, 0, 0, 0, 0x20AC, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        //'NU', 'NU', 'NU', 'NU', 'NU', 'NU', 'NU', 'NU', 'NU', 'NU', 'NU', 'NU', 'NU', 'NU', 'NU', 'NU',
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        //'NU', 'NU', 'NU', 'NU', 'NU', 'NU', 'NU', 'NU', 'NU', 'NU', 'NU', 'NU', 'NU', 'NU', 'NU', 'NU',
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    /**
     * Converts from the GSM charset to a unicode string
     */
    public static byte[] fromGsmCharset(String str) {
        DefaultArray strBytes = new DefaultArray(str.length() * 2);
        int length = 0;
        char charReturned;

        for (int i = 0; i < str.length(); i++) {
            charReturned = fromGsmCharset((byte)str.charAt(i));
            if(str.charAt(i) == EXT_TABLE_PREFIX) {//so search in extension table
                charReturned = GSM_DEFAULT_ALPHABET_EXTENSION_TABLE[(byte)str.charAt(++i)];
            }
            strBytes.set(length++, charReturned);
        }

        return strBytes.subArray(0, length).getBytes();
    }
    
    /**
     * Convert from the GSM charset to a unicode char
     */
    public static char fromGsmCharset(byte gsmChar) {
        return GSM_DEFAULT_ALPHABET_TABLE[gsmChar];
    }

    /**
     * Converts a unicode string to GSM charset
     */
    public static byte[] toGsmCharset(String str) {
        byte byteReturned;
        DefaultArray gsmBytes = new DefaultArray(str.length() * 2);
        int length = 0;

        for (int i = 0; i < str.length(); i++) {
            byteReturned = toGsmCharset(str.charAt(i));
            if(byteReturned == EXT_TABLE_PREFIX)//so search in extension table
            {
                // check next characters
                for (int j = 0; j < GSM_DEFAULT_ALPHABET_EXTENSION_TABLE.length; j++) {
                    if (GSM_DEFAULT_ALPHABET_EXTENSION_TABLE[j] == str.charAt(i)) {
                        byteReturned = (byte) j;
                        gsmBytes.set(length++, EXT_TABLE_PREFIX);
                        gsmBytes.set(length++, byteReturned);
                        break;
                    }
                }
            }
            else
            {
                gsmBytes.set(length++, byteReturned);
            }
        }
        return gsmBytes.subArray(0, length).getBytes();
    }

    /**
     * Convert a unicode char to a GSM char
     */
    public static byte toGsmCharset(char ch) {
        byte charToReturn = EXT_TABLE_PREFIX;//for an unknown char, so search character in extension table

        // First check through the GSM charset table
        for (int i = 0; i < GSM_DEFAULT_ALPHABET_TABLE.length; i++) {
            if (GSM_DEFAULT_ALPHABET_TABLE[i] == ch) {
                charToReturn = (byte) i;
                break;
            }
        }
        return charToReturn;
    }

}
