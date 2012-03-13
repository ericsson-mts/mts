/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.core.newstats;

import com.devoteam.srit.xmlloader.core.RunnerState;
import com.devoteam.srit.xmlloader.core.TestRunner;
import com.devoteam.srit.xmlloader.core.TestRunnerSingle;
import com.devoteam.srit.xmlloader.core.utils.notifications.Notification;
import com.devoteam.srit.xmlloader.core.utils.notifications.NotificationListener;
import java.util.HashSet;

/**
 *
 * @author gpasquiers
 */
public class StatPoolReset
{
    //static part
    private static final StatPoolReset instance = new StatPoolReset();

    public static StatPoolReset instance()
    {
        return instance;
    }


    //instanciated part
    private HashSet<TestRunner> runners;
    private boolean _enabled;

    public StatPoolReset()
    {
        _enabled =  false;
        runners = new HashSet();
    }

    synchronized public void trigger(final TestRunner testRunner)
    {
        if(testRunner instanceof TestRunnerSingle) return;

        if(runners.contains(testRunner)) return;

        if(_enabled && runners.size() ==0) StatPool.getInstance().reset();

        runners.add(testRunner);

        testRunner.addListener(new NotificationListener<Notification<String, RunnerState>>(){
            public void notificationReceived(Notification<String, RunnerState> notification)
            {
                if(notification.getData().isFinished())
                {
                    synchronized(instance)
                    {
                        instance.runners.remove(testRunner);
                    }
                }
            }
        });
    }

    synchronized public void enable(){
        _enabled = true;
    }

    synchronized public void disable(){
        _enabled = false;
    }

}
