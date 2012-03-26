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

import com.devoteam.srit.xmlloader.core.Testcase;
import com.devoteam.srit.xmlloader.master.master.utils.ControlerTest;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.util.LinkedList;
import javax.swing.JPanel;

/**
 *
 * @author Gwenhael
 */
public class TestcasePanelCtrl {
    private TestcasePanelView _view;
    private ControlerTest _controlerTest;
    private LinkedList<TestcaseLineCtrl> _jPanelTestcaseLineCtrls;
    private boolean _disposed;
    
    public TestcasePanelCtrl(TestcasePanelView view, ControlerTest controlerTest) {
        _view = view;
        _disposed = false;
        _controlerTest = controlerTest;
        _jPanelTestcaseLineCtrls = new LinkedList<TestcaseLineCtrl>();

        int i = 0;
        // create and add a TestcaseLineView/Ctrl per testcase
        for (Testcase testcase : controlerTest.getTest().getChildren()) {
            TestcaseLineView jPanelTestcaseLineView = new TestcaseLineView();
            TestcaseLineCtrl jPanelTestcaseLineCtrl = new TestcaseLineCtrl(jPanelTestcaseLineView, testcase);

            _jPanelTestcaseLineCtrls.add(jPanelTestcaseLineCtrl);
            _controlerTest.registerToCache(testcase.getName(), jPanelTestcaseLineCtrl);

            GridBagConstraints constraints = new GridBagConstraints();
            constraints.gridy = i;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.weightx = 1;


            _view.add(jPanelTestcaseLineView, constraints);

            if (i % 2 == 0) {
                jPanelTestcaseLineView.setBackground(jPanelTestcaseLineView.getBackground().brighter());
            }

            i++;
        }

        // create the panel for vertical padding
        JPanel jPanelPadding = new JPanel();
        jPanelPadding.setMinimumSize(new Dimension(0, 0));
        jPanelPadding.setPreferredSize(new Dimension(0, 0));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = i;
        constraints.fill = GridBagConstraints.VERTICAL;
        constraints.weighty = 1;
        _view.add(jPanelPadding, constraints);
    }

    public TestcasePanelView getView() {
        return _view;
    }

    public void dispose() {
        _disposed = true;
        for (TestcaseLineCtrl jPanelTestcaseLineCtrl : _jPanelTestcaseLineCtrls) {
            _controlerTest.unregisterFromCache(jPanelTestcaseLineCtrl.getTestcase().getName(), jPanelTestcaseLineCtrl);
        }
        _view.removeAll();
        _jPanelTestcaseLineCtrls.clear();
    }
    
    public boolean disposed() {
        return _disposed;
    }
    
}
