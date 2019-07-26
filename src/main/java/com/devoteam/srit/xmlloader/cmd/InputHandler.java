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

package com.devoteam.srit.xmlloader.cmd;

/**
 *
 * @author gpasquiers
 */
public class InputHandler implements Runnable
{
    private TextTester textTester;

    /** Creates a new instance of InputHandler */
    public InputHandler(TextTester textTester)
    {
        this.textTester = textTester;
        printMenu();
    }

    public void run()
    {
        try
        {

	        // should be ran as a daemon thread
	        while(true)
	        {
                char aChar = (char) System.in.read();

                if(Character.isLetterOrDigit(aChar))
                {
                    switch(aChar)
                    {
                        case 's':
                        case 'S':
                            System.out.println("Stop test");
                            this.textTester.stop();
                            break;
                        case 'k':
                        case 'K':
                            System.out.println("Kill test");
                            this.textTester.release();
                            break;
                        case 'r':
                        case 'R':
                            System.out.println("Report test");
                            try
                            {
                                this.textTester.getRunner().getTest().report_generate();
                            }
                            catch(Exception e)
                            {
                                e.printStackTrace();
                            }
                            break;
                        default:
                            System.out.println("Unknown command");
                            printMenu();
                            break;
                    }
                }
	        }

        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    static public void printMenu()
    {
        System.out.print( "Available commands: (S)top, (K)ill, (R)eport (ENTER to validate):\n" );
    }
}
