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

package com.devoteam.srit.xmlloader.gui.frames;

import com.devoteam.srit.xmlloader.core.Tester;
import com.devoteam.srit.xmlloader.core.ThreadRunner;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent.Topic;
import com.devoteam.srit.xmlloader.core.newstats.RTStatsTimer;
import com.devoteam.srit.xmlloader.core.newstats.StatKey;
import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.gui.model.ModelTreeRTStats;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;


/**
 * Main frame for display real-times stats
 * @author bthou
 */
public class JFrameRTStats extends javax.swing.JFrame {

    // Instance variable
    static private JFrameRTStats instance;

    // Return current instance of JFrameRTStats
    // and create a new instance if this frame doesn't exist
    static synchronized public JFrameRTStats instance()
    {
        if(null == instance)
        {
            instance = new JFrameRTStats();
        }
        return instance;
    }

    // Return current instance of JFrameRTStats
    static synchronized public JFrameRTStats getinstance()
    {
        return instance;
    }


    /** Creates new form JFrameRTStats */
    public JFrameRTStats(){        
        initComponents();

        // Root could not be visible
        jTree1.setRootVisible(false);

        // We add an action listener on the tree
        jTree1.addTreeSelectionListener(new TreeSelectionListener() {

            // Listener for detect if we have something selected in the jTree
            public void valueChanged(TreeSelectionEvent e) {
              // We create a new thread for update this panel
              java.awt.EventQueue.invokeLater(new ThreadRunner() {
                public void run() {
                    // We remove all graphics
                    getJPanelBottom().removeAll();
                    // We update the bottom panel
                    getJPanelBottom().updateUI();
                    // We want to update the right panel
                    updatePanel();
                }
              });

            }
        });

        // Creation of a timer for refresh the tree each n seconds
        RTStatsTimer.instance();

        // By default, we select the first element of the tree
        jTree1.setSelectionRow(0);

        // By default, we expand all the tree
        //expandTree();

        // We change the delay for apparition of tooltips
        ToolTipManager.sharedInstance().setInitialDelay(100);

        // We forbid the user to select more than one node in the jTree
        jTree1.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

    }

    // Method for expand all the tree on the left part of the window
    public void expandTree()
    {
        expandAll(jTree1);
    }

    // Method for expand a part of the tree (the path of the part is given in parameter)
    public void expandFromPath(TreePath path)
    {
        jTree1.expandPath(path);
    }

    // Methods for expand all the tree
    public static void expandAll(JTree tree)
    {
        // Catch root of the tree
        TreeNode root = (TreeNode)tree.getModel().getRoot();
        
        // Traverse Tree from Root
        expandAll(tree, new TreePath(root));
    }

    // Annexe recursive methods for expand all the tree
    public static void expandAll(JTree tree, TreePath parent)
    {
        TreeNode node = (TreeNode)parent.getLastPathComponent();
        if(node.getChildCount() >= 0)
        {
            for(Enumeration e=node.children(); e.hasMoreElements();)
            {
                TreeNode n = (TreeNode)e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                expandAll(tree, path);
            }
        }
        tree.expandPath(parent);
    }

    // Method for update the panel on the right
    public void updatePanel()
    {
    	// add the static stat counters : specially for the editable parameters and test sections
    	StatPool.getInstance().addStatsStaticTestParameters(Tester.getInstance().getTest());
    	
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) jTree1.getLastSelectedPathComponent();
        /* if nothing is selected, function return */
        if (node == null) {
            return;
        }
        List<String> nodeFamily = new ArrayList();
        // Declaration of a node current
        DefaultMutableTreeNode nodeCurrent;
        // nodeCurrent is node selected by user
        nodeCurrent = node;
        // We add the node selected in a List of String
        nodeFamily.add(node.toString());
        // While current node is not a "section-node" as "Protocol","Transaction","Session",...,"User"
        while (!((DefaultMutableTreeNode) nodeCurrent.getParent()).isRoot()) {
            // We add the current node in the family
            nodeFamily.add(nodeCurrent.getParent().toString());
            // New node current is now the parent
            nodeCurrent = (DefaultMutableTreeNode) nodeCurrent.getParent();
        }
        // conversion of a liste of node in a String[]
        String[] nodeFamilyString = new String[nodeFamily.size()];
        int i = nodeFamily.size() - 1;
        for (String element : nodeFamily) {
            nodeFamilyString[i] = element.replace("+", "<BR>");
            i--;
        }
        try {
            // Now we call getCounters function to display all counter of the family selected in jPanel
            ((ModelTreeRTStats) jTree1.getModel()).displayCounters(new StatKey(nodeFamilyString), jPanel1);
        } catch (Exception ex) {
            GlobalLogger.instance().getApplicationLogger().error(Topic.CORE, ex, "error RT Stats , update Panel");
          }
    }

    public void clearPanel(){
        // We clean the panel
        jPanel1.removeAll();
        jPanelPadding.removeAll();

        // We update the display for this panel
        jPanel1.updateUI();
    }

    // Return true if the path passed in parameter is expanded in jTree1
    public boolean isExpanded(TreePath path)
    {
        return jTree1.isExpanded(path);
    }

    // Method to get the panel in the bottom of the right part of the window
    public JPanel getJPanelBottom()
    {
        return jPanel2;
    }

    // Method to get the panel where we want display the path
    public JPanel getJPanelPath()
    {
        return jPanelPath;
    }


    // Method for set the selected node in jtree1
    public void setSelectedNode(TreePath path)
    {
        jTree1.setSelectionPath(path);
    }

    // Method for set the selected node in jtree1
    public void setSelectedNode(int num)
    {
        jTree1.setSelectionRow(num);
    }

    // Method for get path of the current selected node in jtree1
    public TreePath getSelectedPath()
    {
        return jTree1.getSelectionPath();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTree1 = new javax.swing.JTree();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanelPadding = new javax.swing.JPanel();
        jPanelContaine = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jButtonForceExpir = new javax.swing.JButton();
        jButtonClose = new javax.swing.JButton();
        jPanelPath = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("M.T.S. : real-time statistics");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        jSplitPane1.setDividerLocation(150);
        jSplitPane1.setDoubleBuffered(true);

        jScrollPane1.setDoubleBuffered(true);

        jTree1.setModel(new com.devoteam.srit.xmlloader.gui.model.ModelTreeRTStats(new DefaultMutableTreeNode("Root"){}));
        jTree1.setDoubleBuffered(true);
        jScrollPane1.setViewportView(jTree1);

        jSplitPane1.setLeftComponent(jScrollPane1);

        jScrollPane2.setAlignmentX(0.0F);
        jScrollPane2.setAlignmentY(0.0F);
        jScrollPane2.setAutoscrolls(true);
        jScrollPane2.setDoubleBuffered(true);

        jPanel.setBackground(new java.awt.Color(255, 255, 255));
        jPanel.setAlignmentY(0.0F);
        jPanel.setAutoscrolls(true);
        jPanel.setLayout(new javax.swing.BoxLayout(jPanel, javax.swing.BoxLayout.Y_AXIS));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.Y_AXIS));
        jPanel.add(jPanel1);

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 10, 10, 10));
        jPanel2.setAlignmentY(0.0F);
        jPanel2.setMaximumSize(new java.awt.Dimension(500, 0));
        jPanel2.setLayout(new javax.swing.BoxLayout(jPanel2, javax.swing.BoxLayout.Y_AXIS));
        jPanel.add(jPanel2);

        jPanelPadding.setBackground(new java.awt.Color(255, 255, 255));
        jPanelPadding.setAlignmentX(0.0F);
        jPanelPadding.setAlignmentY(0.0F);
        jPanelPadding.setMinimumSize(new java.awt.Dimension(0, 0));
        jPanelPadding.setPreferredSize(new java.awt.Dimension(0, 450));
        jPanelPadding.setLayout(new java.awt.GridBagLayout());

        jPanelContaine.setBackground(new java.awt.Color(255, 255, 255));
        jPanelContaine.setAlignmentX(0.0F);
        jPanelContaine.setMaximumSize(new java.awt.Dimension(1024, 768));
        jPanelContaine.setMinimumSize(new java.awt.Dimension(1024, 768));
        jPanelContaine.setPreferredSize(new java.awt.Dimension(1024, 768));
        jPanelContaine.setLayout(new javax.swing.BoxLayout(jPanelContaine, javax.swing.BoxLayout.X_AXIS));
        jPanelPadding.add(jPanelContaine, new java.awt.GridBagConstraints());

        jPanel.add(jPanelPadding);

        jScrollPane2.setViewportView(jPanel);

        jButtonForceExpir.setText("Force expiration");
        jButtonForceExpir.setToolTipText("Force expiration of sessions and transactions");
        jButtonForceExpir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonForceExpirActionPerformed(evt);
            }
        });

        jButtonClose.setText("Close");
        jButtonClose.setToolTipText("close real-time stats window");
        jButtonClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCloseActionPerformed(evt);
            }
        });

        jPanelPath.setLayout(new javax.swing.BoxLayout(jPanelPath, javax.swing.BoxLayout.X_AXIS));

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanelPath, javax.swing.GroupLayout.DEFAULT_SIZE, 290, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonForceExpir)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonClose)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanelPath, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 23, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButtonForceExpir)
                        .addComponent(jButtonClose, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 496, Short.MAX_VALUE)
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 306, Short.MAX_VALUE)
                .addGap(42, 42, 42))
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel3Layout.createSequentialGroup()
                    .addContainerGap(303, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jSplitPane1.setRightComponent(jPanel3);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 652, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        // We launch the timer
        RTStatsTimer.instance().unpause();

        // We force expiration of hash map
        StackFactory.cleanStackLists();
    }//GEN-LAST:event_formWindowOpened

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        // When the real-time stats window is close, we stop the RTStatsTimer
        if(RTStatsTimer.getInstance() != null)
        {
            RTStatsTimer.getInstance().pause();
        }
        instance = null;
        dispose();
    }//GEN-LAST:event_formWindowClosing

    private void jButtonCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCloseActionPerformed
        formWindowClosing(null);
    }//GEN-LAST:event_jButtonCloseActionPerformed

    private void jButtonForceExpirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonForceExpirActionPerformed
        // We force expiration of hash maps for "transaction" and "session" sections
        StackFactory.cleanStackLists();

        // We mark we should refresh stats with a flag on the RTStatsTimer
        RTStatsTimer.shouldRefresh();
    }//GEN-LAST:event_jButtonForceExpirActionPerformed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new JFrameRTStats().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonClose;
    private javax.swing.JButton jButtonForceExpir;
    private javax.swing.JPanel jPanel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanelContaine;
    private javax.swing.JPanel jPanelPadding;
    private javax.swing.JPanel jPanelPath;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTree jTree1;
    // End of variables declaration//GEN-END:variables

}
