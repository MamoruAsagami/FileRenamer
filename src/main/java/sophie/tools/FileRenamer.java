package sophie.tools;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import sophie.io.FileUtilities;
import sophie.util.FileNameExpression;
import sophie.widget.ErrorMessageDialog;
import sophie.widget.JFileTextField;

@SuppressWarnings("serial")
public class FileRenamer extends JPanel {
	static String VERSION = "1.10";
	static String TITLE = FileRenamer.class.getSimpleName() + " " + VERSION;
    
    JFileTextField directoryTextField;
    JTextField inclusionTextField;
    JTextField exclusionTextField;
    JCheckBox hiddenCheckbox;
    JTextArea left;
    JTextArea right;
    JSplitPane splitPane;

    public FileRenamer() throws IOException {
        JButton directoryButton = new JButton("Directory");
		setLayout(new BorderLayout());
		JPanel northPanel = new JPanel();
		add(northPanel, BorderLayout.NORTH);
		northPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(2, 2, 2, 2);
		gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
		northPanel.add(directoryButton, gbc);
		gbc.gridx++; gbc.anchor = GridBagConstraints.CENTER;
		gbc.gridwidth = 3;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		northPanel.add(directoryTextField = new JFileTextField(), gbc);
		gbc.gridx += gbc.gridwidth;
		gbc.gridwidth = 1;
		JButton refreshButton = new JButton("Refresh");
		northPanel.add(refreshButton, gbc);
		//
		gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;;
		northPanel.add(new JLabel("Inclusion pattern"), gbc);
		gbc.gridx++; gbc.anchor = GridBagConstraints.CENTER; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 0.5;
		northPanel.add(inclusionTextField = new JTextField(20), gbc);
		gbc.gridx++; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
		northPanel.add(new JLabel("Exclusion pattern"), gbc);
		gbc.gridx++; gbc.anchor = GridBagConstraints.CENTER; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 0.5;
		northPanel.add(exclusionTextField = new JTextField(20), gbc);
		gbc.weightx = 0;
		gbc.gridx++;
		JButton renameButton = new JButton("Rename");
		northPanel.add(renameButton, gbc);
        //
		gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
		gbc.gridx = 1; gbc.gridy++;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        hiddenCheckbox = new JCheckBox("Hidden", false);
        northPanel.add(hiddenCheckbox, gbc);
        //      
    	left = new JTextArea("\n\n\n\n\n\n\n\n\n\n");
    	left.setForeground(Color.blue);
    	left.setEditable(false);
    	right = new JTextArea();
    	splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, captionedComponent("Original names", left), captionedComponent("New names (Edit to rename)", right));
    	splitPane.setResizeWeight(0.5);
    	JScrollPane scrollPane = new JScrollPane(splitPane, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
    	add(scrollPane);
		//
		directoryButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		        browseDirectory();
		    }
		});
		
		DocumentListener documentListener = new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {
				resizeSplitPane();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				resizeSplitPane();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				resizeSplitPane();
			}};
		left.getDocument().addDocumentListener(documentListener);
		right.getDocument().addDocumentListener(documentListener);
		refreshButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		        refreshNames();
		    }
		});
		
		renameButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		        rename();
		    }
		});
		directoryTextField.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent arg0) {
				directoryChanged();
			}
			public void insertUpdate(DocumentEvent arg0) {
				directoryChanged();
			}
			public void removeUpdate(DocumentEvent arg0) {
				directoryChanged();
			}
	    });

    }
    
    JComponent captionedComponent(String caption, JComponent component) {
    	JPanel panel = new JPanel();
    	panel.setLayout(new BorderLayout());
    	JLabel captionLabel = new JLabel(caption);
    	//captionLabel.setBorder(new EtchedBorder());
    	//captionLabel.setBorder(new LineBorder(Color.black));
    	panel.add(captionLabel, BorderLayout.NORTH);
    	panel.add(component);
    	return panel;
    }
    
    void resizeSplitPane() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				splitPane.setSize(splitPane.getPreferredSize());
				updateUI();
			}});
    }

	void browseDirectory() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Choose directory");
        int returnVal = chooser.showOpenDialog(this);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
           directoryTextField.setText(chooser.getSelectedFile().getPath().replace('\\', '/'));
        }
	}
	
    void directoryChanged() {
    	File directory = new File(directoryTextField.getText().trim());
    	if(directory.exists()) {
    		refreshNames();
    	}
    }
    
	private File baseFile() {
		String baseFileName = directoryTextField.getText();
		if(baseFileName.length() != 0) {
			File baseFile = new File(directoryTextField.getText());
			if(baseFile.exists()) {
				if(baseFile.isDirectory()) {
					return baseFile;
				} else {
					JOptionPane.showMessageDialog(this, "Directory expected: " + baseFileName, TITLE, JOptionPane.ERROR_MESSAGE);
				}
			} else {
				JOptionPane.showMessageDialog(this, "File not found: " + baseFileName, TITLE, JOptionPane.ERROR_MESSAGE);
			}
		} else {
			JOptionPane.showMessageDialog(this, "Directory not specified.", TITLE, JOptionPane.ERROR_MESSAGE);
		}
		return null;
	}

    private String[] parseFileNames(String s) {
    	ArrayList<String> nameList = new ArrayList<String>();
    	char[] ca = s.toCharArray();
    	int index = 0;
    	for(;index < ca.length; index++) {
    		char c = ca[index];
    		if(c != ' ' && c != '\t') {
    			if(c == '"') {
    				StringBuilder sb = new StringBuilder();
    				index++;
    				for(;index < ca.length; index++) {
    					c = ca[index];
    					if(c == '"')
    						break;
    					else if(c == '\\') {
    						index++;
    						if(index < ca.length) {
    							sb.append(ca[index]);
    						}
    					} else {
    						sb.append(c);
    					}
    				}
    				if(sb.length() != 0) {
    					nameList.add(sb.toString());
    				}
    			} else {
    				StringBuilder sb = new StringBuilder();
    				sb.append(c);
    				index++;
    				for(;index < ca.length; index++) {
    					c = ca[index];
    					if(c == ' ' || c == '\t')
    						break;
    					sb.append(c);
    				}
    				nameList.add(sb.toString());
    			}
    		}
    	}
    	return nameList.toArray(new String[nameList.size()]);
    }
    
    private FileNameExpression[] parseFilterStrings(String s) {
        ArrayList<FileNameExpression> list = new ArrayList<FileNameExpression>();
        if(s != null) {
	        for(String e: parseFileNames(s)) {
	        	if(e.length() != 0)
	        		list.add(new FileNameExpression(e));
	        }
        }
        return (list.size() == 0)? null: list.toArray(new FileNameExpression[list.size()]);
    }

    void refreshNames() {
    	try {
    		File baseFile = baseFile();
    		if(baseFile != null) {
    			FileNameExpression[] inclusions = parseFilterStrings(inclusionTextField.getText());
    			FileNameExpression[] exclusions = parseFilterStrings(exclusionTextField.getText());
    			boolean includeHidden = hiddenCheckbox.isSelected();
    			ArrayList<String> nameList = new ArrayList<String>();
    			for(String name: baseFile.list()) {
    				if(includeHidden || !new File(baseFile, name).isHidden()) {
    					boolean include = true;
    					if(include) {
    						if(inclusions != null) {
    							include = false;
    							for(int j=0; j<inclusions.length; j++) {
    								if(inclusions[j].matches(name)) {
    									include = true;
    									break;
    								}
    							}
    						}
    						if(include &&  exclusions != null) {
    							for(int j=0; j<exclusions.length; j++) {
    								if(exclusions[j].matches(name)) {
    									include = false;
    									break;
    								}
    							}
    						}
    						if(include) {
    							nameList.add(name);
    						}
    					}
    				}
    			}
    			String[] names = nameList.toArray(new String[nameList.size()]);
    			Arrays.sort(names,  Collator.getInstance());
    			left.setText("");
    			right.setText("");
    			for(String name: names) {
    				left.append(name);
    				left.append("\n");
    				right.append(name);
    				right.append("\n");
    			}
    		}
    	} catch(Exception ex) {
    		ErrorMessageDialog.showMessageDialog(this, ex, TITLE);
    	}
    }
    
    String[] names(String text) {
    	String[] sa = text.split("\n");
    	int n;
    	for(n = sa.length - 1; n >= 0; n--) {
    		if(sa[n].trim().length() != 0)
    			break;
    	}
    	n++;
    	String[] names = new String[n];
    	for(int i = 0; i < n; i++) {
    		names[i] = sa[i].trim();
    	}
    	return names;
    }
    
    void rename() {
    	try {
    		File baseFile = baseFile();
    		if(baseFile != null) {
    			HashSet<String> nameSet = new HashSet<String>();
    			for(String name: baseFile.list()) {
    				nameSet.add(name);
    			}
    			String[] leftNames = names(left.getText());
    			String[] rightNames = names(right.getText());
    			if(leftNames.length != rightNames.length) {
    				throw new IllegalArgumentException("The numbers of files are different.");
    			}
    			HashMap<String, String> map = new HashMap<String, String>();
    			for(int i = 0; i < leftNames.length; i++) {
    				String leftName = leftNames[i];
    				String rightName = rightNames[i];
    				if(leftName.length() == 0) {
    					throw new IllegalArgumentException("Null name at line: " + (i+1));
    				}
    				if(rightName.length() == 0) {
    					throw new IllegalArgumentException("Can't change '" + leftName + "' to null.");
    				}
    				if(!nameSet.contains(leftName)) {
    					throw new IllegalArgumentException("No such a name: " + leftName);
    				}
    				if(!rightName.equals(leftName)) {
    					nameSet.remove(leftName);
    					map.put(leftName, rightName);
    				}
    			}
    			for(String name: map.values()) {
    				if(nameSet.contains(name)) {
    					throw new IllegalArgumentException("The name exists: " + name);
    				}
    				nameSet.add(name);
    			}
    			rename(baseFile, map);
    		}
    	} catch(Exception ex) {
    		ErrorMessageDialog.showMessageDialog(this, ex, TITLE);
    	}
    }
    
    static void renameBack(File baseFile, Stack<String> renameLog) throws IOException {
    	for(;!renameLog.isEmpty();) {
    		String fromName = renameLog.pop();
    		String toName = renameLog.pop();
    		File fromFile = new File(baseFile, fromName);
    		File toFile = new File(baseFile, toName);
    		if(!fromFile.renameTo(toFile)) {
    			throw new IOException("Failed to rename back from '" + fromName + "' to '" + toName + "'.");
    		}
    	}
    }
    
    static void rename(File baseFile, Stack<String> renameStack, Stack<String> renameLog) throws IOException {
    	String toName = renameStack.pop();
    	for(;!renameStack.isEmpty();) {
    		String fromName = renameStack.pop();
    		File fromFile = new File(baseFile, fromName);
    		File toFile = new File(baseFile, toName);
    		if(!fromFile.renameTo(toFile)) {
    			renameBack(baseFile, renameLog);
    			throw new IOException("Failed to rename from '" + fromName + "' to '" + toName + "'.");
    		}
    		renameLog.add(fromName);
    		renameLog.add(toName);
    		toName = fromName;
    	}
    }
    
    static void rename(File baseFile, HashMap<String, String> map) throws IOException {
    	Stack<String> renameLog = new Stack<String>();
    	for(; !map.isEmpty();) {
    		Stack<String> renameStack = new Stack<String>();
    		boolean cyclic = false;
    		String firstName = map.keySet().iterator().next();
    		String fromName = firstName;
    		renameStack.push(fromName);
    		for(;;) {
    			String toName = map.get(fromName);
        		map.remove(fromName);
    			if(toName == null)
    				break;
    			renameStack.push(toName);
    			if(toName.equals(firstName)) {
    				cyclic = true;
    				break;
    			}
    			fromName = toName;
    		}
    		if(cyclic) {
    			File destFile = FileUtilities.createNewFile(new File(baseFile, firstName));
    			String name = destFile.getName();
    			renameStack.push(name);
    			renameStack.set(0, name);
    		}
    		rename(baseFile, renameStack, renameLog);
    	}
    }
    
	public static void main(String args[]) throws IOException {
	  JFrame f = new JFrame();
	  f.getContentPane().add(new FileRenamer());
	  f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      f.setTitle(TITLE);
      f.pack();
      f.setVisible(true);
    }
}
