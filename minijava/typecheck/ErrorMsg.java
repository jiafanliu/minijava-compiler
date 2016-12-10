package minijava.typecheck;

import java.util.*;
import minijava.symboltable.*;

/**
 * To store and print the error messages.
 * 
 * @author jeff
 */
public class ErrorMsg {
	public static Vector<String> errorMsgs = new Vector<String>();
	public static int verbose = 0; // different output level

	public static void addMsg(int line, int col, String msg) {
		String errorMsg = "Error at Line " + line + ", Column " + col + ": " + msg;
		errorMsgs.addElement(errorMsg);
		if (ErrorMsg.verbose == 0) {
			System.out.println("Type error");
			System.exit(1);
		}
	}

	public static void print(MClasses allClasses) {
		for (String className : allClasses.classesTable.keySet()) {
			MClass Class = allClasses.classesTable.get(className);
			if (Class.superClassName != null)
				System.out.println("Class \"" + className + "\" : extends \"" + Class.superClassName + "\" at line "
						+ Class.lineNo + " Column " + Class.colNo);
			else
				System.out.println("Class \"" + className + "\" at line " + Class.lineNo + " Column " + Class.colNo);
			for (String varName : Class.memberVars.keySet()) {
				MVariable var = Class.memberVars.get(varName);
				System.out.println("  Member variable \"" + varName + "\" of type \"" + var.varType + "\" at line "
						+ var.lineNo + " Column " + var.colNo);
			}
			for (String methodName : Class.memberMethods.keySet()) {
				MMethod method = Class.memberMethods.get(methodName);
				System.out.println("  Member method " + methodName + " of return type \"" + method.returnType
						+ "\" at line " + method.lineNo + " Column " + method.colNo);
				for (MVariable param : method.params) {
					System.out.println("    Parameter of type \"" + param.varType + "\"");
				}
				for (String varName2 : method.varsTable.keySet()) {
					MVariable var2 = method.varsTable.get(varName2);
					System.out.println("    Method variable \"" + varName2 + "\" of type \"" + var2.varType
							+ "\" at line " + var2.lineNo + " Column " + var2.colNo);
				}
			}
		}
	}
}