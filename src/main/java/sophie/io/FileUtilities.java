package sophie.io;

import java.awt.Component;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import sophie.widget.YesYesToAllNoCancelOptionDialog;

public class FileUtilities {
	public static String toString(File file) {
		return file.toString().replace('\\', '/');
	}
	
	private static String toString(File file, String tag) {
		String name = toString(file);
		return (tag == null || tag.length() == 0)? name: tag + "(" + name + ")";
	}
	
	public static boolean setLastModified(File file, long lastModifiedTime) {
	 	long sleepTime = 200;
	 	for(int i = 0; i < 4; i++) {
	 		if(file.setLastModified(lastModifiedTime))
	 			return true;
	 		try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
			}
			sleepTime *= 2;
	 	}
	 	return false;
	}
	
	public static boolean verify(File file, boolean isDirectory, boolean isFile, String tag, Component c, String title) {
		if(!file.exists()) {
			JOptionPane.showMessageDialog(c, toString(file, tag) + " does not exist.", title, JOptionPane.ERROR_MESSAGE);
			return false;
		} else if(isDirectory && !file.isDirectory()) {
			JOptionPane.showMessageDialog(c, toString(file, tag) + " is not a directory.", title, JOptionPane.ERROR_MESSAGE);
			return false;
		} else if(isFile && !file.isFile()) {
			JOptionPane.showMessageDialog(c, toString(file, tag) + " is not a file.", title, JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
	
    public static boolean canWrite(File file, Component c, String title) {
        if(file.exists()) {
        	if(file.isDirectory()) {
        		JOptionPane.showMessageDialog(c, toString(file) + " is not a file.", title, JOptionPane.ERROR_MESSAGE);
        		return false;
        	} else {
        		int option = JOptionPane.showConfirmDialog(c, toString(file) + " exists.\nOverwrite it?", title, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        		return option == JOptionPane.YES_OPTION;
        	}
        } else {
            return true;
        }
    }

    public static boolean canWrite(File file, Component c, String title, YesYesToAllNoCancelOptionDialog yesYesToAllNoCancelOptionDialog) {
    	if(yesYesToAllNoCancelOptionDialog == null) {
    		return canWrite(file, c, title);
    	} else {
    		if(file.exists()) {
    			int option = yesYesToAllNoCancelOptionDialog.showOptionDialog(c, toString(file) + " exist.\nOverwrite it?", title, JOptionPane.WARNING_MESSAGE);
    			return option == JOptionPane.YES_OPTION;
    		} else {
    			return true;
    		}
    	}
    }

    public static File chooseFileForWrite(String message, Component c, String title) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setDialogTitle(message);
        int returnVal = chooser.showOpenDialog(c);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            File file = new File(chooser.getSelectedFile().getPath());
            return canWrite(file, c, title)? file: null;
        } else {
            return null;
        }
    }
    
    public static void makeParentDirectories(File file) {
    	File parentFile = file.getParentFile();
    	if(parentFile!= null && !parentFile.exists()) {
    		parentFile.mkdirs();
    	}
    }
    
    public static void delete(File file) throws IOException {
        if(file.isDirectory()) {
            File[] files = file.listFiles();
            if(files != null) {
            	for(int i=0; i<files.length; i++) {
            		delete(files[i]);
            	}
            } else {
            	throw new IOException("Can't look into " + FileUtilities.toString(file) + ".");
            }
        }
        if(!file.delete()) {
        	throw new IOException("Failed to delete " + FileUtilities.toString(file) + ".");
        }
    }
    
    public static void copyFile(File from, File to) throws IOException {
        InputStream in = null;
        OutputStream out = null;
        byte[] buffer = new byte[4096];
        try {
            in = new BufferedInputStream(new FileInputStream(from));
            out = new BufferedOutputStream(new FileOutputStream(to));
            int n;
            while((n = in.read(buffer)) > 0) {
                out.write(buffer, 0, n);
            }
            out.close();
            out = null;
            in.close();
            in = null;
            to.setLastModified(from.lastModified());
        } finally { // Close all files if remain opened
        	try {
	            if(in != null) {
	                in.close();
	            }
        	} finally {
	            if(out != null)
	                out.close();
        	}
        }
    }

    public static void moveFile(File from, File to) throws IOException {
    	if(!to.equals(from)) {
    		if(!from.renameTo(to)) {
    			// Renaming fail.
    			makeParentDirectories(to);
    			if(!from.renameTo(to)) {
    				copyFile(from, to);
    				from.delete();
    			}
    		}
    	}
    }
    
    public static boolean equalContents(File file1, File file2) throws java.io.IOException {
        boolean value = false;
        if(file1.length() == file2.length()) {
            BufferedInputStream in1 = new BufferedInputStream(new FileInputStream(file1));
            BufferedInputStream in2 = new BufferedInputStream(new FileInputStream(file2));
            int c1, c2;
            while((c1 = in1.read()) != -1) {
                c2 = in2.read();
                if(c1 != c2)
                    break;
            }
            if(c1 == -1)
                value = true;
            in1.close();
            in2.close();
        }
        return value;
    }
    
    public static File createNewFile(File file, boolean atomic) throws IOException {
		File parentFile = file.getParentFile();
		String name = file.getName();
		int index = name.lastIndexOf('.');
		String prefix;
		final String suffix;
		if(index >= 0) {
			prefix = name.substring(0, index);
			suffix = name.substring(index);
		} else {
			prefix = name;
			suffix = "";
		}
		int index1 = prefix.lastIndexOf('~');
		if(index1 >= 0) {
			if(Pattern.matches("~\\(\\d+\\)", prefix.substring(index1))) {
				prefix = prefix.substring(0, index1);
			}
		}
		file = new File(parentFile, prefix + suffix);
		makeParentDirectories(file);
		for(int i = 1; !(atomic? file.createNewFile(): !file.exists()); i++) {
			file = new File(parentFile, prefix + "~(" + i + ")" + suffix);
		}
		return file;
    }
    
    public static File createNewFile(File file) throws IOException {
    	return createNewFile(file, false);
    }
    
    public static void cleanupTempFiles(final String prefix, final String suffix) {
    	// Delete phantom temporary files.
    	File tempDirectory = new File(System.getProperty("java.io.tmpdir"));
    	File[] tempFiles = tempDirectory.listFiles(new FilenameFilter() {
    		public boolean accept(File dir, String name) {
    			return name.startsWith(prefix) && name.endsWith(suffix);
    		}
    	});
    	for(File file: tempFiles) {
    		long lifeTime = System.currentTimeMillis() - file.lastModified();
    		if(lifeTime > 7*24*60*60*1000) { // 7 days
    			file.delete();
    		}
    	}
    }
}
