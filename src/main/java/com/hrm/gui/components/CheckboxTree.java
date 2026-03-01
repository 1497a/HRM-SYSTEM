package com.hrm.gui.components;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * A generic JTree that supports checkboxes at each node.
 * Uses CheckboxTreeNode as the standard node type.
 */
public class CheckboxTree extends JTree {

    public CheckboxTree(TreeNode root) {
        super(root);
        
        // Custom cell renderer
        setCellRenderer(new CheckboxTreeCellRenderer());
        
        // Handle mouse clicks manually for the checkboxes
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int selRow = getRowForLocation(e.getX(), e.getY());
                TreePath selPath = getPathForLocation(e.getX(), e.getY());
                if (selRow != -1 && selPath != null) {
                    Object node = selPath.getLastPathComponent();
                    if (node instanceof CheckboxTreeNode) {
                        CheckboxTreeNode cbNode = (CheckboxTreeNode) node;
                        // Toggle the selection state
                        cbNode.setSelected(!cbNode.isSelected());
                        
                        // Cascade the selection state down to children
                        cascadeDown(cbNode, cbNode.isSelected());
                        // Cascade up to parents to update partial selection states
                        cascadeUp(cbNode);
                        
                        repaint();
                    }
                }
            }
        });
        
        setShowsRootHandles(true);
    }
    
    private void cascadeDown(CheckboxTreeNode node, boolean selected) {
        for (int i = 0; i < node.getChildCount(); i++) {
            CheckboxTreeNode child = (CheckboxTreeNode) node.getChildAt(i);
            child.setSelected(selected);
            cascadeDown(child, selected);
        }
    }
    
    private void cascadeUp(CheckboxTreeNode node) {
        CheckboxTreeNode parent = (CheckboxTreeNode) node.getParent();
        if (parent != null) {
            boolean allSelected = true;
            boolean anySelected = false;
            
            for (int i = 0; i < parent.getChildCount(); i++) {
                CheckboxTreeNode child = (CheckboxTreeNode) parent.getChildAt(i);
                if (child.isSelected()) {
                    anySelected = true;
                } else {
                    allSelected = false;
                }
            }
            
            parent.setSelected(allSelected);
            parent.setPartialSelection(anySelected && !allSelected);
            cascadeUp(parent);
        }
    }
    
    /**
     * Helper to get all selected leaves (used for getting the final selected permissions)
     */
    public List<CheckboxTreeNode> getSelectedLeaves() {
        List<CheckboxTreeNode> selectedLeaves = new ArrayList<>();
        CheckboxTreeNode root = (CheckboxTreeNode) getModel().getRoot();
        findSelectedLeaves(root, selectedLeaves);
        return selectedLeaves;
    }
    
    private void findSelectedLeaves(CheckboxTreeNode node, List<CheckboxTreeNode> result) {
        if (node.isLeaf()) {
            if (node.isSelected()) {
                result.add(node);
            }
        } else {
            for (int i = 0; i < node.getChildCount(); i++) {
                findSelectedLeaves((CheckboxTreeNode) node.getChildAt(i), result);
            }
        }
    }

    /**
     * Tree Node class that holds selection state and a user object.
     */
    public static class CheckboxTreeNode extends DefaultMutableTreeNode {
        private boolean isSelected;
        private boolean isPartialSelection;
        private String userObjectCode; // for storing e.g. permission code

        public CheckboxTreeNode(Object userObject) {
            super(userObject);
            this.isSelected = false;
            this.isPartialSelection = false;
        }

        public CheckboxTreeNode(Object userObject, String userObjectCode) {
            super(userObject);
            this.isSelected = false;
            this.isPartialSelection = false;
            this.userObjectCode = userObjectCode;
        }

        public boolean isSelected() {
            return isSelected;
        }

        public void setSelected(boolean selected) {
            isSelected = selected;
            isPartialSelection = false;
        }
        
        public boolean isPartialSelection() {
            return isPartialSelection;
        }
        
        public void setPartialSelection(boolean partial) {
            isPartialSelection = partial;
            if (partial) {
                isSelected = false;
            }
        }

        public String getUserObjectCode() {
            return userObjectCode;
        }
    }

    /**
     * Renderer to draw a checkbox next to the node label.
     */
    private static class CheckboxTreeCellRenderer extends JPanel implements TreeCellRenderer {
        private final TristateCheckBox checkBox;
        private final JLabel label;

        public CheckboxTreeCellRenderer() {
            setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
            setOpaque(false);
            
            checkBox = new TristateCheckBox();
            label = new JLabel();
            label.setFont(UIManager.getFont("Tree.font"));
            
            add(checkBox);
            add(label);
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            
            if (value instanceof CheckboxTreeNode) {
                CheckboxTreeNode node = (CheckboxTreeNode) value;
                
                if (node.isPartialSelection()) {
                    checkBox.setState(TristateCheckBox.State.INDETERMINATE);
                } else if (node.isSelected()) {
                    checkBox.setState(TristateCheckBox.State.SELECTED);
                } else {
                    checkBox.setState(TristateCheckBox.State.UNSELECTED);
                }
                
                label.setText(node.getUserObject() != null ? node.getUserObject().toString() : "");
            }
            
            return this;
        }
    }

    /**
     * A simple tristate checkbox implementation or wrapper for drawing
     */
    private static class TristateCheckBox extends JCheckBox {
        public enum State { UNSELECTED, SELECTED, INDETERMINATE }
        private State state = State.UNSELECTED;

        private final Icon indeterminateIcon = new IndeterminateIcon();

        public TristateCheckBox() {
            super();
            setOpaque(false);
        }

        public void setState(State state) {
            this.state = state;
            if (state == State.SELECTED) {
                super.setSelected(true);
            } else {
                super.setSelected(false);
            }
        }
        
        public State getState() {
            return state;
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (state == State.INDETERMINATE) {
                // If using default look and feel icons, we need to artificially draw a square for indeterminate
                Icon icon = UIManager.getIcon("CheckBox.icon");
                if (icon != null) {
                    Insets i = getInsets();
                    indeterminateIcon.paintIcon(this, g, i.left, i.top);
                }
            }
        }

        private class IndeterminateIcon implements Icon {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                int w = getIconWidth();
                int h = getIconHeight();
                g2d.setColor(UIManager.getColor("CheckBox.background"));
                g2d.fillRect(x + 2, y + 2, w - 4, h - 4);
                
                g2d.setColor(UIManager.getColor("CheckBox.foreground"));
                g2d.fillRect(x + 4, y + w/2 - 1, w - 8, 3);
                g2d.dispose();
            }

            @Override
            public int getIconWidth() { return 13; }
            @Override
            public int getIconHeight() { return 13; }
        }
    }
}
