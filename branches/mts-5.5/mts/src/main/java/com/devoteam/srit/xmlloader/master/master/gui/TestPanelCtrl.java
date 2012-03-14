/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.devoteam.srit.xmlloader.master.master.gui;

import com.devoteam.srit.xmlloader.master.master.utils.DataMaster;
import com.devoteam.srit.xmlloader.master.master.utils.DataTest;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JPanel;

/**
 *
 * @author Gwenhael
 */
public class TestPanelCtrl {

    private TestPanelView _view;
    private LinkedList<TestLineCtrl> _jPanelTestLineCtrls;
    private DataMaster _dataMaster;

    public TestPanelCtrl(TestPanelView view, DataMaster dataMaster) {
        _view = view;
        _dataMaster = dataMaster;
        _jPanelTestLineCtrls = new LinkedList();
        
        int i = 0;
        // create and add a TestcaseLineView/Ctrl per testcase
        for (DataTest dataTest : _dataMaster.getDataTests()) {
            TestLineView jPanelTestLineView = new TestLineView();
            TestLineCtrl jPanelTestCtrl = new TestLineCtrl(jPanelTestLineView, dataTest);

            _jPanelTestLineCtrls.add(jPanelTestCtrl);
            
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.gridy = i;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.weightx = 1;

            _view.add(jPanelTestLineView, constraints);

            if ((i/2) % 2 == 0) {
                jPanelTestLineView.setBackground(jPanelTestLineView.getBackground().brighter());
            }

            i+=2;
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
    
    public void free(){
        for(TestLineCtrl jPanelTestLineCtrl:_jPanelTestLineCtrls){
            jPanelTestLineCtrl.free();
        }
    }
    
    public List<TestLineCtrl> getTestLineCtrls(){
        return Collections.unmodifiableList(_jPanelTestLineCtrls);
    }
}
