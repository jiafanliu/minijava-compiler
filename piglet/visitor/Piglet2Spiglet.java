package piglet.visitor;

import java.util.Enumeration;

import piglet.piglet2spiglet.SpigletOut;
import piglet.syntaxtree.*;

public class Piglet2Spiglet extends GJNoArguDepthFirst<String> {

	int tempNo = GetMaxTempNo.maxTempNo + 1;

	// when Call ::= "CALL" Exp "(" ( Exp )* ")"
	// let ( Exp )* returns ( String )*
	public String visit(NodeListOptional n) {
		if (n.present()) {
			String _ret = "";
			for (Enumeration<Node> e = n.elements(); e.hasMoreElements();) {
				_ret += " " + e.nextElement().accept(this);
			}
			return _ret;
		} else
			return null;
	}

	// when StmtList ::= ( ( Label() )? Stmt() )*
	// should print Label
	public String visit(NodeOptional n) {
		if (n.present()) {
			SpigletOut.printLabel(n.node.accept(this));
		}
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
		String _ret = null;
		SpigletOut.beginMain();
		n.f1.accept(this);
		SpigletOut.end();
		SpigletOut.println();
		n.f3.accept(this);
		return _ret;
	}

	/**
	 * f0 -> Label()
	 * f1 -> "["
	 * f2 -> IntegerLiteral()
	 * f3 -> "]"
	 * f4 -> StmtExp()
	 */
	public String visit(Procedure n) {
		String _ret = null;
		SpigletOut.println(n.f0.accept(this) + " [ " + n.f2.accept(this) + " ] ");
		SpigletOut.begin();
		SpigletOut.println("RETURN " + n.f4.accept(this));
		SpigletOut.end();
		SpigletOut.println();
		return _ret;
	}

	/**
	 * f0 -> "NOOP"
	 */
	public String visit(NoOpStmt n) {
		String _ret = null;
		SpigletOut.println("NOOP");
		return _ret;
	}

	/**
	 * f0 -> "ERROR"
	 */
	public String visit(ErrorStmt n) {
		String _ret = null;
		SpigletOut.println("ERROR");
		return _ret;
	}

	/**
	 * f0 -> "CJUMP"
	 * f1 -> Exp()
	 * f2 -> Label()
	 */
	public String visit(CJumpStmt n) {
		String _ret = null;
		SpigletOut.println("CJUMP " + n.f1.accept(this) + " " + n.f2.accept(this));
		return _ret;
	}

	/**
	 * f0 -> "JUMP"
	 * f1 -> Label()
	 */
	public String visit(JumpStmt n) {
		String _ret = null;
		SpigletOut.println("JUMP " + n.f1.accept(this));
		return _ret;
	}

	/**
	 * f0 -> "HSTORE"
	 * f1 -> Exp()
	 * f2 -> IntegerLiteral()
	 * f3 -> Exp()
	 */
	public String visit(HStoreStmt n) {
		String _ret = null;
		SpigletOut.println("HSTORE " + n.f1.accept(this) + " " + n.f2.accept(this) + " " + n.f3.accept(this));
		return _ret;
	}

	/**
	 * f0 -> "HLOAD"
	 * f1 -> Temp()
	 * f2 -> Exp()
	 * f3 -> IntegerLiteral()
	 */
	public String visit(HLoadStmt n) {
		String _ret = null;
		SpigletOut.println("HLOAD " + n.f1.accept(this) + " " + n.f2.accept(this) + " " + n.f3.accept(this));
		return _ret;
	}

	/**
	 * f0 -> "MOVE"
	 * f1 -> Temp()
	 * f2 -> Exp()
	 */
	public String visit(MoveStmt n) {
		String _ret = null;
		SpigletOut.println("MOVE " + n.f1.accept(this) + " " + n.f2.accept(this));
		return _ret;
	}

	/**
	 * f0 -> "PRINT"
	 * f1 -> Exp()
	 */
	public String visit(PrintStmt n) {
		String _ret = null;
		SpigletOut.println("PRINT " + n.f1.accept(this));
		return _ret;
	}

	/**
	 * f0 -> StmtExp()
	 * | Call()
	 * | HAllocate()
	 * | BinOp()
	 * | Temp()
	 * | IntegerLiteral()
	 * | Label()
	 */
	// MOVE the Exp to a TEMP tempExp
	// return TEMP
	public String visit(Exp n) {
		int tempExp = (tempNo++);
		String _ret = "TEMP " + tempExp;
		SpigletOut.println("MOVE TEMP " + tempExp + " " + n.f0.accept(this));
		return _ret;
	}

	/**
	 * f0 -> "BEGIN"
	 * f1 -> StmtList()
	 * f2 -> "RETURN"
	 * f3 -> Exp()
	 * f4 -> "END"
	 */
	// execute StmtList()
	// return TEMP of Exp()
	public String visit(StmtExp n) {
		String _ret = null;
		n.f1.accept(this);
		_ret = n.f3.accept(this);
		return _ret;
	}

	/**
	 * f0 -> "CALL"
	 * f1 -> Exp()
	 * f2 -> "("
	 * f3 -> ( Exp() )*
	 * f4 -> ")"
	 */
	public String visit(Call n) {
		String _ret = "CALL " + n.f1.accept(this) + " (" + n.f3.accept(this) + " )";
		return _ret;
	}

	/**
	 * f0 -> "HALLOCATE"
	 * f1 -> Exp()
	 */
	public String visit(HAllocate n) {
		String _ret = "HALLOCATE " + n.f1.accept(this);
		return _ret;
	}

	/**
	 * f0 -> Operator()
	 * f1 -> Exp()
	 * f2 -> Exp()
	 */
	public String visit(BinOp n) {
		String _ret = n.f0.accept(this) + " " + n.f1.accept(this) + " " + n.f2.accept(this);
		return _ret;
	}

	/**
	 * f0 -> "LT"
	 * | "PLUS"
	 * | "MINUS"
	 * | "TIMES"
	 */
	public String visit(Operator n) {
		String[] retValue = { "LT", "PLUS", "MINUS", "TIMES" };
		String _ret = retValue[n.f0.which];
		return _ret;
	}

	/**
	 * f0 -> "TEMP"
	 * f1 -> IntegerLiteral()
	 */
	public String visit(Temp n) {
		String _ret = "TEMP " + n.f1.accept(this);
		return _ret;
	}

	/**
	 * f0 -> <INTEGER_LITERAL>
	 */
	public String visit(IntegerLiteral n) {
		String _ret = n.f0.toString();
		return _ret;
	}

	/**
	 * f0 -> <IDENTIFIER>
	 */
	public String visit(Label n) {
		String _ret = n.f0.toString();
		return _ret;
	}

}
