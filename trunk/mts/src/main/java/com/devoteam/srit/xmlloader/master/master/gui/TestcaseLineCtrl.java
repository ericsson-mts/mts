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
