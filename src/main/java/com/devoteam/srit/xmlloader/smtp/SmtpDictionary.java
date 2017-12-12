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

package com.devoteam.srit.xmlloader.smtp;

import java.util.HashSet;



public class SmtpDictionary
{
    private static SmtpDictionary instance = null;
    
    public static SmtpDictionary instance()
    {
        if(null == instance)
        {
            instance = new SmtpDictionary();
        }
        
        return instance;
    }
    
    private HashSet<String> commands = null;
    private HashSet<String> results = null;

    public SmtpDictionary()
    {
        commands = new HashSet<String>();
        commands.add (COMMAND_CRLF);
        commands.add (COMMAND_RSET);
        commands.add (COMMAND_DATA);
        commands.add (COMMAND_QUIT);
        commands.add (COMMAND_HELO);
        commands.add (COMMAND_EHLO);
        commands.add (COMMAND_MAIL);
        commands.add (COMMAND_RCPT);
        commands.add (COMMAND_VRFY);
        commands.add (COMMAND_EXPN);
        commands.add (COMMAND_HELP);
        commands.add (COMMAND_NOOP);
        commands.add (DEFINI_CNTT);
        commands.add(COMMAND_STARTTLS);

        results = new HashSet<String>();
        results.add (REP_DATA_220);
        results.add (REP_DATA_550);
        results.add (REP_DATA_354);
        results.add (REP_DATA_221);
        results.add (REP_DATA_250);
    }

    public boolean containsResult(String result)
    {
        return results.contains(result);
    }

    public boolean containsCommand(String command)
    {
        return commands.contains(command);
    }

    // command name
    public static final String COMMAND_CRLF = "CRLF";
    public static final String COMMAND_RSET = "RSET";
    public static final String COMMAND_DATA = "DATA";
    public static final String COMMAND_QUIT = "QUIT";
    public static final String COMMAND_HELO = "HELO";
    public static final String COMMAND_EHLO = "EHLO";
    public static final String COMMAND_MAIL = "MAIL";
    public static final String COMMAND_RCPT = "RCPT";
    public static final String COMMAND_VRFY = "VRFY";
    public static final String COMMAND_EXPN = "EXPN";
    public static final String COMMAND_HELP = "HELP";
    public static final String COMMAND_NOOP = "NOOP";
    public static final String DEFINI_CNTT = "CNTT";
    public static final String COMMAND_STARTTLS = "STARTTLS";
    // reply code & reply data
    public static final String REP_DATA_220 = "220";// "Simple Mail Transfer Service Ready";
    public static final String REP_DATA_550 = "550";// "No such user here";
    public static final String REP_DATA_354 = "354";// "Start mail input; end with <CRLF>.<CRLF>";
    public static final String REP_DATA_221 = "221";// "Service closing transmission channel";
    public static final String REP_DATA_250 = "250";// "OK";
    
    /*
     * 500 For the "command line too long" case or if the command not
     * recognized. Note that producing a "command not recognized" error in
     * response to the required subset of these commands violation of this
     * specification. 501 Syntax error in command or arguments. In order to
     * provide future extensions, commands that are specified in this not
     * accepting arguments (DATA, RSET, QUIT) SHOULD return message if arguments
     * are supplied in the absence of EHLO- advertised extensions. 421 Service
     * shutting down and closing transmission channel
     * 
     * CONNECTION ESTABLISHMENT S: 220 E: 554
     * 
     * EHLO or HELO S: 250 E: 504, 550
     * 
     * MAIL S: 250 E: 552, 451, 452, 550, 553, 503
     * 
     * RCPT S: 250, 251 (but see section 3.4 for discussion of 251 and 551) E:
     * 550, 551, 552, 553, 450, 451, 452, 503, 550
     * 
     * DATA I: 354 -> data -> S: 250 E: 552, 554, 451, 452 E: 451, 554, 503
     * 
     * RSET S: 250
     * 
     * VRFY S: 250, 251, 252 E: 550, 551, 553, 502, 504
     * 
     * EXPN S: 250, 252 E: 550, 500, 502, 504
     * 
     * HELP S: 211, 214 E: 502, 504
     * 
     * NOOP S: 250
     * 
     * QUIT S: 221
     */
}
