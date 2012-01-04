package org.l3eta.turntable.util;

import java.util.regex.Pattern;

public class Line {
	private String line;
	
	public Line(String line) {
		this.line = line;
	}
	
	public String getString(String o) {
		String v = Pattern.compile(o + ": \"").split(line)[1];
		return v.substring(0, v.indexOf("\""));
	}
	
	public String getAdvString(String o, String i) {
		String v = Pattern.compile(o + ": \"").split(line)[1];
		return v.substring(0, v.indexOf(i));
	}
	
	public boolean equals(Object obj) {
		return this.toString().equalsIgnoreCase(String.valueOf(obj));
	}
	
	public int getInt(String o) {
		String i = ", ";
		if(o.equals("avatarid"))
			i = "}";
		String v = Pattern.compile(o + ": ").split(line)[1];
		return Integer.parseInt(v.substring(0, v.indexOf(i)));
	}
	
	public double getDouble(String o) {
		String v = Pattern.compile(o + ": ").split(line)[1];
		return Double.parseDouble(v.substring(0, v.indexOf(", ")));
	}
	
	public String toString() {
		return line;
	}

	public int indexOf(String string) {
		return line.indexOf(string);
	}

	public String substring(int indexOf) {
		return line.substring(indexOf);
	}
	
	public String substring(int indexOf, int end) { 
		return line.substring(indexOf, end);
	}
	
	public boolean contains(String string) {
		return line.contains(string);
	}
}