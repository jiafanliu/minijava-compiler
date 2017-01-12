package spiglet.visitor;

import java.util.*;
import spiglet.syntaxtree.*;
import spiglet.spiglet2kanga.*;

public class Spiglet2Kanga extends GJNoArguDepthFirst<String> {
	HashMap<String, Method> mMethod = Main.mMethod;
	Method currMethod;

	String getNewLabel(String labelName) {
		return labelName + "_" + currMethod.methodName;
	}

	// tempName->regName
	// if spilled, load tempName in regName
	String temp2Reg(String regName, String tempName) {
		if (currMethod.regT.containsKey(tempName)) {
			return currMethod.regT.get(tempName);
		} else if (currMethod.regS.containsKey(tempName)) {
			return currMethod.regS.get(tempName);
		} else {
			/*
			System.out.println("*****" + tempName);
			for (String i : currMethod.regT.keySet())
				System.out.println("****" + i);
			for (String i : currMethod.regS.keySet())
				System.out.println("***" + i);
			for (String i : currMethod.regSpilled.keySet())
				System.out.println("**" + i);
			*/
			// spilled
			System.out.printf("\t\tALOAD %s %s\n", regName, currMethod.regSpilled.get(tempName));
			return regName;
		}
	}

	// MOVE tempName exp
	// if spilled, store in regSpilled
	void moveToTemp(String tempName, String exp) {
		if (currMethod.regSpilled.containsKey(tempName)) {
			System.out.printf("\t\tMOVE v0 %s\n", exp);
			System.out.printf("\t\tASTORE %s v0\n", currMethod.regSpilled.get(tempName));
		} else {
			tempName = temp2Reg("", tempName);
			if (!tempName.equals(exp))
				System.out.printf("\t\tMOVE %s %s\n", tempName, exp);
		}
	}

	// StmtList ::= ( (Label)?Stmt)*
	// get Labels
	public String visit(NodeOptional n) {
		if (n.present()) // print new label
			System.out.print(getNewLabel(n.node.accept(this)));
		return null;
	}

	/**
	 * f0 -> "MAIN"
	 * f1 -> StmtList()
	 * f2 -> "END"
	 * f3 -> ( Procedure() )*
	 * f4 -> <EOF>
	 */
	public String visit(Goal n) {
		currMethod = mMethod.get("MAIN");
		System.out.printf("MAIN [%d][%d][%d]\n", currMethod.paramNum, currMethod.stackNum, currMethod.callParamNum);
		n.f1.accept(this);
		System.out.println("END");
		n.f3.accept(this);
		return null;
	}

	/**
	 * f0 -> Label()
	 * f1 -> "["
	 * f2 -> IntegerLiteral()
	 * f3 -> "]"
	 * f4 -> StmtExp()
	 */
	public String visit(Procedure n) {
		String methodName = n.f0.accept(this);
		currMethod = mMethod.get(methodName);
		System.out.printf("\n%s [%d][%d][%d]\n", methodName, currMethod.paramNum, currMethod.stackNum,
				currMethod.callParamNum);
		n.f4.accept(this);
		return null;
	}

	/**
	 * f0 -> "NOOP"
	 */
	public String visit(NoOpStmt n) {
		System.out.println("\t\tNOOP");
		return null;
	}

	/**
	 * f0 -> "ERROR"
	 */
	public String visit(ErrorStmt n) {
		System.out.println("\t\tERROR");
		return null;
	}

	/**
	 * f0 -> "CJUMP"
	 * f1 -> Temp()
	 * f2 -> Label()
	 */
	public String visit(CJumpStmt n) {
		System.out.printf("\t\tCJUMP %s %s\n", temp2Reg("v0", n.f1.accept(this)), getNewLabel(n.f2.accept(this)));
		return null;
	}

	/**
	 * f0 -> "JUMP"
	 * f1 -> Label()
	 */
	public String visit(JumpStmt n) {
		System.out.printf("\t\tJUMP %s\n", getNewLabel(n.f1.accept(this)));
		return null;
	}

	/**
	 * f0 -> "HSTORE"
	 * f1 -> Temp()
	 * f2 -> IntegerLiteral()
	 * f3 -> Temp()
	 */
	public String visit(HStoreStmt n) {
		System.out.printf("\t\tHSTORE %s %s %s\n", temp2Reg("v0", n.f1.accept(this)), n.f2.accept(this),
				temp2Reg("v1", n.f3.accept(this)));
		return null;
	}

	/**
	 * f0 -> "HLOAD"
	 * f1 -> Temp()
	 * f2 -> Temp()
	 * f3 -> IntegerLiteral()
	 */
	public String visit(HLoadStmt n) {
		String tempTo = n.f1.accept(this);
		String regFrom = temp2Reg("v1", n.f2.accept(this));
		String offset = n.f3.accept(this);
		if (currMethod.regSpilled.containsKey(tempTo)) {
			System.out.printf("\t\tHLOAD v1 %s %s\n", regFrom, offset);
			moveToTemp(tempTo, "v1");
		} else {
			System.out.printf("\t\tHLOAD %s %s %s\n", temp2Reg("", tempTo), regFrom, offset);
		}
		return null;
	}

	/**
	 * f0 -> "MOVE"
	 * f1 -> Temp()
	 * f2 -> Exp()
	 */
	public String visit(MoveStmt n) {
		moveToTemp(n.f1.accept(this), n.f2.accept(this));
		return null;
	}

	/**
	 * f0 -> "PRINT"
	 * f1 -> SimpleExp()
	 */
	public String visit(PrintStmt n) {
		System.out.printf("\t\tPRINT %s\n", n.f1.accept(this));
		return null;
	}

	/**
	 * f0 -> Call() | HAllocate() | BinOp() | SimpleExp()
	 */
	public String visit(Exp n) {
		return n.f0.accept(this);
	}

	/**
	 * f0 -> "BEGIN"
	 * f1 -> StmtList()
	 * f2 -> "RETURN"
	 * f3 -> SimpleExp()
	 * f4 -> "END"
	 */
	public String visit(StmtExp n) {
		int stackIdx = currMethod.paramNum > 4 ? currMethod.paramNum - 4 : 0;
		// store callee-saved S
		if (currMethod.regS.size() != 0) {
			for (int idx = stackIdx; idx < stackIdx + currMethod.regS.size(); idx++) {
				if (idx - stackIdx > 7)
					break;
				System.out.println("\t\tASTORE SPILLEDARG " + idx + " s" + (idx - stackIdx));
			}
		}
		// move params regA to TEMP
		for (stackIdx = 0; stackIdx < currMethod.paramNum && stackIdx < 4; stackIdx++)
			if (currMethod.mTemp.containsKey(stackIdx))
				moveToTemp("TEMP " + stackIdx, "a" + stackIdx);
		// load params(>4)
		for (; stackIdx < currMethod.paramNum; stackIdx++) {
			String tempName = "TEMP " + stackIdx;
			if (currMethod.mTemp.containsKey(stackIdx)) {
				if (currMethod.regSpilled.containsKey(tempName)) {
					System.out.printf("\t\tALOAD v0 SPILLEDARG %d\n", stackIdx - 4);
					moveToTemp(tempName, "v0");
				} else {
					System.out.printf("\t\tALOAD %s SPILLEDARG %d\n", temp2Reg("", tempName), stackIdx - 4);
				}
			}
		}

		n.f1.accept(this);
		// v0 stores returnValue
		System.out.println("\t\tMOVE v0 " + n.f3.accept(this));

		// restore callee-saved S
		stackIdx = currMethod.paramNum > 4 ? currMethod.paramNum - 4 : 0;
		if (currMethod.regS.size() != 0) {
			for (int j = stackIdx; j < stackIdx + currMethod.regS.size(); j++) {
				if (j - stackIdx > 7)
					break;
				System.out.println("\t\tALOAD s" + (j - stackIdx) + " SPILLEDARG " + j);
			}
		}

		System.out.println("END");
		return null;
	}

	/**
	 * f0 -> "CALL"
	 * f1 -> SimpleExp()
	 * f2 -> "("
	 * f3 -> ( Temp() )*
	 * f4 -> ")"
	 */
	public String visit(Call n) {
		Vector<Node> vTemp = n.f3.nodes;
		int nParam = vTemp.size();
		int paramIdx;
		// pass params
		for (paramIdx = 0; paramIdx < nParam && paramIdx < 4; paramIdx++)
			System.out.printf("\t\tMOVE a%d %s\n", paramIdx, temp2Reg("v0", vTemp.get(paramIdx).accept(this)));
		for (; paramIdx < nParam; paramIdx++)
			System.out.printf("\t\tPASSARG %d %s\n", paramIdx - 3, temp2Reg("v0", vTemp.get(paramIdx).accept(this)));
		// call
		System.out.printf("\t\tCALL %s\n", n.f1.accept(this));
		return "v0";
	}

	/**
	 * f0 -> "HALLOCATE"
	 * f1 -> SimpleExp()
	 */
	public String visit(HAllocate n) {
		return "HALLOCATE " + n.f1.accept(this);
	}

	/**
	 * f0 -> Operator()
	 * f1 -> Temp()
	 * f2 -> SimpleExp()
	 */
	public String visit(BinOp n) {
		return n.f0.accept(this) + temp2Reg("v0", n.f1.accept(this)) + " " + n.f2.accept(this);
	}

	/**
	 * f0 -> "LT" | "PLUS" | "MINUS" | "TIMES"
	 */
	public String visit(Operator n) {
		String[] _ret = { "LT ", "PLUS ", "MINUS ", "TIMES " };
		return _ret[n.f0.which];
	}

	/**
	 * f0 -> Temp() | IntegerLiteral() | Label()
	 */
	public String visit(SimpleExp n) {
		String _ret = n.f0.accept(this);
		if (n.f0.which == 0)
			_ret = temp2Reg("v1", _ret);
		return _ret;
	}

	/**
	 * f0 -> "TEMP"
	 * f1 -> IntegerLiteral()
	 */
	public String visit(Temp n) {
		return "TEMP " + n.f1.accept(this);
	}

	/**
	 * f0 -> <INTEGER_LITERAL>
	 */
	public String visit(IntegerLiteral n) {
		return n.f0.toString();
	}

	/**
	 * f0 -> <IDENTIFIER>
	 */
	public String visit(Label n) {
		return n.f0.toString();
	}

}
