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

package com.devoteam.srit.xmlloader.core.log;

import java.io.Serializable;

/**
 * @author pn007888 An event containing text and debug level
 */
public class TextEvent implements Serializable {
    // Log levels

    public final static int DEBUG = 0;
    public final static int INFO = 1;
    public final static int WARN = 2;
    public final static int ERROR = 3;
    // Levels description
    public final static String DEBUG_STRING = "DEBUG";
    public final static String INFO_STRING = "INFO";
    public final static String WARN_STRING = "WARN";
    public final static String ERROR_STRING = "ERROR";

    public enum Topic {

        USER,
        PARAM,
        CORE,
        CALLFLOW,
        PROTOCOL,
        MASTER
    }
    private int level;
    private String text;
    private Topic topic;
    private long timestamp;
    private static long counter = 0;
    private long index;
    private boolean open = false;

    public TextEvent(String text, int level, Topic topic) {
        this.text = text;
        this.level = level;
        this.topic = topic;
        this.timestamp = System.currentTimeMillis();
        this.index = counter++;

    }

    public TextEvent(String text, int level, Topic topic, long timestamp) {
        this.text = text;
        this.level = level;
        this.topic = topic;
        this.timestamp = timestamp;
        this.index = counter++;
    }

    public TextEvent(String text, int level, Topic topic, long timestamp, long index, boolean open) {
        this.text = text;
        this.level = level;
        this.topic = topic;
        this.timestamp = timestamp;
        this.index = index;
        this.open = open;
    }

    public String getText() {
        return text;
    }

    public int getLevel() {
        return level;
    }

    public Topic getTopic() {
        return topic;
    }

    public String getLevelStr() {
        switch (level) {

            case TextEvent.INFO:
                return INFO_STRING;
            case TextEvent.WARN:
                return WARN_STRING;
            case TextEvent.ERROR:
                return ERROR_STRING;
            case TextEvent.DEBUG:
                return DEBUG_STRING;
            default:
                return null;
        }
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public long getIndex() {
        return this.index;
    }

        public boolean getOpen() {
        return this.open;
    }

    /**
     * @param level the level to convert
     * @return a string representation of this level
     */
    public static final String level2String(int level) {
        switch (level) {
            case TextEvent.INFO:
                return INFO_STRING;
            case TextEvent.WARN:
                return WARN_STRING;
            case TextEvent.ERROR:
                return ERROR_STRING;
            default:
            case TextEvent.DEBUG:
                return DEBUG_STRING;
        }
    }

    /**
     * @param level the level to convert
     * @return a string representation of this level
     */
    public static final int string2Level(String string) {
        if (string.equalsIgnoreCase(ERROR_STRING)) {
            return TextEvent.ERROR;
        }
        if (string.equalsIgnoreCase(WARN_STRING)) {
            return TextEvent.WARN;
        }
        if (string.equalsIgnoreCase(INFO_STRING)) {
            return TextEvent.INFO;
        }
        return TextEvent.DEBUG;
    }
}
