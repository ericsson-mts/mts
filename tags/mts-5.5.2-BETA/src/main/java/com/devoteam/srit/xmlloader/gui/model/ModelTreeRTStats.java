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

package com.devoteam.srit.xmlloader.gui.model;

import com.devoteam.srit.xmlloader.core.exception.ParsingException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent.Topic;
import com.devoteam.srit.xmlloader.core.newstats.StatCounter;
import com.devoteam.srit.xmlloader.core.newstats.StatCounterConfigManager;
import com.devoteam.srit.xmlloader.core.newstats.StatKey;
import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.report.CounterReportTemplate;
import com.devoteam.srit.xmlloader.core.report.SectionReportGenerator;
import com.devoteam.srit.xmlloader.core.report.derived.DerivedCounter;
import com.devoteam.srit.xmlloader.core.report.derived.StatCount;
import com.devoteam.srit.xmlloader.core.report.derived.StatFlow;
import com.devoteam.srit.xmlloader.core.report.derived.StatPercent;
import com.devoteam.srit.xmlloader.core.report.derived.StatText;
import com.devoteam.srit.xmlloader.core.report.derived.StatValue;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.gui.frames.JFrameRTStats;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import java.awt.event.MouseListener;
import java.awt.font.TextAttribute;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.Spring;
import javax.swing.SpringLayout;
import javax.swing.border.Border;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * TreeModel for Tree in Real-time stats window
 * @author bthou
 */
public class ModelTreeRTStats extends DefaultTreeModel {

    // StatPool we have to present in the tree
    private StatPool pool;
    // Flag for know if sections are already created
    private boolean sectionIsCreate = false;
    // List of all node sections
    private ArrayList<DefaultMutableTreeNode> nodesSectionsList;
    private HashMap<String, Color> colorMap;
    // Attribut to save the stat that is display like a graphic
    private StatKey statSelected;
    // Instance variable
    static private ModelTreeRTStats instance;

    // Constructor for this model of tree. Root node is given in parameter
    public ModelTreeRTStats(MutableTreeNode root) {
        // We call constructor of DefaultTreeModel
        super(root);

        // Take instance of this ModelTree
        instance = this;

        // We create the ArrayList
        nodesSectionsList = new ArrayList();

        initColorMap();

        // We generate the tree
        generateTree();
    }

    // Return current instance of ModelTreeRTStats
    static synchronized public ModelTreeRTStats instance() {
        return instance;
    }

    // Method for init (and choose) all colors
    public void initColorMap() {
        // Create the new hashMap
        colorMap = new HashMap<String, Color>();
        // Default background color
        colorMap.put("neutral", new java.awt.Color(255, 255, 255));
        // Color for head title of each column
        colorMap.put("columnTitle", new java.awt.Color(226, 231, 222));
        // Color for total row background
        colorMap.put("backgroundTotal", new java.awt.Color(248, 248, 248));
        // Color when we have mouse over a label we can click for explore
        colorMap.put("selectForExplore", new java.awt.Color(236, 236, 236));
        // Color when we have mouse over a label we can click for explore
        colorMap.put("selectPathForExplore", new java.awt.Color(255, 255, 255));
        // Color when we have mouse over a label we can click for display graph
        colorMap.put("selectForGraph", new java.awt.Color(255, 238, 238));
        // Color when a label is selected for display his graph
        colorMap.put("selectedForGraph", new java.awt.Color(255, 210, 210));
    }

    // Get a color with a string
    public Color getColorByString(String key) {
        return colorMap.get(key);
    }

    // Method for reset all stats
    synchronized public void resetStats() {
        if (instance() != null) {
            // We reset this tree
            resetTree();
        }
    }

    // Method for choose what stat is selected for graph display
    synchronized public void setStatSelected(StatKey statkey) {
        statSelected = statkey;
    }

    // Method for generate the Tree
    synchronized public void generateTree() {
        // We get a local instance of the StatPool
        pool = StatPool.getInstance();

        // Generation of all sections in the tree ("protocol", "transaction", "session", ..., "user")
        generateSection();
    }

    // Method for generate all sections in the tree
    synchronized public void generateSection() {
        // Catch sections from config file
        String reportListSections = Config.getConfigByName("tester.properties").getString("stats.LIST_SECTIONS", "");

        // Create a table where each case have the name of one section
        String[] listSections = reportListSections.split(",");

        // Index for nodesSectionsList
        int i = 0;

        // For each section
        for (String section : listSections) {
            // We convert the section to lower case
            section = section.toLowerCase();

            // If we have a space before the name of the section, we remove it
            if (section.charAt(0) == ' ') {
                section = section.substring(1, section.length());
            }

            // If section not already created
            if (!sectionIsCreate) {
                // Creation of the node
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(section);

                // We add the new node in the list
                nodesSectionsList.add(i, node);

                // Insertion of the node in the tree
                insertNode(nodesSectionsList.get(i), (DefaultMutableTreeNode) this.getRoot(), i);
            }
            try {
                // Generation of the branch with the StatKey and the section node that is the base of the branch
                if (i < nodesSectionsList.size()) {
                    generateBranch(new StatKey(section.trim().toLowerCase()), nodesSectionsList.get(i));
                }
            }
            catch (Exception ex) {
                // If fail we log it
                GlobalLogger.instance().getApplicationLogger().error(Topic.CORE, ex, "error RT Stats generate branch : ", nodesSectionsList.get(i));
            }

            i++;
        }
        sectionIsCreate = true;
    }

    // Method for generate a branch of the tree with the nodeParent and all StatKeys
    synchronized public void generateBranch(StatKey prefixKey, DefaultMutableTreeNode nodeParent) throws Exception {
        // String Tab of all descendent
        String[] descendentSelect = CounterReportTemplate.concat(prefixKey.getAllAttributes(), "^[^_].*");

        // Descendents list construction
        List<StatKey> descendentsList = pool.findMatchingKeyStrict(new StatKey(descendentSelect));

        // Sort descendent collection
        Collections.sort(descendentsList);

        // String for the name of the tree
        String descName;

        // counter for determinate index of the child in the parent node
        int childIndex = 0;

        // For each descendant
        for (StatKey descendent : descendentsList) {
            // We get the last part of the key
            descName = descendent.getLastAttribute();

            descName = descName.replace("<BR>", "+");

            // We create a new node with the name of the last attribute
            DefaultMutableTreeNode nodeChild = new DefaultMutableTreeNode(descName);

            // We insert this new node in the parent node
            insertNode(getNode(nodeChild, nodeParent), nodeParent, childIndex);

            // Recursive call with the new child
            generateBranch(descendent, getNode(nodeChild, nodeParent));

            // Increment counter
            childIndex++;
        }
    }

    // New version of the InsertNodeInto for only add node which isn't already in the tree
    synchronized public void insertNode(DefaultMutableTreeNode nodeChild, DefaultMutableTreeNode nodeParent, int position) {
        // Flag for know if the nodeChild already exist in the nodeParent
        boolean alreadyExist = false;

        // We get all children of node parent
        Enumeration<DefaultMutableTreeNode> allChildren = nodeParent.children();

        // For each child
        while (allChildren.hasMoreElements()) {
            // If this child match (with the name) with the node searching
            if (allChildren.nextElement().toString().equals(nodeChild.toString())) {
                // That's mean the node already exist
                alreadyExist = true;
            }
        }

        // If the node don't exist
        if (!alreadyExist) {
            // We really insert the node in the tree in the good position
            insertNodeInto(nodeChild, nodeParent, position);

            // Code for expand only this new node

            // We get the path of the child
            TreeNode[] childTreeNode = nodeChild.getPath();

            // We prepare a new TreeNode tab for the path of the parent (=the child path without the last element)
            TreeNode[] parentTreeNode = new TreeNode[childTreeNode.length - 1];

            // If tree is ready
            if (sectionIsCreate) {
                // Increment variable to progress in parent path
                int i = 0;

                // For each element of childTreeNode
                for (TreeNode currentNode : childTreeNode) {
                    // if we are not on the last element
                    if (i < childTreeNode.length - 1) {
                        // parentTreeNode received the currentNode for the child path
                        parentTreeNode[i] = currentNode;
                    }
                    // Increment i
                    i++;
                }
            }
        }
        // Else we not insert again the node
    }

    // Method for get a node of the tree. Parameter 'nodeChild' : node we are searching for - 'nodeParent' : node root where we search
    synchronized public DefaultMutableTreeNode getNode(DefaultMutableTreeNode nodeChild, DefaultMutableTreeNode nodeParent) {
        // Initialisation of the node returned
        DefaultMutableTreeNode node = null;

        // We get all children of node parent
        Enumeration<DefaultMutableTreeNode> allChildren = nodeParent.children();

        // For each child
        while (allChildren.hasMoreElements()) {
            // Get the current child
            node = allChildren.nextElement();

            // If this child match (with the name) with the node searching
            if (node.toString().equals(nodeChild.toString())) {
                // We return the node
                return node;
            }
        }

        // Else (that's mean we don't found) we return the new node child
        node = nodeChild;
        return node;
    }

    // Method for regenerate the tree
    synchronized public void reGenerateTree() {
        // Generate the new tree
        generateTree();
    }

    // Method for reset the tree
    synchronized public void resetTree() {
        // We say we don't have generate sections yet
        sectionIsCreate = false;

        // We remove all child under the root
        ((DefaultMutableTreeNode) getRoot()).removeAllChildren();

        // We reprepare the tree for an other fillage
        generateTree();

        // We reload the view
        reload();

        // We clear the panel
        JFrameRTStats.instance().clearPanel();

        // By default we select the first node in the tree
        JFrameRTStats.instance().setSelectedNode(0);

    }

    // Method for display all counter of the family (descendentSelect) in the panel
    synchronized public void displayCounters(StatKey prefixKey, javax.swing.JPanel panel) throws Exception {
        // We clear the panel
        panel.removeAll();

        // We generate the railWay in relartion with th StatKey displayed
        generateRailWay(prefixKey);

        // We get all template possible for the StatKey prefixKey
        List<CounterReportTemplate> templateList = StatCounterConfigManager.getInstance().getTemplateList(prefixKey);

        // We create a String tab with all descendent of the prefixKey
        String[] descendentSelect = CounterReportTemplate.concat(prefixKey.getAllAttributes(), "^[^_].*");

        // We create a list with all StatKey matching with descendent of prefixKey
        List<StatKey> descendentsList = pool.findMatchingKeyStrict(new StatKey(descendentSelect));
        Collections.sort(descendentsList);

        // We create a new panel for insert a table for short stats
        JPanel jPanelShort = new JPanel();

        // Color of this panel
        jPanelShort.setBackground(ModelTreeRTStats.instance().getColorByString("neutral"));

        // We choose a SpringLayout to have disposition like in a tab
        //jPanelShort.setLayout(new GridLayout(descendentsList.size()+2,0,10,5));
        jPanelShort.setLayout(new SpringLayout());

        // Layout alignment to the left
        jPanelShort.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        jPanelShort.setAlignmentY(java.awt.Component.TOP_ALIGNMENT);

        // We fill jPanelShort with all titles
        if (!templateList.isEmpty()) {
            fillWithSummaryTitles(jPanelShort, prefixKey, templateList);
        }

        // For each descendents
        for (StatKey descendent : descendentsList) {
            // We create a new line with all data
            fillWithData(jPanelShort, descendent, templateList, true);
        }

        // If there is any information in this section
        if (!templateList.isEmpty()) {
            // We add the line with total
            fillWithTotal(jPanelShort, prefixKey, templateList);
        }

        // We create a grid with elements in jPanelShort
        makeCompactGrid(jPanelShort, descendentsList.size() + 2, templateList.size() + 1, 3, 3, 5, 5);

        // We add this short panel to the main panel
        panel.add(jPanelShort);

        // We refresh the panel
        panel.updateUI();
    }

    // Method for fill a panel with grid layout (gridPanel) with all title elements of a StatKey
    public void fillWithSummaryTitles(JPanel gridPanel, StatKey prefixKey, List<CounterReportTemplate> templateList) {
        // Panel for titles of all cols
        JPanel panelTmp = new JPanel();

        // Color of this panel
        panelTmp.setBackground(ModelTreeRTStats.instance().getColorByString("columnTitle"));

        // We want a text alignment on the left
        panelTmp.setLayout(new javax.swing.BoxLayout(panelTmp, javax.swing.BoxLayout.X_AXIS));

        // First column
        panelTmp.add(new JLabel("<html>Summary</html>"));

        // We add this panel to the main panel for shorts stats
        gridPanel.add(panelTmp);

        // For each template
        for (CounterReportTemplate template : templateList) {
            // Others columns
            JPanel panelTmp2 = new JPanel();

            // Color of this panel
            panelTmp2.setBackground(ModelTreeRTStats.instance().getColorByString("columnTitle"));

            // We want a text alignment on the left
            panelTmp2.setLayout(new javax.swing.BoxLayout(panelTmp2, javax.swing.BoxLayout.X_AXIS));

            // We add to this panel short descr of each template
            panelTmp2.add(new JLabel("<html>" + template.summary + "</html>"));

            // We add a toolTip on head section
            panelTmp2.setToolTipText(template.name);

            // We add this panel to the main panel for shorts stats
            gridPanel.add(panelTmp2);
        }
    }

    // Method for fill a panel with grid layout (gridPanel) with datas of the StatKey descendent
    public void fillWithData(JPanel gridPanel, StatKey descendent, List<CounterReportTemplate> templateList, boolean addLinks) {
        // We add the last part of the key as a title of the line
        JPanel panelTitle = new JPanel();

        // Color of this panel
        panelTitle.setBackground(ModelTreeRTStats.instance().getColorByString("neutral"));

        // We want a text alignment on the left
        panelTitle.setLayout(new javax.swing.BoxLayout(panelTitle, javax.swing.BoxLayout.X_AXIS));

        // We create a new Label for title of this row
        // and we take the last part of the key as title of the row

        JLabel labelTitle = new JLabel("<html><u>" + descendent.getLastAttribute() + "</u></html>");

        if (addLinks) {
            labelTitle.setForeground(Color.blue);
        }

        // We add the label to the main panel
        panelTitle.add(labelTitle);

        if (addLinks) {
            // We add a tootip to the title panel
            panelTitle.setToolTipText("Click to explore");

            // We add a mouse listener to this panel if we want to click on it
            panelTitle.addMouseListener(new MouseListener() {

                java.awt.Color lastColor;

                public void mouseClicked(MouseEvent e) {
                }

                public void mousePressed(MouseEvent e) {
                    // We take the path actually selected
                    TreePath pathDisplayed = JFrameRTStats.instance().getSelectedPath();

                    // We get the JLabel of the JPanel we want to explore
                    Component firstElement = ((Container) e.getComponent()).getComponent(0);

                    // We check if this is really a JPanel
                    if (firstElement instanceof JLabel) {
                        // We expand the part of the tree we want to explore
                        JFrameRTStats.instance().expandFromPath(pathDisplayed);

                        // We get the last node of the selected path
                        TreeNode node = (TreeNode) pathDisplayed.getLastPathComponent();

                        // If this node have any child
                        if (node.getChildCount() >= 0) {
                            // We get the content of the label we are click for
                            String labelContent = ((JLabel) firstElement).getText();

                            // We remove <html> and </html>
                            labelContent = labelContent.replace("<html>", "");
                            labelContent = labelContent.replace("</html>", "");
                            labelContent = labelContent.replace("<u>", "");
                            labelContent = labelContent.replace("</u>", "");
                            labelContent = labelContent.replace("<BR>", "+");

                            // For each child
                            for (Enumeration child = node.children(); child.hasMoreElements();) {
                                // Take the child
                                TreeNode n = (TreeNode) child.nextElement();

                                // If this child is equal to to node we want to explore
                                if (n.toString().equals(labelContent)) {
                                    // We create the new path we will selected
                                    TreePath path = pathDisplayed.pathByAddingChild(n);

                                    // We select this new path
                                    JFrameRTStats.instance().setSelectedNode(path);
                                }
                            }
                        }
                    }
                }

                public void mouseReleased(MouseEvent e) {
                }

                public void mouseEntered(MouseEvent e) {
                    // We save the last background color
                    lastColor = e.getComponent().getBackground();

                    // We change the background color when mouse enter in the panel
                    e.getComponent().setBackground(ModelTreeRTStats.instance().getColorByString("selectForExplore"));
                }

                public void mouseExited(MouseEvent e) {
                    // We restore background with the color saved
                    e.getComponent().setBackground(lastColor);
                }
            });
        }

        // We add this element on the main panel for shorts stats
        gridPanel.add(panelTitle);

        // For each template
        for (CounterReportTemplate template : templateList) {
            // We get (if possible) the counter matching the current StatKey and current template
            DerivedCounter derivedCounter = null;
            try {
                derivedCounter = template.getDerivedCounter(pool, descendent);
            }
            catch (ParsingException ex) {
                GlobalLogger.instance().getApplicationLogger().error(Topic.CORE, ex, "error RT Stats , fill With Data , can't get derived counter ");
            }
            if (derivedCounter != null) {
                // We get short version of counter informations
                JPanel panelShortStats = derivedCounter.generateShortRTStats();

                // We choose the color
                panelShortStats.setBackground(ModelTreeRTStats.instance().getColorByString("neutral"));

                // If we display this counter with a graphic
                if (derivedCounter.id.equals(statSelected)) {
                    // We change the color of the background (now it's pink)
                    panelShortStats.setBackground(ModelTreeRTStats.instance().getColorByString("selectedForGraph"));

                    // We clean the bottom panel
                    JFrameRTStats.instance().getJPanelBottom().removeAll();

                    // We add the panel with the long report and graph
                    JFrameRTStats.instance().getJPanelBottom().add(derivedCounter.generateLongRTStats());

                    // We create a blank line
                    JFrameRTStats.instance().getJPanelBottom().add(new JLabel("<html><br/></html>"));

                    // We add graphics
                    JFrameRTStats.instance().getJPanelBottom().add(derivedCounter.getChartPanel());
                }

                // We add this element on the main panel for shorts stats
                gridPanel.add(panelShortStats);
            }
            else {
                // This mean there is no information for this counter
                // So we create a new panel for display "-"
                JPanel panelEmpty = new JPanel();

                // Color of this panel
                panelEmpty.setBackground(ModelTreeRTStats.instance().getColorByString("neutral"));

                // We want a text alignment on the left
                panelEmpty.setLayout(new javax.swing.BoxLayout(panelEmpty, javax.swing.BoxLayout.X_AXIS));

                // We add a label "-"
                panelEmpty.add(new JLabel(" - "));

                // We add this element on the main panel for shorts stats
                gridPanel.add(panelEmpty);
            }
        }
    }

    // Method for fill a panel with grid layout (gridPanel) with all total  of the StatKey prefixKey
    public void fillWithTotal(JPanel gridPanel, StatKey prefixKey, List<CounterReportTemplate> templateList) {
        // Creation of a border in top of panel
        Border totalBorder = BorderFactory.createMatteBorder(2, 0, 0, 0, ModelTreeRTStats.instance().getColorByString("columnTitle"));

        // We create a new panel for total row
        JPanel panelTotal = new JPanel();
        panelTotal.setLayout(new javax.swing.BoxLayout(panelTotal, javax.swing.BoxLayout.X_AXIS));
        panelTotal.setBackground(ModelTreeRTStats.instance().getColorByString("backgroundTotal"));
        panelTotal.setBorder(totalBorder);

        // We create a new panel for the title of the row
        JPanel panelTotalTitle = new JPanel();
        panelTotalTitle.setLayout(new javax.swing.BoxLayout(panelTotalTitle, javax.swing.BoxLayout.X_AXIS));
        panelTotalTitle.setBackground(ModelTreeRTStats.instance().getColorByString("backgroundTotal"));

        // Title of the row
        panelTotalTitle.add(new JLabel("TOTAL"));

        panelTotal.add(panelTotalTitle);

        // We add  this panel to the global panel for short stats (gridLayout)
        gridPanel.add(panelTotal);

        for (CounterReportTemplate template : templateList) {
            // We get (if possible) the counter matching the current StatKey and current template
            DerivedCounter derivedCounter = null;
            try {
                derivedCounter = template.getDerivedCounter(pool, prefixKey);
            }
            catch (ParsingException ex) {
                GlobalLogger.instance().getApplicationLogger().error(Topic.CORE, ex, "error RT Stats , fill With Total , can't get derived counter ");
            }
            if (derivedCounter != null) {
                // We get the short panel of this stat
                JPanel panelShortTotal = derivedCounter.generateShortRTStats();

                // We want a white color for this panel background
                panelShortTotal.setBackground(ModelTreeRTStats.instance().getColorByString("backgroundTotal"));

                // We specify the border style of this panel
                panelShortTotal.setBorder(totalBorder);

                if (derivedCounter.id.equals(statSelected)) {
                    // We change the color of the background (now it's pink)
                    panelShortTotal.setBackground(ModelTreeRTStats.instance().getColorByString("selectedForGraph"));

                    // We clean the bottom panel
                    JFrameRTStats.instance().getJPanelBottom().removeAll();

                    // We add the panel with the long report and graph
                    JFrameRTStats.instance().getJPanelBottom().add(derivedCounter.generateLongRTStats());

                    // We create a blank line
                    JFrameRTStats.instance().getJPanelBottom().add(new JLabel("<html><br/></html>"));

                    // We add graphics
                    JFrameRTStats.instance().getJPanelBottom().add(derivedCounter.getChartPanel());
                }

                // We add this element on the main panel for shorts stats
                gridPanel.add(panelShortTotal);
            }
            else {
                JPanel panelEmpty = new JPanel();

                // Color of this panel
                panelEmpty.setBackground(ModelTreeRTStats.instance().getColorByString("backgroundTotal"));

                // We specify the border style of this panel
                panelEmpty.setBorder(totalBorder);

                // We want a text alignment on the left
                panelEmpty.setLayout(new javax.swing.BoxLayout(panelEmpty, javax.swing.BoxLayout.X_AXIS));

                // We add a label "-"
                panelEmpty.add(new JLabel(" - "));

                // We add this element on the main panel for shorts stats
                gridPanel.add(panelEmpty);
            }
        }
    }

    public void generateRailWay(StatKey prefixKey) {
        // Title of the panel
        String[] path = prefixKey.getAllAttributes();

        // We create a new panel for create the railway
        JPanel railWay = new JPanel();
        railWay.setLayout(new BoxLayout(railWay, javax.swing.BoxLayout.X_AXIS));
        railWay.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        railWay.setAlignmentY(java.awt.Component.TOP_ALIGNMENT);
        //railWay.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // for know number of the current element in rail way
        int k = 0;

        // For each element of the current selected path
        for (String pathElement : path) {
            JLabel chevron = new JLabel(" > ");
            chevron.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
            chevron.setAlignmentY(java.awt.Component.TOP_ALIGNMENT);
            railWay.add(chevron);

            pathElement = pathElement.replace("<BR>", "+");

            // creation of an underline font
            Map<TextAttribute, Object> map = new Hashtable<TextAttribute, Object>();
            map.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
            Font underlineFont = new Font(map);

            // creation of text for the railway
            JLabel linkRail = new JLabel(pathElement);
            linkRail.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
            linkRail.setAlignmentY(java.awt.Component.TOP_ALIGNMENT);

            // if this is not the last element of the railway, this should be a link
            if (k < path.length - 1) {
                // we color it in blue
                linkRail.setForeground(Color.blue);

                // we underline it
                linkRail.setFont(underlineFont);
            }

            // Essential if we want to see background color
            linkRail.setOpaque(true);

            // Tool tip for link rail
            linkRail.setToolTipText("Click to explore");

            // if this is not the last element of the railway
            if (k < path.length - 1) {
                // we can add a mouse listener for interactives actions
                linkRail.addMouseListener(new MouseListener() {

                    Color lastColor;

                    public void mouseClicked(MouseEvent e) {
                        // We take the path actually selected
                        TreePath pathDisplayed = JFrameRTStats.instance().getSelectedPath();

                        // We get the content of the label we are click for
                        String labelContent = ((JLabel) e.getComponent()).getText();

                        // We get all parents of the path actually displayed
                        Object[] allParents = pathDisplayed.getPath();

                        // We prepare a new object TreePath for create new path to selected
                        TreePath pathToSelect = pathDisplayed;

                        // We browse the allParents tab from right to left
                        int i = allParents.length - 1;
                        while (i >= 0) {
                            // Current parent
                            Object parent = allParents[i];

                            // If this current parent matching with label selected
                            if (parent.toString().equals(labelContent)) {
                                // We select this new node
                                JFrameRTStats.instance().setSelectedNode(pathToSelect);
                            }

                            // Else we update the path to select without the last parent
                            pathToSelect = pathToSelect.getParentPath();

                            i--;
                        }

                    }

                    public void mousePressed(MouseEvent e) {
                        //throw new UnsupportedOperationException("Not supported yet.");
                    }

                    public void mouseReleased(MouseEvent e) {
                        //throw new UnsupportedOperationException("Not supported yet.");
                    }

                    public void mouseEntered(MouseEvent e) {
                        // We save the last background color
                        lastColor = e.getComponent().getBackground();

                        // We change the background color when mouse enter in the panel
                        e.getComponent().setBackground(ModelTreeRTStats.instance().getColorByString("selectPathForExplore"));
                    }

                    public void mouseExited(MouseEvent e) {
                        // We restore background with the color saved
                        e.getComponent().setBackground(lastColor);
                    }
                });
            }
            railWay.add(linkRail);
            k++;
        }

        // We add the rail way to the panel jPanelPath
        JFrameRTStats.getinstance().getJPanelPath().removeAll();
        JFrameRTStats.getinstance().getJPanelPath().add(railWay);
        JFrameRTStats.getinstance().getJPanelPath().updateUI();

    }

    // Method for return constraints about a cell (specified by row and col numbers)
    private static SpringLayout.Constraints getConstraintsForCell(int row, int col, Container parent, int cols) {
        SpringLayout layout = (SpringLayout) parent.getLayout();
        Component c = parent.getComponent(row * cols + col);
        return layout.getConstraints(c);
    }

    // Method for structure a container as a grid
    // int rows : number of rows
    // int cols : number of cols
    // int initialX : position of the origine point on X axis
    // int initialY : position of the origine point on Y axis
    // int xPad : horizontal padding between each cell
    // int yPad : vertical padding between each cell
    public static void makeCompactGrid(Container parent,
            int rows, int cols,
            int initialX, int initialY,
            int xPad, int yPad) {
        SpringLayout layout;
        try {
            layout = (SpringLayout) parent.getLayout();
        }
        catch (ClassCastException exc) {
            GlobalLogger.instance().getApplicationLogger().error(Topic.CORE, exc, "error RT Stats , make Compact Grid , can't get Parent layout");
            return;
        }

        //Align all cells in each column and make them the same width.
        Spring x = Spring.constant(initialX);
        for (int c = 0; c < cols; c++) {
            Spring width = Spring.constant(0);
            for (int r = 0; r < rows; r++) {
                width = Spring.max(width,
                        getConstraintsForCell(r, c, parent, cols).
                        getWidth());
            }
            for (int r = 0; r < rows; r++) {
                SpringLayout.Constraints constraints =
                        getConstraintsForCell(r, c, parent, cols);
                constraints.setX(x);
                constraints.setWidth(width);
            }
            x = Spring.sum(x, Spring.sum(width, Spring.constant(xPad)));
        }

        //Align all cells in each row and make them the same height.
        Spring y = Spring.constant(initialY);
        for (int r = 0; r < rows; r++) {
            Spring height = Spring.constant(0);
            for (int c = 0; c < cols; c++) {
                height = Spring.max(height,
                        getConstraintsForCell(r, c, parent, cols).
                        getHeight());
            }
            for (int c = 0; c < cols; c++) {
                SpringLayout.Constraints constraints =
                        getConstraintsForCell(r, c, parent, cols);
                constraints.setY(y);
                constraints.setHeight(height);
            }
            y = Spring.sum(y, Spring.sum(height, Spring.constant(yPad)));
        }

        //Set the parent's size.
        SpringLayout.Constraints pCons = layout.getConstraints(parent);
        pCons.setConstraint(SpringLayout.SOUTH, y);
        pCons.setConstraint(SpringLayout.EAST, x);
    }
}
