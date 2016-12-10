package minijava.symboltable;

import java.util.*;

/**
 * The info of a method in a class.
 * 
 * @author jeff
 *
 */
public class MMethod extends MType {
	public MClass owner;
	public String returnType;
	public Vector<MVariable> params = new Vector<MVariable>();
	public Vector<MVariable> otherLocalVars = new Vector<MVariable>();
	public Hashtable<String, MVariable> varsTable = new Hashtable<String, MVariable>();

	public boolean insertParam(MVariable param) {
		if (varsTable.containsKey(param.symbolName)) {
			return false;
		} else {
			varsTable.put(param.symbolName, param);
			params.addElement(param);
			return true;
		}
	}

	public boolean insertVar(MVariable var) {
		if (varsTable.containsKey(var.symbolName)) {
			return false;
		} else {
			varsTable.put(var.symbolName, var);
			otherLocalVars.addElement(var);
			return true;
		}
	}

	public MMethod(String returnType, MType methodID, MClass owner) {
		super(methodID);
		this.owner = owner;
		this.returnType = returnType;
	}

	public MVariable queryVar(String varName) {
		if (varsTable.containsKey(varName))
			return varsTable.get(varName);
		else
			return owner.queryVar(varName);
	}

	public MMethod(MType another) {
		super(another);
	}
}
