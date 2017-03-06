package sophie.util;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class PathExpression {
	private Pattern[] patterns;
	
	public PathExpression(String path) {
		ArrayList<Pattern> patternList = new ArrayList<Pattern>();
		String[] elements = path.split("/", -1);
		int i;
		for(i = 0; i < elements.length; i++) {
			elements[i] = elements[i].trim();
		}
		i = 0;
		if(elements.length > 0 && elements[0].length() == 0) {
			i++;
		}
		
		for(; i < elements.length; i++) {
			if(elements[i].length() == 0) {
				int j;
				for(j = i+1; j < elements.length && elements[j].length() == 0; j++) {
					// Nothing to do. 
				}
				if(j >= elements.length) {
					throw new IllegalArgumentException("No path elements after '/'.");
				}
				if(j - i > 1) {
					throw new IllegalArgumentException("'/' after '//' in " + path);
				}
				patternList.add(null);
			} else if(elements[i].equals("**")) {
				patternList.add(null);
			} else {
				patternList.add(Pattern.compile(glob2Re(elements[i])));
			}
		}
		patterns = patternList.toArray(new Pattern[patternList.size()]);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < patterns.length; i++) {
			Pattern p = patterns[i];
			if(p == null) {
				if(i == 0) {
					sb.append('/');
				}
			} else {
				sb.append(p.pattern());
			}
			if(i + 1 < patterns.length || p == null) {
				sb.append('/');
			}
		}
		return sb.toString();
	}
	
	static String glob2Re(String s) {
		StringBuilder sb = new StringBuilder();
		while(s.startsWith("(?i)") || s.startsWith("(?u)")) {
			if(s.startsWith("(?i)")) {
				sb.append("(?i)");
				s = s.substring(4);
			} else if(s.startsWith("(?u)")) {
				sb.append("(?u)");
				s = s.substring(4);
			} else {
				throw new IllegalStateException();
			}
		}
		char[] ca = s.toCharArray();
		for(int i = 0; i < ca.length; i++) {
			char c = ca[i];
			switch(c) {
			case '*':
				sb.append(".*");
				break;
			case '?':
				sb.append('.');
				break;
			case '.':
				sb.append("\\.");
				break;
			case '\\':
				if(i + 1 < ca.length) {
					i++;
					if(ca[i] == '*' || ca[i] == '?') {
						sb.append(ca[i]);
					} else {
						sb.append('\\');
						sb.append(ca[i]);
					}
				} else {
					sb.append("\\\\");
				}
				break;
			default:
				sb.append(c);
				break;
			}
		}
		return sb.toString();
	}
	
	public boolean isDescendant(String path) {
		String[] elements = pathArray(path);
		String[] elements1 = new String[elements.length + 1];
		for(int i = 0; i < elements.length; i++) {
			elements1[i] = elements[i];
		}
		return matches(elements1, 0, elements1.length, 0, patterns.length);
	}
	
	public boolean isAncestor(String path) {
		String[] elements = pathArray(path);
		for(int n = elements.length -1; n >= 0; n--) {
			if(matches(elements, 0, n, 0, patterns.length)) {
				return true;
			}
		}
		return false;		
	}
	
	public boolean matches(String path) {
		String[] elements = pathArray(path);
		return matches(elements, 0, elements.length, 0, patterns.length);
	}
	
	private boolean matches(String[] elements, int elemBegin, int elemEnd, int patBegin, int patEnd) {
		int i;
		int j;
		for(i = elemBegin, j = patBegin; i < elemEnd && j < patEnd; i++, j++) {
			Pattern pattern = patterns[j];
			if(pattern != null) {
				if(elements[i] == null) {
					// only last item can be null.
					return true;
				}
				if(!pattern.matcher(elements[i]).matches()) {
					return false;
				}
			} else {
				for(int k = elemEnd; k >= i; k--) {
					if(matches(elements, k, elemEnd, j+1, patEnd)) {
						return true;
					}
				}
				return false;
			}
		}
		while(j < patEnd) {
			if(patterns[j] != null)
				break;
			j++;
		}
		return i == elemEnd && j == patEnd;
	}

	private static String[] pathArray(String path) {
		String[] elements = path.split("/", -1);
		for(int i = 0; i < elements.length; i++) {
			elements[i] = elements[i].trim();
		}
		return elements;
	}
	
/*
	private static void unitTestMatches() {
		System.out.println("unitTestMatches()");
    	PathExpression pex;
    	String path;
    	pex = new PathExpression("a/b/c/d/e"); System.out.println(pex.toString());
    	path = "a/b/c/d/e";
    	System.out.println(path + ": " + pex.matches(path));
    	path = "a/b/c/d/e/f";
    	System.out.println(path + ": " + pex.matches(path));
    	path = "a/b/c/d";
    	System.out.println(path + ": " + pex.matches(path));
    	pex = new PathExpression("a/b|B/(c|C)/d/(\\?i)e"); System.out.println(pex.toString());
    	path = "a/b/c/d/e";
    	System.out.println(path + ": " + pex.matches(path));
    	path = "a/B/c/d/e";
    	System.out.println(path + ": " + pex.matches(path));
    	path = "a/b/c/d/e";
    	System.out.println(path + ": " + pex.matches(path));
    	path = "a/b/C/d/E";
    	System.out.println(path + ": " + pex.matches(path));
    	pex = new PathExpression("a/b/c.*"); System.out.println(pex.toString());
    	path = "a/b/c.";
    	System.out.println(path + ": " + pex.matches(path));
    	path = "a/b/c.a";
    	System.out.println(path + ": " + pex.matches(path));
    	path = "a/b/c.ab";
    	System.out.println(path + ": " + pex.matches(path));
    	pex = new PathExpression("a/b/c.?"); System.out.println(pex.toString());
    	path = "a/b/c.";
    	System.out.println(path + ": " + pex.matches(path));
    	path = "a/b/c.a";
    	System.out.println(path + ": " + pex.matches(path));
    	path = "a/b/c.ab";
    	System.out.println(path + ": " + pex.matches(path));
    	pex = new PathExpression("a//c"); System.out.println(pex.toString());
    	path = "a/c";
    	System.out.println(path + ": " + pex.matches(path));
    	path = "a/b/c";
    	System.out.println(path + ": " + pex.matches(path));
    	path = "a/b/x/c";
    	System.out.println(path + ": " + pex.matches(path));
    	path = "a/b/c/c";
    	System.out.println(path + ": " + pex.matches(path));
    	path = "a/c/d";
    	System.out.println(path + ": " + pex.matches(path));		
    	pex = new PathExpression("a/b/c/**"); System.out.println(pex.toString());
    	path = "a/b";
    	System.out.println(path + ": " + pex.matches(path));
    	path = "a/b/c";
    	System.out.println(path + ": " + pex.matches(path));
    	path = "a/b/c/d";
    	System.out.println(path + ": " + pex.matches(path));
    	path = "a/b/c/d/e";
    	System.out.println(path + ": " + pex.matches(path));		
	}
	
	private static void unitTestAncestor() {
		System.out.println("unitTestAncestor()");
    	PathExpression pex;
    	String path;
    	pex = new PathExpression("a/b/c/d/e"); System.out.println(pex.toString());
    	path = "a/b/c/d/e/f/g";
    	System.out.println(path + ": " + pex.isAncestor(path));
    	path = "a/b/c/d/e/f";
    	System.out.println(path + ": " + pex.isAncestor(path));
    	path = "a/b/c/d/e";
    	System.out.println(path + ": " + pex.isAncestor(path));
    	path = "a/b/c/d";
    	System.out.println(path + ": " + pex.isAncestor(path));
    	path = "a/b/c";
    	System.out.println(path + ": " + pex.isAncestor(path));
    	path = "a/b";
    	System.out.println(path + ": " + pex.isAncestor(path));
    	path = "a";
    	System.out.println(path + ": " + pex.isAncestor(path));
    	path = "";
    	System.out.println(path + ": " + pex.isAncestor(path));
    	pex = new PathExpression("a//e"); System.out.println(pex.toString());
    	path = "a/b/c/d/e/f/g";
    	System.out.println(path + ": " + pex.isAncestor(path));
    	path = "a/b/c/d/e/f";
    	System.out.println(path + ": " + pex.isAncestor(path));
    	path = "a/b/c/d/e";
    	System.out.println(path + ": " + pex.isAncestor(path));
    	path = "a/b/c/d";
    	System.out.println(path + ": " + pex.isAncestor(path));
    	path = "a/b/c";
    	System.out.println(path + ": " + pex.isAncestor(path));
    	path = "a/b";
    	System.out.println(path + ": " + pex.isAncestor(path));
    	path = "a";
    	System.out.println(path + ": " + pex.isAncestor(path));
    	path = "";
    	System.out.println(path + ": " + pex.isAncestor(path));
//
    	pex = new PathExpression("a//e"); System.out.println(pex.toString());
    	path = "a/b/e/d/e/f/g";
    	System.out.println(path + ": " + pex.isAncestor(path));
    	path = "a/b/e/d/e/f";
    	System.out.println(path + ": " + pex.isAncestor(path));
    	path = "a/b/e/d/e";
    	System.out.println(path + ": " + pex.isAncestor(path));
    	path = "a/b/e/d";
    	System.out.println(path + ": " + pex.isAncestor(path));
    	path = "a/b/e";
    	System.out.println(path + ": " + pex.isAncestor(path));
    	path = "a/b";
    	System.out.println(path + ": " + pex.isAncestor(path));
    	path = "a";
    	System.out.println(path + ": " + pex.isAncestor(path));
    	path = "";
    	System.out.println(path + ": " + pex.isAncestor(path));
    	pex = new PathExpression("//x"); System.out.println(pex.toString());
    	path = "a/x/c";
    	System.out.println(path + ": " + pex.isAncestor(path));
	}

	private static void unitTestDescendatnt() {
		System.out.println("unitTestDescendatnt()");
    	PathExpression pex;
    	String path;
    	pex = new PathExpression("a/b/c/d/e"); System.out.println(pex.toString());
    	path = "a/b/c/d/e/f/g";
    	System.out.println(path + ": " + pex.isDescendant(path));
    	path = "a/b/c/d/e/f";
    	System.out.println(path + ": " + pex.isDescendant(path));
    	path = "a/b/c/d/e";
    	System.out.println(path + ": " + pex.isDescendant(path));
    	path = "a/b/c/d";
    	System.out.println(path + ": " + pex.isDescendant(path));
    	path = "a/b/c";
    	System.out.println(path + ": " + pex.isDescendant(path));
    	path = "a/b";
    	System.out.println(path + ": " + pex.isDescendant(path));
    	path = "a";
    	System.out.println(path + ": " + pex.isDescendant(path));
    	path = "";
    	System.out.println(path + ": " + pex.isDescendant(path));
    	pex = new PathExpression("a//e"); System.out.println(pex.toString());
    	path = "a/b/c/d/e/f/g";
    	System.out.println(path + ": " + pex.isDescendant(path));
    	path = "a/b/c/d/e/f";
    	System.out.println(path + ": " + pex.isDescendant(path));
    	path = "a/b/c/d/e";
    	System.out.println(path + ": " + pex.isDescendant(path));
    	path = "a/b/c/d";
    	System.out.println(path + ": " + pex.isDescendant(path));
    	path = "a/b/c";
    	System.out.println(path + ": " + pex.isDescendant(path));
    	path = "a/b";
    	System.out.println(path + ": " + pex.isDescendant(path));
    	path = "a";
    	System.out.println(path + ": " + pex.isDescendant(path));
    	path = "";
    	System.out.println(path + ": " + pex.isDescendant(path));
//
    	pex = new PathExpression("a/b/c"); System.out.println(pex.toString());
    	path = "a/b/e/d/e/f/g";
    	System.out.println(path + ": " + pex.isDescendant(path));
    	path = "a/b/e/d/e/f";
    	System.out.println(path + ": " + pex.isDescendant(path));
    	path = "a/b/e/d/e";
    	System.out.println(path + ": " + pex.isDescendant(path));
    	path = "a/b/e/d";
    	System.out.println(path + ": " + pex.isDescendant(path));
    	path = "a/b/e";
    	System.out.println(path + ": " + pex.isDescendant(path));
    	path = "a/b";
    	System.out.println(path + ": " + pex.isDescendant(path));
    	path = "a";
    	System.out.println(path + ": " + pex.isDescendant(path));
    	path = "";
    	System.out.println(path + ": " + pex.isDescendant(path));
	}
	
	private static void unitTestPathExpression() {
		System.out.println("unitTestPathExpression()");
    	PathExpression pex;
    	String s;
    	s = "a*b?c.txt\\*\\.\\"; pex = new PathExpression(s); System.out.println(s + ": " + pex.toString());
    	s = "a/b"; pex = new PathExpression(s); System.out.println(s + ": " + pex.toString());
    	s = "/a//b"; pex = new PathExpression(s); System.out.println(s + ": " + pex.toString());
    	s = "a//b/c"; pex = new PathExpression(s); System.out.println(s + ": " + pex.toString());
    	s = "//a//b/c"; pex = new PathExpression(s); System.out.println(s + ": " + pex.toString());
    	s = "/**" + "/a/b/c"; pex = new PathExpression(s); System.out.println(s + ": " + pex.toString());
    	s = "**" +  "/a/b/c"; pex = new PathExpression(s); System.out.println(s + ": " + pex.toString());
    	s = "a/**" + "/b/c"; pex = new PathExpression(s); System.out.println(s + ": " + pex.toString());
    	s = "a/b/c/**"; pex = new PathExpression(s); System.out.println(s + ": " + pex.toString());
	}
	
    public static void main(String args[]) {
        try {
        	//unitTestPathExpression();
        	//unitTestMatches();
        	unitTestAncestor();
        	//unitTestDescendatnt();
        } catch(Exception e) {
            e.printStackTrace(System.err);
        }
    }
*/
}
