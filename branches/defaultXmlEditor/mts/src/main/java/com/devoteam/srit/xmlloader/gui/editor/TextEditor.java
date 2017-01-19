package com.devoteam.srit.xmlloader.gui.editor;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

/**
 * The default editor class.
 * @author Mickaël MAUDOIGT
 * 
 * 
 */
public class TextEditor extends JFrame {

	private List<RTextScrollPane> listEditor = new ArrayList<RTextScrollPane>();

	/**
	 * init the code editor
	 * @param windowName
	 */
	public void init(String windowName) {

		JPanel cp = new JPanel(new BorderLayout());
		JTabbedPane tabbed = new JTabbedPane();
		// a tab for each file
		if(!listEditor.isEmpty()){
			for (RTextScrollPane sp : listEditor) {
				tabbed.add(sp);
			}
		} else {
			tabbed.add(new RTextScrollPane(new RSyntaxTextArea()));
		}
		cp.add(tabbed, BorderLayout.NORTH);

		JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JButton btnSave = new JButton("Save");
		JButton btnQuit = new JButton("Quit");
		btnPanel.add(btnSave);
		btnPanel.add(btnQuit);

		btnSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean check = true;
				for(RTextScrollPane file : listEditor){
					try {
						RTextArea ta = file.getTextArea();
						FileWriter fw = new FileWriter(ta.getName());
						ta.write(fw);
						fw.close();
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(null, "Error, files didn't save", "Error", JOptionPane.ERROR_MESSAGE);
						check = false;
						e1.printStackTrace();
					}
				}
				if(check){
					JOptionPane.showMessageDialog(null, "Saved", "Saved", JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});

		btnQuit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int rep = JOptionPane.showConfirmDialog(null, "Do you really want to quit ?", "Quit",
						JOptionPane.YES_NO_OPTION);
				if (rep == JOptionPane.YES_OPTION) {
					dispose();
				}
			}
		});

		cp.add(btnPanel, BorderLayout.SOUTH);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowListener() {
			
			@Override
			public void windowOpened(WindowEvent arg0) {}
			
			@Override
			public void windowIconified(WindowEvent arg0) {}
			
			@Override
			public void windowDeiconified(WindowEvent arg0) {}
			
			@Override
			public void windowDeactivated(WindowEvent arg0) {}
			
			@Override
			public void windowClosing(WindowEvent arg0) {
				int rep = JOptionPane.showConfirmDialog(null, "Do you really want to quit ?", "Quit",
						JOptionPane.YES_NO_OPTION);
				if (rep == JOptionPane.YES_OPTION) {
					dispose();
				}
			}
			
			@Override
			public void windowClosed(WindowEvent arg0) {}
			
			@Override
			public void windowActivated(WindowEvent arg0) {}
		});
		setContentPane(cp);
		setTitle(windowName);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	/**
	 * Add files of scenario into the same window editor
	 * 
	 * @param uri
	 */
	public void addFileToEditor(URI uri) {
		try {

			RSyntaxTextArea textArea = new RSyntaxTextArea(30, 100);
			textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
			FileReader fw = new FileReader(uri.getPath());
			textArea.read(fw, uri.getPath());
			RTextScrollPane sp = new RTextScrollPane(textArea);
			String[] ret = uri.getPath().split("/");
			sp.setName(ret[ret.length - 1]);
			textArea.setName(uri.getPath());
			listEditor.add(sp);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "File loading error : " + e.getMessage(),
					"File loading error", JOptionPane.ERROR_MESSAGE);
		}

	}

	/**
	 * Main for tests
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// Start all Swing applications on the EDT.
		try {
			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (Exception ex) {
		}
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				TextEditor te = new TextEditor();
				try {
					te.addFileToEditor(new URI(
							"F:/workspace/MTS/mts-ericsson/target/mts/tutorial/http/100_http_post_client_server/client.xml"));
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
				try {
					te.addFileToEditor(new URI(
							"F:/workspace/MTS/mts-ericsson/target/mts/tutorial/http/100_http_post_client_server/serveur.xml"));
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
				te.init("test");
			}
		});
	}
	

}