package minijava.visitor;

import minijava.syntaxtree.*;
import minijava.typecheck.ErrorMsg;
import minijava.symboltable.*;

/**
 * To complete type check.
 * 
 * @author jeff
 *
 */
public class TypeCheck extends GJDepthFirst<MType, MType> {
	MClasses classesTable = null;

	/**
	 * @param argu
	 *            MClass/MMethod/MVariable
	 *            Query if it exists in owner.
	 * @param owner
	 *            MClasses/MClass/MMethod
	 * @return inserted MType or null
	 */
	public MType query(MType argu, MType owner) {
		if (argu == null)
			return null;
		MType _ret = null;
		if (argu instanceof MClass) {
			_ret = classesTable.queryClass(argu.symbolName);
			if (_ret == null)
				ErrorMsg.addMsg(argu.lineNo, argu.colNo, "Class " + argu.symbolName + " has not been declared before.");
		} else if (argu instanceof MMethod) {
			_ret = ((MClass) owner).queryMethod(argu.symbolName);
			if (_ret == null)
				ErrorMsg.addMsg(argu.lineNo, argu.colNo,
						"Method " + argu.symbolName + " has not been declared before.");
		} else if (argu instanceof MVariable) {
			if (owner instanceof MMethod) {
				_ret = ((MMethod) owner).queryVar(argu.symbolName);
				if (_ret == null)
					ErrorMsg.addMsg(argu.lineNo, argu.colNo,
							"Variable " + argu.symbolName + " has not been declared before.");

			} else if (owner instanceof MClass) {
				_ret = ((MClass) owner).queryVar(argu.symbolName);
				if (_ret == null)
					ErrorMsg.addMsg(argu.lineNo, argu.colNo,
							"Variable " + argu.symbolName + " has not been declared before.");
			} else {
				ErrorMsg.addMsg(argu.lineNo, argu.colNo, "?");
			}
		}
		return _ret;
	}

	/**
	 * Check if subClass is a subclass of superClass.
	 * 
	 * @param subClass
	 * @param superClass
	 * @return yes or not
	 */
	public boolean isSubClass(MClass subClass, MClass superClass) {
		subClass = classesTable.queryClass(subClass.symbolName);
		while (subClass != null && subClass.superClassName != null) {
			subClass = (MClass) query(new MClass(subClass.superClassName), classesTable);
			if (subClass.symbolName.equals(superClass.symbolName))
				return true;
		}
		return false;
	}

	/**
	 * Check if argu in owner is of type "type".
	 * (If "type" is a clssType, argu can be of "type"'s subclass.)
	 * 
	 * @param argu
	 * @param owner
	 * @param type
	 * @return yes or not
	 */
	public boolean isMatched(MType argu, MType owner, String type) {
		if (argu == null)
			return false;
		// "int"/"boolean"/"int[]"
		if (argu.symbolName.equals(type))
			return true;
		if (argu.symbolName.equals("int") || argu.symbolName.equals("boolean") || argu.symbolName.equals("int[]"))
			return false;
		MClass Class = classesTable.queryClass(argu.symbolName);
		if (Class == null) {
			// Identifier
			MVariable var = (MVariable) query(new MVariable(argu), owner);
			if (var == null)
				return false;
			// int/boolean/int[] identifier
			if (var.varType.equals(type))
				return true;
			// classType identifier
			if (isSubClass(new MClass(var.varType), new MClass(type)))
				return true;

		} else {
			// "classType"
			return Class.symbolName.equals(type) || isSubClass(Class, new MClass(type));
		}
		return false;
	}

	/**
	 * Check if assignment "left=right" matches.
	 * 
	 * @param left
	 * @param right
	 * @param leftOwner
	 * @param rightOwner
	 */
	public void checkMatched(MType left, MType right, MType leftOwner, MType rightOwner) {
		if (left == null || right == null)
			return;
		MVariable leftvar = (MVariable) query(new MVariable(left), leftOwner);
		if (leftvar == null) {
			return;
		} else if (!isMatched(right, rightOwner, leftvar.varType)) {
			ErrorMsg.addMsg(left.lineNo, left.colNo, "Two sides of the assignment does not match.");
		}
	}

	/**
	 * Check if argu is of type "int".
	 * 
	 * @param argu
	 * @param owner
	 * @return yes or not
	 */
	public boolean checkInt(MType argu, MType owner) {
		if (argu == null)
			return false;
		if (!isMatched(argu, owner, "int")) {
			ErrorMsg.addMsg(argu.lineNo, argu.colNo, "The expression " + argu.symbolName + " is not of type INT.");
			return false;
		} else
			return true;
	}

	/**
	 * Check if argu is of type "boolean".
	 * 
	 * @param argu
	 * @param owner
	 * @return yes or not
	 */
	public boolean checkBool(MType argu, MType owner) {
		if (argu == null)
			return false;
		if (!isMatched(argu, owner, "boolean")) {
			ErrorMsg.addMsg(argu.lineNo, argu.colNo, "The expression " + argu.symbolName + " is not of type BOOLEAN.");
			return false;
		} else
			return true;

	}

	/**
	 * Check if argu is of type "int[]".
	 * 
	 * @param argu
	 * @param owner
	 * @return yes or not
	 */
	public boolean checkArray(MType argu, MType owner) {
		if (argu == null)
			return false;
		if (!isMatched(argu, owner, "int[]")) {
			ErrorMsg.addMsg(argu.lineNo, argu.colNo, "The expression " + argu.symbolName + " is not of type ARRAY.");
			return false;
		} else
			return true;
	}

	/**
	 * Query class or object argu in its owner.
	 * 
	 * @param argu
	 * @param owner
	 * @return the class or null
	 */
	public MClass getClass(MType argu, MType owner) {
		if (argu == null)
			return null;
		MClass Class = classesTable.queryClass(argu.symbolName);// Class
		if (Class != null)
			return Class;
		MType Object = query(new MVariable(argu), owner);// Object
		if (Object == null)
			return null;
		Class = classesTable.queryClass(((MVariable) Object).varType);
		if (Class == null)
			ErrorMsg.addMsg(argu.lineNo, argu.colNo, "The symbol " + Object.symbolName + " is not an object.");
		return Class;
	}

	/**
	 * Query method argu in its owner.
	 * 
	 * @param argu
	 * @param owner
	 * @return the method or null
	 */
	public MMethod getMethod(MType argu, MType owner) {
		if (argu == null)
			return null;
		MType get = query(new MMethod(argu), owner);
		if (get == null)
			return null;
		return ((MMethod) get);
	}

	/**
	 * f0 -> "class"
	 * f1 -> Identifier()
	 * f2 -> "{"
	 * f3 -> "public"
	 * f4 -> "static"
	 * f5 -> "void"
	 * f6 -> "main"
	 * f7 -> "("
	 * f8 -> "String"
	 * f9 -> "["
	 * f10 -> "]"
	 * f11 -> Identifier()
	 * f12 -> ")"
	 * f13 -> "{"
	 * f14 -> ( VarDeclaration() )*
	 * f15 -> ( Statement() )*
	 * f16 -> "}"
	 * f17 -> "}"
	 */
	// Pass the method to next points.
	public MType visit(MainClass n, MType argu) {
		classesTable = (MClasses) argu;
		MType _ret = null;
		String className = n.f1.accept(this, argu).symbolName;
		MClass Class = ((MClasses) argu).queryClass(className);
		MMethod method = Class.queryMethod("main");
		n.f14.accept(this, method);
		n.f15.accept(this, method);
		return _ret;
	}

	/**
	 * f0 -> "class"
	 * f1 -> Identifier()
	 * f2 -> "{"
	 * f3 -> ( VarDeclaration() )*
	 * f4 -> ( MethodDeclaration() )*
	 * f5 -> "}"
	 */
	// Pass the class to next points.
	public MType visit(ClassDeclaration n, MType argu) {
		MType _ret = null;
		String className = n.f1.accept(this, argu).symbolName;
		MClass Class = ((MClasses) argu).queryClass(className);
		n.f3.accept(this, Class);
		n.f4.accept(this, Class);
		return _ret;
	}

	/**
	 * f0 -> "class"
	 * f1 -> Identifier()
	 * f2 -> "extends"
	 * f3 -> Identifier()
	 * f4 -> "{"
	 * f5 -> ( VarDeclaration() )*
	 * f6 -> ( MethodDeclaration() )*
	 * f7 -> "}"
	 */
	// Check circle of extension.
	// Check if superClass exists.
	// Pass the class to next points.
	public MType visit(ClassExtendsDeclaration n, MType argu) {
		MType _ret = null;
		String className = n.f1.accept(this, argu).symbolName;
		MClass Class = ((MClasses) argu).queryClass(className);
		MType superClass = n.f3.accept(this, argu);
		if (getClass(new MClass(superClass), argu) == null) {
			ErrorMsg.addMsg(superClass.lineNo, superClass.colNo,
					"Superclass " + superClass.symbolName + " does not exist.");
			Class.superClassName = null;
		}

		MClass iter = Class;
		while (iter != null && iter.superClassName != null) {
			if (iter.superClassName.equals(Class.symbolName)) {
				iter.superClassName = null;
				ErrorMsg.addMsg(Class.lineNo, Class.colNo,
						"Class " + Class.symbolName + " is in a circle of extension.");
				break;
			}
			iter = classesTable.queryClass(iter.superClassName);
		}
		n.f5.accept(this, Class);
		n.f6.accept(this, Class);
		return _ret;
	}

	/**
	 * f0 -> "public"
	 * f1 -> Type()
	 * f2 -> Identifier()
	 * f3 -> "("
	 * f4 -> ( FormalParameterList() )?
	 * f5 -> ")"
	 * f6 -> "{"
	 * f7 -> ( VarDeclaration() )*
	 * f8 -> ( Statement() )*
	 * f9 -> "return"
	 * f10 -> Expression()
	 * f11 -> ";"
	 * f12 -> "}"
	 */
	// Check return type.
	// Check if the subClass overloads superClass.
	public MType visit(MethodDeclaration n, MType argu) {
		MType returnType = n.f1.accept(this, argu);
		MMethod method = ((MClass) argu).queryMethod(n.f2.accept(this, argu).symbolName);
		n.f7.accept(this, method);
		n.f8.accept(this, method);
		MType returnVal = n.f10.accept(this, method);
		if (!isMatched(returnVal, method, returnType.symbolName))
			ErrorMsg.addMsg(returnVal.lineNo, returnVal.colNo, "Return type does not match.");

		// overload superClass?
		if (((MClass) argu).superClassName == null)
			return null;
		MClass superClass = classesTable.queryClass(((MClass) argu).superClassName);
		if (superClass == null)
			return null;
		MMethod superMethod = superClass.queryMethod(method.symbolName);
		if (superMethod == null)
			return null;
		if (!method.returnType.equals(superMethod.returnType)) {
			ErrorMsg.addMsg(method.lineNo, method.colNo, "Overload is not allowed");
			return null;
		}
		int len = method.params.size();
		if (len != ((MMethod) superMethod).params.size()) {
			ErrorMsg.addMsg(method.lineNo, method.colNo, "Overload is not allowed");
			return null;
		}
		for (int i = 0; i < len; i++) {
			if (!method.params.elementAt(i).varType.equals(superMethod.params.elementAt(i).varType)) {
				ErrorMsg.addMsg(method.lineNo, method.colNo, "Overload is not allowed");
				return null;
			}
		}
		return null;
	}

	/**
	 * f0 -> ArrayType()
	 * | BooleanType()
	 * | IntegerType()
	 * | Identifier()
	 */
	// Check if classType not defined.
	public MType visit(Type n, MType argu) {
		MType _ret = n.f0.accept(this, argu);
		if (_ret.symbolName.equals("int") || _ret.symbolName.equals("boolean") || _ret.symbolName.equals("int[]"))
			return _ret;
		if (getClass(_ret, argu) == null)
			return null;
		return _ret;
	}

	/**
	 * f0 -> "int"
	 * f1 -> "["
	 * f2 -> "]"
	 */
	// return "int[]"
	public MType visit(ArrayType n, MType argu) {
		MType _ret = new MType("int[]", n.f0.beginLine, n.f0.beginColumn);
		return _ret;
	}

	/**
	 * f0 -> "boolean"
	 */
	// return "boolean"
	public MType visit(BooleanType n, MType argu) {
		MType _ret = new MType("boolean", n.f0.beginLine, n.f0.beginColumn);
		return _ret;
	}

	/**
	 * f0 -> "int"
	 */
	// return "int"
	public MType visit(IntegerType n, MType argu) {
		MType _ret = new MType("int", n.f0.beginLine, n.f0.beginColumn);
		return _ret;
	}

	/**
	 * f0 -> Identifier()
	 * f1 -> "="
	 * f2 -> Expression()
	 * f3 -> ";"
	 */
	// Check if left=right matches.
	public MType visit(AssignmentStatement n, MType argu) {
		MType _ret = null;
		MType left = n.f0.accept(this, argu);
		MType right = n.f2.accept(this, argu);
		checkMatched(left, right, argu, argu);
		return _ret;
	}

	/**
	 * f0 -> Identifier()
	 * f1 -> "["
	 * f2 -> Expression()
	 * f3 -> "]"
	 * f4 -> "="
	 * f5 -> Expression()
	 * f6 -> ";"
	 */
	// Check array.
	// Check if index is "int".
	// Check if right value is "int".
	public MType visit(ArrayAssignmentStatement n, MType argu) {
		MType _ret = null;
		MType array = n.f0.accept(this, argu);
		MType index = n.f2.accept(this, argu);
		MType assign = n.f5.accept(this, argu);
		checkArray(array, argu);
		checkInt(index, argu);
		checkInt(assign, argu);
		return _ret;
	}

	/**
	 * f0 -> "if"
	 * f1 -> "("
	 * f2 -> Expression()
	 * f3 -> ")"
	 * f4 -> Statement()
	 * f5 -> "else"
	 * f6 -> Statement()
	 */
	// Check if condition is "boolean".
	public MType visit(IfStatement n, MType argu) {
		MType _ret = null;
		MType a = n.f2.accept(this, argu);
		checkBool(a, argu);
		n.f4.accept(this, argu);
		n.f6.accept(this, argu);
		return _ret;
	}

	/**
	 * f0 -> "while"
	 * f1 -> "("
	 * f2 -> Expression()
	 * f3 -> ")"
	 * f4 -> Statement()
	 */
	// Check if condition is "boolean".
	public MType visit(WhileStatement n, MType argu) {
		MType _ret = null;
		MType a = n.f2.accept(this, argu);
		checkBool(a, argu);
		n.f4.accept(this, argu);
		return _ret;
	}

	/**
	 * f0 -> "System.out.println"
	 * f1 -> "("
	 * f2 -> Expression()
	 * f3 -> ")"
	 * f4 -> ";"
	 */
	// Check if expression is "int".
	public MType visit(PrintStatement n, MType argu) {
		MType _ret = null;
		MType a = n.f2.accept(this, argu);
		checkInt(a, argu);
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
	// Pass the expression type.
	public MType visit(Expression n, MType argu) {
		MType _ret = n.f0.accept(this, argu);
		return _ret;
	}

	/**
	 * f0 -> PrimaryExpression()
	 * f1 -> "&&"
	 * f2 -> PrimaryExpression()
	 */
	// Check if expression is "boolean".
	public MType visit(AndExpression n, MType argu) {
		MType _ret = null;
		MType a = n.f0.accept(this, argu);
		MType b = n.f2.accept(this, argu);
		checkBool(a, argu);
		checkBool(b, argu);
		_ret = new MType("boolean", a.lineNo, a.colNo);
		return _ret;
	}

	/**
	 * f0 -> PrimaryExpression()
	 * f1 -> "<"
	 * f2 -> PrimaryExpression()
	 */
	// Check if expression is "int".
	public MType visit(CompareExpression n, MType argu) {
		MType _ret = null;
		MType a = n.f0.accept(this, argu);
		MType b = n.f2.accept(this, argu);
		checkInt(a, argu);
		checkInt(b, argu);
		_ret = new MType("boolean", a.lineNo, a.colNo);
		return _ret;
	}

	/**
	 * f0 -> PrimaryExpression()
	 * f1 -> "+"
	 * f2 -> PrimaryExpression()
	 */
	// Check if expression is "int".
	public MType visit(PlusExpression n, MType argu) {
		MType _ret = null;
		MType a = n.f0.accept(this, argu);
		MType b = n.f2.accept(this, argu);
		checkInt(a, argu);
		checkInt(b, argu);
		_ret = new MType("int", a.lineNo, a.colNo);
		return _ret;
	}

	/**
	 * f0 -> PrimaryExpression()
	 * f1 -> "-"
	 * f2 -> PrimaryExpression()
	 */
	// Check if expression is "int".
	public MType visit(MinusExpression n, MType argu) {
		MType _ret = null;
		MType a = n.f0.accept(this, argu);
		MType b = n.f2.accept(this, argu);
		checkInt(a, argu);
		checkInt(b, argu);
		_ret = new MType("int", a.lineNo, a.colNo);
		return _ret;
	}

	/**
	 * f0 -> PrimaryExpression()
	 * f1 -> "*"
	 * f2 -> PrimaryExpression()
	 */
	// Check if expression is "int".
	public MType visit(TimesExpression n, MType argu) {
		MType _ret = null;
		MType a = n.f0.accept(this, argu);
		MType b = n.f2.accept(this, argu);
		checkInt(a, argu);
		checkInt(b, argu);
		_ret = new MType("int", a.lineNo, a.colNo);
		return _ret;
	}

	/**
	 * f0 -> PrimaryExpression()
	 * f1 -> "["
	 * f2 -> PrimaryExpression()
	 * f3 -> "]"
	 */
	// Check array.
	// Check if index is "int".
	public MType visit(ArrayLookup n, MType argu) {
		MType _ret = null;
		MType a = n.f0.accept(this, argu);
		MType b = n.f2.accept(this, argu);
		checkArray(a, argu);
		checkInt(b, argu);
		_ret = new MType("int", a.lineNo, a.colNo);
		return _ret;
	}

	/**
	 * f0 -> PrimaryExpression()
	 * f1 -> "."
	 * f2 -> "length"
	 */
	// Check array.
	public MType visit(ArrayLength n, MType argu) {
		MType _ret = null;
		MType a = n.f0.accept(this, argu);
		checkArray(a, argu);
		_ret = new MType("int", a.lineNo, a.colNo);
		return _ret;
	}

	/**
	 * f0 -> PrimaryExpression()
	 * f1 -> "."
	 * f2 -> Identifier()
	 * f3 -> "("
	 * f4 -> ( ExpressionList() )?
	 * f5 -> ")"
	 */
	// Check class.
	// Check method.
	// Check if the parameters match the original method declaration.
	public MType visit(MessageSend n, MType argu) {
		MType _ret = null;
		MClass Class = getClass(n.f0.accept(this, argu), argu);
		if (Class == null) {
			return null;
		}
		MType methodName = n.f2.accept(this, argu);
		MMethod method = getMethod(methodName, Class);
		if (method != null) {
			_ret = new MType(((MMethod) method).returnType, Class.lineNo, Class.colNo);
		} else {
			return null;
		}
		MType paramList = n.f4.accept(this, argu);
		int len = 0;
		if (paramList != null)
			len = ((MParamList) paramList).paramList.size();
		if (len != ((MMethod) method).params.size()) {
			ErrorMsg.addMsg(n.f3.beginLine, n.f3.beginColumn, "The number of parameters does not match.");
			return null;
		}
		for (int i = 0; i < len; i++) {
			MVariable defParam = ((MMethod) method).params.elementAt(i);
			MType hereParam = ((MParamList) paramList).paramList.elementAt(i);
			checkMatched(defParam, hereParam, method, argu);
		}
		return _ret;
	}

	/**
	 * f0 -> Expression()
	 * f1 -> ( ExpressionRest() )*
	 */
	// Make a MParamList and pass it to MessageSend and ExpressionRest.
	public MType visit(ExpressionList n, MType argu) {
		MParamList _ret = new MParamList(argu);
		MType param = n.f0.accept(this, argu);
		_ret.paramList.addElement(param);
		n.f1.accept(this, _ret);
		return _ret;
	}

	/**
	 * f0 -> ","
	 * f1 -> Expression()
	 */
	// Insert parameter into MParamList.
	public MType visit(ExpressionRest n, MType argu) {
		MType _ret = null;
		MType param = n.f1.accept(this, ((MParamList) argu).owner);
		((MParamList) argu).paramList.addElement(param);
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
	// Pass the MType of PrimaryExpression.
	public MType visit(PrimaryExpression n, MType argu) {
		MType _ret = n.f0.accept(this, argu);
		return _ret;
	}

	/**
	 * f0 -> <INTEGER_LITERAL>
	 */
	// return "int"
	public MType visit(IntegerLiteral n, MType argu) {
		MType _ret = new MType("int", n.f0.beginLine, n.f0.beginColumn);
		return _ret;
	}

	/**
	 * f0 -> "true"
	 */
	// return "boolean"
	public MType visit(TrueLiteral n, MType argu) {
		MType _ret = new MType("boolean", n.f0.beginLine, n.f0.beginColumn);
		return _ret;
	}

	/**
	 * f0 -> "false"
	 */
	// return "boolean"
	public MType visit(FalseLiteral n, MType argu) {
		MType _ret = new MType("boolean", n.f0.beginLine, n.f0.beginColumn);
		return _ret;
	}

	/**
	 * f0 -> <IDENTIFIER>
	 */
	// return Identifier
	public MType visit(Identifier n, MType argu) {
		MType _ret = new MType(n.f0.toString(), n.f0.beginLine, n.f0.beginColumn);
		return _ret;
	}

	/**
	 * f0 -> "this"
	 */
	// return the classType
	public MType visit(ThisExpression n, MType argu) {
		MType _ret = new MType(((MMethod) argu).owner.symbolName, n.f0.beginLine, n.f0.beginColumn);
		return _ret;
	}

	/**
	 * f0 -> "new"
	 * f1 -> "int"
	 * f2 -> "["
	 * f3 -> Expression()
	 * f4 -> "]"
	 */
	// return "int[]"
	// Check if index is "int".
	public MType visit(ArrayAllocationExpression n, MType argu) {
		MType index = n.f3.accept(this, argu);
		MType _ret = new MType("int[]", n.f0.beginLine, n.f0.beginColumn);
		checkInt(index, argu);
		return _ret;
	}

	/**
	 * f0 -> "new"
	 * f1 -> Identifier()
	 * f2 -> "("
	 * f3 -> ")"
	 */
	// return classType
	public MType visit(AllocationExpression n, MType argu) {
		MType _ret = n.f1.accept(this, argu);
		MClass Class = classesTable.queryClass(_ret.symbolName);// Class
		if (Class == null) {
			ErrorMsg.addMsg(_ret.lineNo, _ret.colNo, "The identifier " + _ret.symbolName + " is not a Class Type.");
			return null;
		}
		return _ret;
	}

	/**
	 * f0 -> "!"
	 * f1 -> Expression()
	 */
	// Check "boolean".
	// return "boolean"
	public MType visit(NotExpression n, MType argu) {
		MType _ret = n.f1.accept(this, argu);
		checkBool(_ret, argu);
		_ret = new MType("boolean", n.f0.beginLine, n.f0.beginColumn);
		return _ret;
	}

	/**
	 * f0 -> "("
	 * f1 -> Expression()
	 * f2 -> ")"
	 */
	// return the MType of expression
	public MType visit(BracketExpression n, MType argu) {
		MType _ret = n.f1.accept(this, argu);
		return _ret;
	}

}