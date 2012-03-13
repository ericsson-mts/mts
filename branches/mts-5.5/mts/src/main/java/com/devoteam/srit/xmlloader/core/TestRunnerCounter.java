/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.core;

/**
 *
 * @author gpasquiers
 */
public class TestRunnerCounter {
    // singleton part
    static private TestRunnerCounter instance = null;

    static public synchronized TestRunnerCounter instance(){
        if(null == instance){
            instance = new TestRunnerCounter();
        }

        return instance;
    }
    // end of singleton part

    private int runningTests;

    private TestRunnerCounter(){
        runningTests = 0;
    }

    public synchronized void runningTestsIncreased(){

        runningTests++;
    }

    public synchronized void runningTestsDecreased(){

        runningTests--;
    }

    public synchronized int runningTestsCount(){
        return runningTests;
    }
}
