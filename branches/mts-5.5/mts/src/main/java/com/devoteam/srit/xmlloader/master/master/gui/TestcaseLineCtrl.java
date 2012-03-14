/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.devoteam.srit.xmlloader.master.master.gui;

import com.devoteam.srit.xmlloader.core.RunnerState;
import com.devoteam.srit.xmlloader.core.Testcase;
import com.devoteam.srit.xmlloader.core.utils.notifications.NotificationListener;
import javax.swing.SwingUtilities;

/**
 *
 * @author Gwenhael
 */
public class TestcaseLineCtrl implements NotificationListener<RunnerState> {

    private TestcaseLineView _view;
    private TestcaseLineCtrl _this = this;
    private Testcase _testcase;

    public TestcaseLineCtrl(TestcaseLineView view, Testcase testcase) {
        _view = view;
        _testcase = testcase;
        
        _view.jCheckBoxSelected.setText(_testcase.getName());
        _view.jCheckBoxSelected.setSelected(_testcase.getState());
    }
    
    // this methods does the necessary modifications on swing components when receiving a state change from the testcase runner
    @Override
    public synchronized void notificationReceived(final RunnerState state) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                _view.jProgressBar.setValue(state._progression);
                _view.jProgressBar.setToolTipText(state.toPopupHTMLString());

                if (state.isFinished() && state.isFailed()) {
                    _view.jLabelIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/silk/cross.png"))); // NOI18N
                }
                else if (state.isFinished() && !state.isInterrupted()) {
                    _view.jLabelIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/silk/tick.png"))); // NOI18N
                }
                else if (!state.isFinished() && state.isFailed()) {
                    _view.jLabelIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/silk/hourglass_delete.png"))); // NOI18N
                }
                else if (state.isStarted()) {
                    _view.jLabelIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/silk/hourglass.png"))); // NOI18N
                }
                else{
                    _view.jLabelIcon.setIcon(null);
                }
            }
        });
    }
    
    public Testcase getTestcase(){
        return _testcase;
    }
}
