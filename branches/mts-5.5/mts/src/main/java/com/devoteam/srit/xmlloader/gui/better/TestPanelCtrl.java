/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.devoteam.srit.xmlloader.gui.better;

import com.devoteam.srit.xmlloader.core.Test;
import com.devoteam.srit.xmlloader.core.Testcase;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.util.LinkedList;
import javax.swing.JPanel;

/**
 *
 * @author Gwenhael
 */
public class TestPanelCtrl {
    private TestPanelView _view;
    private Test _test;
    private LinkedList<TestcaseLineCtrl> _testcaseLineCtrls;
    
    public TestPanelCtrl(TestPanelView view, Test test){
        _view = view;
        _test = test;
        _testcaseLineCtrls = new LinkedList<TestcaseLineCtrl>();
        
        int i = 0;
        // create and add a TestcaseLineView/Ctrl per testcase
        for(Testcase testcase:test.getChildren()){
            TestcaseLineView testcaseLineView = new TestcaseLineView();
            TestcaseLineCtrl testcaseLineCtrl = new TestcaseLineCtrl(testcaseLineView, testcase);
            _testcaseLineCtrls.add(testcaseLineCtrl);
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.gridy = i;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.weightx = 1;
            
            _view.add(testcaseLineView, constraints);
            
            if(i%2 == 0){
                testcaseLineView.setBackground(testcaseLineView.getBackground().brighter());
            }
            
            i++;
        }
        
        // create the panel for vertical padding
        JPanel jPanelPadding = new JPanel();
        jPanelPadding.setMinimumSize(new Dimension(0,0));
        jPanelPadding.setPreferredSize(new Dimension(0,0));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = i;
        constraints.fill = GridBagConstraints.VERTICAL;
        constraints.weighty = 1;
        _view.add(jPanelPadding, constraints);
    }
    
    public void setAllTestLineSelected(boolean selected){
        for(TestcaseLineCtrl testcaseLineCtrl:_testcaseLineCtrls){
            testcaseLineCtrl.setTestcaseSelected(selected);
        }
    }
}
