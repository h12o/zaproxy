/*
 *
 * Paros and its related class files.
 * 
 * Paros is an HTTP/HTTPS proxy for assessing web application security.
 * Copyright (C) 2003-2004 Chinotec Technologies Company
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Clarified Artistic License
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Clarified Artistic License for more details.
 * 
 * You should have received a copy of the Clarified Artistic License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
// ZAP: 2012/01/12 Reflected the rename of the class ExtensionPopupMenu to
// ExtensionPopupMenuItem.
// ZAP: 2012/04/23 Added @Override annotation to all appropriate methods and 
// removed unnecessary cast.
// ZAP: 2012/07/29 Issue 43: Cleaned up access to ExtensionHistory UI

package org.parosproxy.paros.view;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.ExtensionPopupMenuItem;
import org.parosproxy.paros.extension.history.ExtensionHistory;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.SiteMap;
import org.parosproxy.paros.model.SiteNode;

public class PopupDeleteMenu extends ExtensionPopupMenuItem {

	private static final long serialVersionUID = 2282358266003940700L;

	private Component invoker = null;
    
	/**
	 * This method initializes 
	 * 
	 */
	public PopupDeleteMenu() {
		super();
		initialize();
	}
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setText(Constant.messages.getString("sites.delete.popup"));	// ZAP: i18n
        this.addActionListener(new java.awt.event.ActionListener() { 

        	@Override
        	public void actionPerformed(java.awt.event.ActionEvent e) {    
        	    if (invoker.getName().equals("treeSite")) {
        	        JTree tree = (JTree) invoker;
                    TreePath[] paths = tree.getSelectionPaths();
                    SiteMap map = (SiteMap) tree.getModel();
                    for (int i=0; i<paths.length;i++) {
                        SiteNode node = (SiteNode) paths[i].getLastPathComponent(); 
                        delete(map, node);
                    }
                }
 
        	    
        	}
        });

			
	}
	
	
    @Override
    public boolean isEnableForComponent(Component invoker) {
        if (invoker.getName() != null && invoker.getName().equals("treeSite")) {
            this.invoker = invoker;
            // ZAP: prevents a NullPointerException when the treeSite doesn't have a node selected and a popup menu option (Delete/Purge) is selected
            JTree tree = (JTree)invoker;
            if (tree.isSelectionEmpty() || ((TreeNode)tree.getModel().getRoot()).getChildCount() == 0) {
                this.setEnabled(false);
            } else {
                this.setEnabled(true);
            }
            return true;
        } else {
            this.invoker = null;
            return false;
        }

    }
    
   public static void delete(SiteMap map, SiteNode node) {
        SiteNode child = null;
        synchronized(map) {
            while (node.getChildCount() > 0) {
                try {
                    child = (SiteNode) node.getChildAt(0);
                    delete(map, child);
                } catch (Exception e) {}
            }
            
            if (node.isRoot()) {
                return;
            }

            // delete reference in node
            
            ExtensionHistory ext = (ExtensionHistory) Control.getSingleton().getExtensionLoader().getExtension(ExtensionHistory.NAME);
            ext.removeFromHistoryList(node.getHistoryReference());

            // delete past reference in node
            while (node.getPastHistoryReference().size() > 0) {
                HistoryReference ref = node.getPastHistoryReference().get(0);
                ext.removeFromHistoryList(ref);
                node.getPastHistoryReference().remove(0);
            }
            
            map.removeNodeFromParent(node);
        }
    }

    
}
