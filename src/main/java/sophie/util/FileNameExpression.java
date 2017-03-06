package sophie.util;
import java.util.regex.Pattern;

public class FileNameExpression {
	private Pattern pattern;

    public FileNameExpression(String pattern) {
        this.pattern = Pattern.compile(PathExpression.glob2Re(pattern));
    }

    public String toString() {
    	return pattern.pattern();
    }
    
    public boolean matches(String name) {
        return pattern.matcher(name).matches();
    }
}