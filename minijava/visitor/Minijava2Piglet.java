package minijava.visitor;

import java.util.*;
import minijava.minijava2piglet.PigletOut;
import minijava.symboltable.*;
import minijava.syntaxtree.*;

/**
 * Compile Minijava code to Piglet code.
 * 
 * @author jeff
 *
 */
public class Minijava2Piglet extends GJDepthFirst<MType, MType> {
	static int tempNo = 0;
	static int labelNo = 0;
	MClasses classesTable = null;
	MClass currClass = null;
	MMethod currMethod = null;

	// need to translate Class to Table
	// Variable Table
	static Hashtable<String, Vector<String>> VTable = new Hashtable<String, Vector<String>>();
	// Dispatch Table
	static Hashtable<String, Vector<String>> DTable = new Hashtable<String, Vector<String>>();

	boolean greaterThan20 = false;
	int paramNo = 0;

	// local var -> tempNo
	static Hashtable<String, Integer> localVarTable = new Hashtable<String, Integer>();

	Vector<String> setVTable(MClass Class) {
		if (VTable.containsKey(Class.symbolName))
			return (Vector<String>) VTable.get(Class.symbolName).clone();
		Vector<String> _ret = null;
		// superclass first
		if (Class.superClassName != null)
			_ret = setVTable(classesTable.queryClass(Class.superClassName));
		else
			_ret = new Vector<String>();
		for (String str : Class.memberVars.keySet()) {
			_ret.addElement(str);
		}
		VTable.put(Class.symbolName, _ret);
		return _ret;
	}

	int getVTableOffset(MClass Class, String var) {
		Vector<String> vtable = VTable.get(Class.symbolName);
		// search from end
		int idx = vtable.size();
		while ((--idx) != -1) {
			// the first element is the link to DTable
			if (vtable.get(idx).equals(var))
				return 4 * (idx + 1);
		}
		return -1;
	}

	Vector<String> setDTable(MClass Class) {
		if (DTable.containsKey(Class.symbolName))
			return (Vector<String>) DTable.get(Class.symbolName).clone();
		Vector<String> _ret = null;
		// superclass first
		if (Class.superClassName != null)
			_ret = setDTable(classesTable.queryClass(Class.superClassName));
		else
			_ret = new Vector<String>();
		for (String str : Class.memberMethods.keySet()) {
			if (!_ret.contains(str))
				_ret.addElement(str);
		}
		DTable.put(Class.symbolName, _ret);
		return _ret;
	}

	int getDTableOffset(MClass Class, String var) {
		Vector<String> dtable = DTable.get(Class.symbolName);
		int idx = dtable.size();
		while ((--idx) != -1) {
			if (dtable.get(idx).equals(var))
				return 4 * idx;
		}
		return -1;
	}

	/**
	 * f0 ->"class"
	 * f1 -> Identifier()
	 * f2 ->"{"
	 * f3 ->"public"
	 * f4 ->"static"
	 * f5 ->"void"
	 * f6 ->"main"
	 * f7 ->"("
	 * f8 ->"String"
	 * f9 ->"["
	 * f10 ->"]"
	 * f11 -> Identifier()
	 * f12 ->")"
	 * f13 ->"{"
	 * f14 -> ( VarDeclaration() )*
	 * f15 -> ( Statement() )*
	 * f16 ->"}"
	 * f17 ->"}"
	 */
	// output main
	public MType visit(MainClass n, MType argu) {
		MType _ret = null;
		classesTable = (MClasses) argu;
		for (String className : classesTable.classesTable.keySet()) {
			MClass Class = classesTable.queryClass(className);
			setVTable(Class);
			setDTable(Class);
		}
		currClass = classesTable.queryClass(n.f1.accept(this, argu).symbolName);
		currMethod = currClass.queryMethod("main");
		for (MVariable param : currMethod.params) {
			localVarTable.put(param.symbolName, tempNo);
			tempNo++;
		}
		PigletOut.beginMain();
		for (MVariable otherLocalVar : currMethod.otherLocalVars) {
			localVarTable.put(otherLocalVar.symbolName, tempNo);
			PigletOut.println("MOVE TEMP " + tempNo + " 0");// set default to 0
			tempNo++;
		}
		n.f15.accept(this, currMethod);
		PigletOut.end();
		PigletOut.println();
		return _ret;
	}

	/**
	 * f0 ->"class"
	 * f1 -> Identifier()
	 * f2 ->"{"
	 * f3 -> ( VarDeclaration() )*
	 * f4 -> ( MethodDeclaration() )*
	 * f5 ->"}"
	 */
	// set current Class
	public MType visit(ClassDeclaration n, MType argu) {
		MType _ret = null;
		String className = n.f1.accept(this, argu).symbolName;
		currClass = classesTable.queryClass(className);
		n.f3.accept(this, currClass);
		n.f4.accept(this, currClass);
		return _ret;
	}

	/**
	 * f0 ->"class"
	 * f1 -> Identifier()
	 * f2 ->"extends"
	 * f3 -> Identifier()
	 * f4 ->"{"
	 * f5 -> ( VarDeclaration() )*
	 * f6 -> ( MethodDeclaration() )*
	 * f7 ->"}"
	 */
	// set current Class
	public MType visit(ClassExtendsDeclaration n, MType argu) {
		MType _ret = null;
		String className = n.f1.accept(this, argu).symbolName;
		currClass = classesTable.queryClass(className);
		n.f5.accept(this, currClass);
		n.f6.accept(this, currClass);
		return _ret;
	}

	/**
	 * f0 ->"public"
	 * f1 -> Type()
	 * f2 -> Identifier()
	 * f3 ->"("
	 * f4 -> ( FormalParameterList() )?
	 * f5 ->")"
	 * f6 ->"{"
	 * f7 -> ( VarDeclaration() )*
	 * f8 -> ( Statement() )*
	 * f9 ->"return"
	 * f10 -> Expression()
	 * f11 ->";"
	 * f12 ->"}"
	 */
	public MType visit(MethodDeclaration n, MType argu) {
		MType _ret = null;
		String methodName = n.f2.accept(this, argu).symbolName;
		currMethod = currClass.queryMethod(methodName);
		// make local var table
		localVarTable = new Hashtable<String, Integer>();
		localVarTable.put("this", 0);
		// reset temp number
		tempNo = 1;
		int size = currMethod.params.size();
		if (size >= 20) {
			for (MVariable param : currMethod.params) {
				localVarTable.put(param.symbolName, tempNo);
				tempNo++;
				if (tempNo == 19)
					break;
			}
			tempNo++;// TEMP 19 for array address
		} else {
			for (MVariable param : currMethod.params) {
				localVarTable.put(param.symbolName, tempNo);
				tempNo++;
			}
		}
		if (size >= 20)
			size = 19;
		PigletOut.println(currClass.symbolName + "_" + currMethod.symbolName + " [" + (size + 1) + "]");
		PigletOut.begin();
		for (MVariable otherLocalVar : currMethod.otherLocalVars) {
			localVarTable.put(otherLocalVar.symbolName, tempNo);
			PigletOut.println("MOVE TEMP " + tempNo + " 0");// set default to 0
			tempNo++;
		}
		n.f8.accept(this, argu);
		PigletOut.print("RETURN");
		n.f10.accept(this, argu);
		PigletOut.println();
		PigletOut.end();
		PigletOut.println();
		return _ret;
	}

	/**
	 * f0 -> Identifier()
	 * f1 ->"="
	 * f2 -> Expression()
	 * f3 ->";"
	 */
	public MType visit(AssignmentStatement n, MType argu) {
		MType _ret = null;
		String varName = n.f0.accept(this, argu).symbolName;
		if (localVarTable.get(varName) != null) {// in localVarTable
			int tempVar = localVarTable.get(varName);
			PigletOut.print("MOVE TEMP " + tempVar);
		} else if (currClass.queryVar(varName) != null) {// class var
			PigletOut.print("HSTORE TEMP 0 " + getVTableOffset(currClass, varName));
		} else if (currMethod.varsTable.containsKey(varName)) { // param after
																// TEMP 18
			int offset;
			int size = currMethod.params.size();
			// params 0 ~ 17 already in localVarTable
			for (offset = 18; offset < size; offset++) {
				if (currMethod.params.elementAt(offset).symbolName.equals(varName))
					break;
			}
			offset = (offset - 17) * 4;
			PigletOut.print("HSTORE TEMP 19 " + offset);
		}
		n.f2.accept(this, argu);
		PigletOut.println();
		return _ret;
	}

	/**
	 * f0 -> Identifier()
	 * f1 ->"["
	 * f2 -> Expression()
	 * f3 ->"]"
	 * f4 ->"="
	 * f5 -> Expression()
	 * f6 ->";"
	 */
	public MType visit(ArrayAssignmentStatement n, MType argu) {
		MType _ret = null;
		String varName = n.f0.accept(this, argu).symbolName;
		int arrayAddr = (tempNo++);
		if (localVarTable.get(varName) != null) {// in localVarTable
			int tempVar = localVarTable.get(varName);
			PigletOut.print("MOVE TEMP " + arrayAddr + " TEMP " + tempVar);
		} else if (currClass.queryVar(varName) != null) {// class var
			PigletOut.println("HLOAD TEMP " + arrayAddr + " TEMP 0 " + getVTableOffset(currClass, varName));
		} else if (currMethod.varsTable.containsKey(varName)) { // param after
																// TEMP 18
			int offset;
			int size = currMethod.params.size();
			// params 0 ~ 17 already in localVarTable
			for (offset = 18; offset < size; offset++) {
				if (currMethod.params.elementAt(offset).symbolName.equals(varName))
					break;
			}
			offset = (offset - 17) * 4;
			PigletOut.println("HLOAD TEMP " + arrayAddr + " TEMP 19 " + offset);
		}
		// check if null
		int noErrorLabel = (labelNo++);
		PigletOut.println("CJUMP LT TEMP " + arrayAddr + " 1 L" + noErrorLabel);
		PigletOut.println("ERROR");
		PigletOut.println("L" + noErrorLabel + " NOOP");
		// get index
		int tempIdx = (tempNo++);
		PigletOut.print("MOVE TEMP " + tempIdx);
		n.f2.accept(this, argu);
		PigletOut.println();
		// check out of range
		int tempLen = (tempNo++);
		PigletOut.println("HLOAD TEMP " + tempLen + " TEMP " + arrayAddr + " 0");
		PigletOut.println("CJUMP MINUS 1 LT TEMP " + tempIdx + " TEMP " + tempLen + " L" + labelNo);
		PigletOut.println("ERROR"); // if !(Idx<Len)
		PigletOut.print("L" + labelNo + " HSTORE PLUS TEMP " + arrayAddr + " TIMES 4 PLUS 1 TEMP " + tempIdx + " 0");
		labelNo++;
		n.f5.accept(this, argu);
		PigletOut.println("");
		return _ret;
	}

	/**
	 * f0 ->"if"
	 * f1 ->"("
	 * f2 -> Expression()
	 * f3 ->")"
	 * f4 -> Statement()
	 * f5 ->"else"
	 * f6 -> Statement()
	 */
	public MType visit(IfStatement n, MType argu) {
		MType _ret = null;
		// if
		PigletOut.print("CJUMP");
		n.f2.accept(this, argu);
		int falseLabel = (labelNo++);
		PigletOut.println("L" + falseLabel);
		// true
		n.f4.accept(this, argu);
		int overLabel = (labelNo++);
		PigletOut.println("JUMP L" + overLabel);
		// false
		PigletOut.println("L" + falseLabel + " NOOP");
		n.f6.accept(this, argu);
		// over
		PigletOut.println("L" + overLabel + " NOOP");
		return _ret;
	}

	/**
	 * f0 ->"while"
	 * f1 ->"("
	 * f2 -> Expression()
	 * f3 ->")"
	 * f4 -> Statement()
	 */
	public MType visit(WhileStatement n, MType argu) {
		MType _ret = null;
		// while
		int loopLabel = (labelNo++);
		PigletOut.print("L" + loopLabel + " CJUMP");
		n.f2.accept(this, argu);
		int outLabel = (labelNo++);
		PigletOut.println("L" + outLabel);
		// true
		n.f4.accept(this, argu);
		PigletOut.println("JUMP L" + loopLabel);
		// over
		PigletOut.println("L" + outLabel + " NOOP");
		return _ret;
	}

	/**
	 * f0 ->"System.out.println"
	 * f1 ->"("
	 * f2 -> Expression()
	 * f3 ->")"
	 * f4 ->";"
	 */
	// PRINT
	public MType visit(PrintStatement n, MType argu) {
		MType _ret = null;
		PigletOut.print("PRINT");
		n.f2.accept(this, argu);
		PigletOut.println();
		return _ret;
	}

	/**
	 * f0 -> AndExpression()
	 * | CompareExpression()
	 * | PlusExpression()
	 * | MinusExpression()
	 * | TimesExpression()
	 * | ArrayLookup()
	 * | ArrayLength()
	 * | MessageSend()
	 * | PrimaryExpression()
	 */
	// return the result of the expression
	// *The result may be an object for MessageSend() or PrimaryExpression()*
	public MType visit(Expression n, MType argu) {
		MType _ret = n.f0.accept(this, argu);
		return _ret;
	}

	/**
	 * f0 -> PrimaryExpression()
	 * f1 ->"&&"
	 * f2 -> PrimaryExpression()
	 */
	// CJUMP
	// *Consider short-circuit evaluation*
	public MType visit(AndExpression n, MType argu) {
		MType _ret = null;
		PigletOut.begin();
		int tempBool = (tempNo++);
		PigletOut.println("MOVE TEMP " + tempBool + " 0");
		// left == false?
		int falseLabel = (labelNo++);
		PigletOut.print("CJUMP");
		n.f0.accept(this, argu);
		PigletOut.println("L" + falseLabel);
		// right == false?
		PigletOut.print("CJUMP");
		n.f2.accept(this, argu);
		PigletOut.println("L" + falseLabel);
		// both true
		PigletOut.println("MOVE TEMP " + tempBool + " 1");
		// return
		PigletOut.println("L" + falseLabel + " NOOP");
		PigletOut.println("RETURN TEMP " + tempBool);
		PigletOut.end();
		return _ret;
	}

	/**
	 * f0 -> PrimaryExpression()
	 * f1 ->"<"
	 * f2 -> PrimaryExpression()
	 */
	// LT
	public MType visit(CompareExpression n, MType argu) {
		MType _ret = null;
		PigletOut.print("LT");
		n.f0.accept(this, argu);
		n.f2.accept(this, argu);
		return _ret;
	}

	/**
	 * f0 -> PrimaryExpression()
	 * f1 ->"+"
	 * f2 -> PrimaryExpression()
	 */
	// PLUS
	public MType visit(PlusExpression n, MType argu) {
		MType _ret = null;
		PigletOut.print("PLUS");
		n.f0.accept(this, argu);
		n.f2.accept(this, argu);
		return _ret;
	}

	/**
	 * f0 -> PrimaryExpression()
	 * f1 ->"-"
	 * f2 -> PrimaryExpression()
	 */
	// MINUS
	public MType visit(MinusExpression n, MType argu) {
		MType _ret = null;
		PigletOut.print("MINUS");
		n.f0.accept(this, argu);
		n.f2.accept(this, argu);
		return _ret;
	}

	/**
	 * f0 -> PrimaryExpression()
	 * f1 ->"*"
	 * f2 -> PrimaryExpression()
	 */
	// TIMES
	public MType visit(TimesExpression n, MType argu) {
		MType _ret = null;
		PigletOut.print("TIMES");
		n.f0.accept(this, argu);
		n.f2.accept(this, argu);
		return _ret;
	}

	/**
	 * f0 -> PrimaryExpression()
	 * f1 ->"["
	 * f2 -> PrimaryExpression()
	 * f3 ->"]"
	 */
	public MType visit(ArrayLookup n, MType argu) {
		MType _ret = null;
		// get array
		PigletOut.begin();
		int tempArrayAddr = (tempNo++);
		PigletOut.print("MOVE TEMP " + tempArrayAddr);
		n.f0.accept(this, argu);
		PigletOut.println();
		// check if null
		int noErrorLabel = (labelNo++);
		PigletOut.println("CJUMP LT TEMP " + tempArrayAddr + " 1 L" + noErrorLabel);
		PigletOut.println("ERROR");
		PigletOut.println("L" + noErrorLabel + " NOOP");
		// get index
		int tempIdx = (tempNo++);
		PigletOut.print("MOVE TEMP " + tempIdx);
		n.f2.accept(this, argu);
		PigletOut.println();
		// get element
		int tempEle = (tempNo++);
		int tempLen = (tempNo++);
		PigletOut.println("HLOAD TEMP " + tempLen + " TEMP " + tempArrayAddr + " 0");
		PigletOut.println("CJUMP MINUS 1 LT TEMP " + tempIdx + " TEMP " + tempLen + " L" + labelNo);
		PigletOut.println("ERROR"); // if !(Idx<Len)
		// ArrayAddr + 4*Idx + 1
		PigletOut.println("L" + labelNo + " HLOAD TEMP " + tempEle + " PLUS TEMP " + tempArrayAddr
				+ " TIMES 4 PLUS 1 TEMP " + tempIdx + " 0");
		labelNo++;
		PigletOut.println("RETURN TEMP " + tempEle);
		PigletOut.end();
		return _ret;
	}

	/**
	 * f0 -> PrimaryExpression()
	 * f1 ->"."
	 * f2 ->"length"
	 */
	public MType visit(ArrayLength n, MType argu) {
		MType _ret = null;
		// get array
		PigletOut.begin();
		int tempArrayAddr = (tempNo++);
		PigletOut.print("MOVE TEMP " + tempArrayAddr);
		n.f0.accept(this, argu);
		PigletOut.println();
		// check if null
		int noErrorLabel = (labelNo++);
		PigletOut.println("CJUMP LT TEMP " + tempArrayAddr + " 1 L" + noErrorLabel);
		PigletOut.println("ERROR");
		PigletOut.println("L" + noErrorLabel + " NOOP");
		// get the first block
		int tempLen = (tempNo++);
		PigletOut.println("HLOAD TEMP " + tempLen + " TEMP " + tempArrayAddr + " 0");
		PigletOut.println("RETURN TEMP " + tempLen);
		PigletOut.end();
		return _ret;
	}

	/**
	 * f0 -> PrimaryExpression()
	 * f1 ->"."
	 * f2 -> Identifier()
	 * f3 ->"("
	 * f4 -> ( ExpressionList() )?
	 * f5 ->")"
	 */
	public MType visit(MessageSend n, MType argu) {
		PigletOut.print("CALL");
		PigletOut.begin();
		int tempVTable = (tempNo++);
		PigletOut.print("MOVE TEMP " + tempVTable);
		MType Class = n.f0.accept(this, argu);
		PigletOut.println();
		// check if null
		int noErrorLabel = (labelNo++);
		PigletOut.println("CJUMP LT TEMP " + tempVTable + " 1 L" + noErrorLabel);
		PigletOut.println("ERROR");
		PigletOut.println("L" + noErrorLabel + " NOOP");
		String methodName = n.f2.accept(this, argu).symbolName;
		// get method
		MMethod method = classesTable.queryClass(Class.symbolName).queryMethod(methodName);
		// return returnType
		MType _ret = classesTable.queryClass(((MMethod) method).returnType);
		// load method
		int tempMethod = (tempNo++);
		int tempDTable = (tempNo++);
		PigletOut.println("HLOAD TEMP " + tempDTable + " TEMP " + tempVTable + " 0");
		PigletOut.println(
				"HLOAD TEMP " + tempMethod + " TEMP " + tempDTable + " " + getDTableOffset((MClass) Class, methodName));
		PigletOut.println("RETURN TEMP " + tempMethod);
		PigletOut.end();
		// output "(params)"
		PigletOut.print("(TEMP " + tempVTable); // class itself
		n.f4.accept(this, method);// other params
		PigletOut.println(")");
		return _ret;
	}

	/**
	 * f0 -> Expression()
	 * f1 -> ( ExpressionRest() )*
	 */
	public MType visit(ExpressionList n, MType argu) {
		MType _ret = null;
		int size = ((MMethod) argu).params.size();
		if (size >= 20)
			greaterThan20 = true;
		paramNo = 1; // reset
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		greaterThan20 = false;
		return _ret;
	}

	/**
	 * f0 ->","
	 * f1 -> Expression()
	 */
	static int tempArray = -1; // should be static for many ExpressionRest

	public MType visit(ExpressionRest n, MType argu) {
		MType _ret = null;
		paramNo++;
		int size = ((MMethod) argu).params.size();
		if (greaterThan20) {
			if (paramNo == 19) { // begin: array address at TEMP 19
				int tempLen = (tempNo++);
				tempArray = (tempNo++);
				int leftParams = size - 18;
				PigletOut.begin();
				PigletOut.println("MOVE TEMP " + tempLen + " " + leftParams);
				PigletOut.println("MOVE TEMP " + tempArray + " HALLOCATE TIMES 4 PLUS 1 TEMP " + tempLen);
				PigletOut.println("HSTORE TEMP " + tempArray + " 0 TEMP " + tempLen);
				PigletOut.print("HSTORE TEMP " + tempArray + " 4");
				n.f1.accept(this, argu);
				PigletOut.println();
			} else if (paramNo > 19) { // store every param after TEMP 18
				PigletOut.print("HSTORE TEMP " + tempArray + " " + (4 * (paramNo - 18)));
				n.f1.accept(this, argu);
				PigletOut.println();
			} else {
				n.f1.accept(this, argu);
			}
			if (paramNo == size) {
				PigletOut.println("RETURN TEMP " + tempArray);
				PigletOut.end();
			}
		} else
			n.f1.accept(this, argu);
		return _ret;
	}

	/**
	 * f0 -> IntegerLiteral()
	 * | TrueLiteral()
	 * | FalseLiteral()
	 * | Identifier()
	 * | ThisExpression()
	 * | ArrayAllocationExpression()
	 * | AllocationExpression()
	 * | NotExpression()
	 * | BracketExpression()
	 */
	public MType visit(PrimaryExpression n, MType argu) {
		MType _ret = n.f0.accept(this, argu);
		if (_ret == null)
			return null;
		if (_ret.symbolName.equals("int") || _ret.symbolName.equals("boolean") || _ret.symbolName.equals("int[]"))
			return _ret;
		MClass Class = classesTable.queryClass(_ret.symbolName);
		if (Class != null) // classType
			return _ret;
		// Identifier
		// print it (TEMP or HLOAD) and return
		String varName = _ret.symbolName;
		// local var
		if (localVarTable.get(varName) != null) {
			int tempVar = localVarTable.get(varName);
			PigletOut.print("TEMP " + tempVar);
			String varType = currMethod.queryVar(varName).varType;
			_ret = classesTable.queryClass(varType);

		} else if (currClass.queryVar(varName) != null) {// class var
			int tempVar = (tempNo++);
			PigletOut.begin();
			PigletOut.println("HLOAD TEMP " + tempVar + " TEMP 0 " + getVTableOffset(currClass, varName));
			PigletOut.println("RETURN TEMP " + tempVar);
			PigletOut.end();
			String varType = currClass.queryVar(varName).varType;
			_ret = classesTable.queryClass(varType);
		} else if (currMethod.varsTable.containsKey(varName)) { // param after
																// TEMP 18
			int tempVar = (tempNo++);
			PigletOut.begin();
			int offset;
			int size = currMethod.params.size();
			// params 0 ~ 17 already in localVarTable
			for (offset = 18; offset < size; offset++) {
				if (currMethod.params.elementAt(offset).symbolName.equals(varName))
					break;
			}
			offset = (offset - 17) * 4;
			PigletOut.println("HLOAD TEMP " + tempVar + " TEMP 19 " + offset);
			PigletOut.println("RETURN TEMP " + tempVar);
			PigletOut.end();
		}
		return _ret;
	}

	/**
	 * f0 -> <INTEGER_LITERAL>
	 */
	// print the original literal string
	public MType visit(IntegerLiteral n, MType argu) {
		MType _ret = null;
		String integer = n.f0.toString();
		PigletOut.print(integer);
		return _ret;
	}

	/**
	 * f0 ->"true"
	 */
	// print 1
	public MType visit(TrueLiteral n, MType argu) {
		MType _ret = null;
		PigletOut.print("1");
		return _ret;
	}

	/**
	 * f0 ->"false"
	 */
	// print 0
	public MType visit(FalseLiteral n, MType argu) {
		MType _ret = null;
		PigletOut.print("0");
		return _ret;
	}

	/**
	 * f0 -> <IDENTIFIER>
	 */
	// return identifier
	public MType visit(Identifier n, MType argu) {
		MType _ret = new MType(n.f0.toString(), n.f0.beginLine, n.f0.beginColumn);
		return _ret;
	}

	/**
	 * f0 ->"this"
	 */
	// TEMP 0
	// return the class
	public MType visit(ThisExpression n, MType argu) {
		MType _ret = currClass;
		PigletOut.print("TEMP 0");
		return _ret;
	}

	/**
	 * f0 ->"new"
	 * f1 ->"int"
	 * f2 ->"["
	 * f3 -> Expression()
	 * f4 ->"]"
	 */
	public MType visit(ArrayAllocationExpression n, MType argu) {
		MType _ret = null;
		PigletOut.begin();
		// get length
		int tempLen = (tempNo++);
		PigletOut.print("MOVE TEMP " + tempLen);
		n.f3.accept(this, argu);
		PigletOut.println();
		// allocate
		int tempArrayAddr = (tempNo++);
		PigletOut.println("MOVE TEMP " + tempArrayAddr + " HALLOCATE TIMES 4 PLUS 1 TEMP " + tempLen);
		// store length in the first block
		PigletOut.println("HSTORE TEMP " + tempArrayAddr + " 0 TEMP " + tempLen);
		// set elements to 0s
		int loopLabel = (labelNo++);
		PigletOut.println("L" + loopLabel + " CJUMP LT 0 TEMP " + tempLen + " L" + labelNo);
		PigletOut.println("HSTORE PLUS TEMP " + tempArrayAddr + " TIMES 4 TEMP " + tempLen + " 0 0");
		PigletOut.println("MOVE TEMP " + tempLen + " MINUS TEMP " + tempLen + " 1");
		PigletOut.println("JUMP L" + loopLabel);
		// return
		PigletOut.println("L" + labelNo + " NOOP");
		PigletOut.println("RETURN TEMP " + tempArrayAddr);
		labelNo++;
		PigletOut.end();
		return _ret;
	}

	/**
	 * f0 ->"new"
	 * f1 -> Identifier()
	 * f2 ->"("
	 * f3 ->")"
	 */
	public MType visit(AllocationExpression n, MType argu) {
		String className = n.f1.accept(this, argu).symbolName;
		MType _ret = classesTable.queryClass(className);
		PigletOut.begin();
		// allocate VTable and DTable for new instance
		int tempV = (tempNo++);
		int tempD = (tempNo++);
		PigletOut.println("MOVE TEMP " + tempV + " HALLOCATE TIMES 4 PLUS 1 " + VTable.get(className).size());
		PigletOut.println("MOVE TEMP " + tempD + " HALLOCATE TIMES 4 " + DTable.get(className).size());
		// store DTable in the first block of VTable
		PigletOut.println("HSTORE TEMP " + tempV + " 0 TEMP " + tempD);
		int offset = 0;
		// set methods
		for (String methodName : DTable.get(className)) {
			String wrappedName = className + "_" + methodName;
			PigletOut.println("HSTORE TEMP " + tempD + " " + offset + " " + wrappedName);
			offset += 4;
		}
		// set vars to 0
		offset = 4;
		for (String varName : VTable.get(className)) {
			PigletOut.println("HSTORE TEMP " + tempV + " " + offset + " 0");
			offset += 4;
		}
		PigletOut.println("RETURN TEMP " + tempV);
		PigletOut.end();

		return _ret;
	}

	/**
	 * f0 ->"!"
	 * f1 -> Expression()
	 */
	// NotExp = 1 - Exp
	public MType visit(NotExpression n, MType argu) {
		MType _ret = null;
		PigletOut.print("MINUS 1");
		n.f1.accept(this, argu);
		PigletOut.println();
		return _ret;
	}

	/**
	 * f0 ->"("
	 * f1 -> Expression()
	 * f2 ->")"
	 */
	// return the result of expression
	public MType visit(BracketExpression n, MType argu) {
		MType _ret = n.f1.accept(this, argu);
		return _ret;
	}
}
