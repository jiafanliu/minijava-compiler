package minijava.symboltable;

import java.util.*;

/**
 * The info of a class.
 * 
 * @author jeff
 *
 */
public class MClass extends MType {
	public MClasses classesTable;
	public String superClassName;
	public Hashtable<String, MVariable> memberVars = new Hashtable<String, MVariable>();
	public Hashtable<String, MMethod> memberMethods = new Hashtable<String, MMethod>();

	public MClass(MType classID, MClasses classesTable, String superClassName) {
		super(classID);
		this.classesTable = classesTable;
		this.superClassName = superClassName;
	}

	public MClass(String className) {
		super(className, -1, -1);
	}

	public boolean insertMethod(MMethod method) {
		if (memberMethods.containsKey(method.symbolName)) {
			return false;
		} else {
			memberMethods.put(method.symbolName, method);
			return true;
		}
	}

	public boolean insertVar(MVariable var) {
		if (memberVars.containsKey(var.symbolName)) {
			return false;
		} else {
			memberVars.put(var.symbolName, var);
			return true;
		}
	}

	public MMethod queryMethod(String methodName) {
		if (memberMethods.containsKey(methodName))
			return memberMethods.get(methodName);
		else if (superClassName != null) {
			MClass superClass = classesTable.queryClass(superClassName);
			if (superClass == null)
				return null;
			return superClass.queryMethod(methodName);
		} else
			return null;
	}

	public MVariable queryVar(String varName) {
		if (memberVars.containsKey(varName))
			return memberVars.get(varName);
		else if (superClassName != null) {
			MClass superClass = classesTable.queryClass(superClassName);
			if (superClass == null)
				return null;
			return superClass.queryVar(varName);
		} else
			return null;
	}

	public MClass(MType another) {
		super(another);
	}
}