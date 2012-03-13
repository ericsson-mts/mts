/*
 * InputHandler.java
 *
 * Created on 5 décembre 2007, 10:11
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
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
