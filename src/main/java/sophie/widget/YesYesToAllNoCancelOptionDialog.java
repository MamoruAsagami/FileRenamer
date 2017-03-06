package sophie.widget;

import java.awt.Component;
import java.awt.HeadlessException;

import javax.swing.JOptionPane;

public class YesYesToAllNoCancelOptionDialog {
	public static final int YES_OPTION = 1;
	public static final int YES_TO_ALL_OPTION = 2;
	public static final int NO_OPTION = 4;
	public static final int NO_TO_ALL_OPTION = 8;
	public static final int CANCEL_OPTION = 16;
	public static final int ALL_OPTIONS = YES_OPTION|YES_TO_ALL_OPTION|NO_OPTION|NO_TO_ALL_OPTION|CANCEL_OPTION;
	public static final String YES = "Yes";
	public static final String YES_TO_ALL = "Yes to all";
	public static final String NO = "No";
	public static final String NO_TO_ALL = "No to all";
	public static final String CANCEL = "Cancel";
	
	private String[] options;
	private boolean yesToAll = false;
	private boolean noToAll = false;
	private boolean cancelled = false;
	
	public YesYesToAllNoCancelOptionDialog(int options) {
		int n = options & ALL_OPTIONS;
		int count = 0 ;
		while (n != 0)  {
			count++ ;
			n &= (n - 1); // inverts the rightmost one bit.
		}
		int i = 0;
		this.options = new String[count];
		if((options & YES_OPTION) != 0) {
			this.options[i++] = YES;
		}
		if((options & YES_TO_ALL_OPTION) != 0) {
			this.options[i++] = YES_TO_ALL;
		}
		if((options & NO_OPTION) != 0) {
			this.options[i++] = NO;
		}
		if((options & NO_TO_ALL_OPTION) != 0) {
			this.options[i++] = NO_TO_ALL;
		}
		if((options & CANCEL_OPTION) != 0) {
			this.options[i++] = CANCEL;
		}
	}
	public YesYesToAllNoCancelOptionDialog() {
		this(YES_OPTION|YES_TO_ALL_OPTION|NO_OPTION|CANCEL_OPTION);
	}
	
	public int showOptionDialog(Component parentComponent, Object message, String title, int messageType) throws HeadlessException {
		int value;
		if(yesToAll) {
			value = JOptionPane.YES_OPTION;
		} else if(noToAll) {
			value = JOptionPane.NO_OPTION;
		} else if(cancelled) {
			value = JOptionPane.CANCEL_OPTION;
		} else {
			int index = JOptionPane.showOptionDialog(parentComponent, message, title, JOptionPane.DEFAULT_OPTION, messageType, null, options, options[0]);
			String option = options[index];
			if(option == YES) {
				value = JOptionPane.YES_OPTION;
			} else if(option == YES_TO_ALL) {
				value = JOptionPane.YES_OPTION;
				yesToAll = true;
			} else if(option == NO) {
				value = JOptionPane.NO_OPTION;
			} else if(option == NO_TO_ALL) {
				value = JOptionPane.NO_OPTION;
				noToAll = true;
			} else if(option == CANCEL) {
				value = JOptionPane.CANCEL_OPTION;
				cancelled = true;
			} else  {
				throw new IllegalStateException();
			}
		}
		return value;
	}
	
	public int showOptionDialog(Component parentComponent, Object message, String title) throws HeadlessException {
		return showOptionDialog(parentComponent, message, title, JOptionPane.QUESTION_MESSAGE);
	}
}
