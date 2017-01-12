package kanga.visitor;

import kanga.syntaxtree.*;
import kanga.kanga2mips.*;

/**
 * Compile Kanga code to MIPS code.
 * 
 * @author jeff
 *
 */

public class Kanga2MIPS extends GJNoArguDepthFirst<String> {

	// when StmtList ::= ( ( Label() )? Stmt() )*
	// should print Label
	public String visit(NodeOptional n) {
		if (n.present()) {
			MIPSOut.printLabel(n.node.accept(this));
		}
		return null;
	}

	/**
	 * f0 -> "MAIN"
	 * f1 -> "["
	 * f2 -> IntegerLiteral()
	 * f3 -> "]"
	 * f4 -> "["
	 * f5 -> IntegerLiteral()
	 * f6 -> "]"
	 * f7 -> "["
	 * f8 -> IntegerLiteral()
	 * f9 -> "]"
	 * f10 -> StmtList()
	 * f11 -> "END"
	 * f12 -> ( Procedure() )*
	 * f13 -> <EOF>
	 */
	int paramNum, stackNum, callParamNum; // about the 3 numbers in method[][][]

	public String visit(Goal n) {
		String _ret = null;
		paramNum = Integer.parseInt(n.f2.accept(this));
		paramNum = paramNum > 4 ? paramNum - 4 : 0;
		// 4 params using registers
		callParamNum = Integer.parseInt(n.f8.accept(this));
		callParamNum = callParamNum > 4 ? callParamNum - 4 : 0;
		stackNum = Integer.parseInt(n.f5.accept(this));
		stackNum = stackNum - paramNum + callParamNum + 2;
		// parameters of this method is stored above this stack frame
		// additional 2: $ra $fp
		String[] beginLines = { "sw $fp, -8($sp)", "sw $ra, -4($sp)", "move $fp, $sp",
				"subu $sp, $sp, " + 4 * stackNum };
		String[] endLines = { "lw $ra, -4($fp)", "lw $fp, -8($fp)", "addu $sp, $sp, " + 4 * stackNum, "j $ra" };

		MIPSOut.begin("main");
		for (String line : beginLines)
			MIPSOut.println(line);
		n.f10.accept(this);
		for (String line : endLines)
			MIPSOut.println(line);
		MIPSOut.end();

		// other methods
		n.f12.accept(this);

		// final
		String[] finalLines = { "", ".text", ".globl _halloc", "_halloc:", "li $v0, 9", "syscall", "j $ra", ".text",
				".globl _print", "_print:", "li $v0, 1", "syscall", "la $a0, newl", "li $v0, 4", "syscall", "j $ra",
				".data", ".align   0", "newl:", ".asciiz \"\\n\"", ".data", ".align   0", "str_er:",
				".asciiz \" ERROR: abnormal termination\\n\"" };
		for (String line : finalLines)
			MIPSOut.println("\t\t" + line);

		return _ret;
	}

	/**
	 * f0 -> Label()
	 * f1 -> "["
	 * f2 -> IntegerLiteral()
	 * f3 -> "]"
	 * f4 -> "["
	 * f5 -> IntegerLiteral()
	 * f6 -> "]"
	 * f7 -> "["
	 * f8 -> IntegerLiteral()
	 * f9 -> "]"
	 * f10 -> StmtList()
	 * f11 -> "END"
	 */
	public String visit(Procedure n) {
		String _ret = null;
		String method = n.f0.accept(this);
		paramNum = Integer.parseInt(n.f2.accept(this));
		paramNum = paramNum > 4 ? paramNum - 4 : 0;
		// 4 params using registers
		callParamNum = Integer.parseInt(n.f8.accept(this));
		callParamNum = callParamNum > 4 ? callParamNum - 4 : 0;
		stackNum = Integer.parseInt(n.f5.accept(this));
		stackNum = stackNum - paramNum + callParamNum + 2;
		// parameters of this method is stored above this stack frame
		// additional 2: $ra $fp
		String[] beginLines = { "sw $fp, -8($sp)", "sw $ra, -4($sp)", "move $fp, $sp",
				"subu $sp, $sp, " + 4 * stackNum };
		String[] endLines = { "lw $ra, -4($fp)", "lw $fp, -8($fp)", "addu $sp, $sp, " + 4 * stackNum, "j $ra" };

		MIPSOut.begin(method);
		for (String line : beginLines)
			MIPSOut.println(line);
		n.f10.accept(this);
		for (String line : endLines)
			MIPSOut.println(line);
		MIPSOut.end();
		return _ret;
	}

	/**
	 * f0 -> "NOOP"
	 */
	public String visit(NoOpStmt n) {
		String _ret = null;
		MIPSOut.println("nop");
		return _ret;
	}

	/**
	 * f0 -> "CJUMP"
	 * f1 -> Reg()
	 * f2 -> Label()
	 */
	public String visit(CJumpStmt n) {
		String _ret = null;
		String reg = n.f1.accept(this);
		String label = n.f2.accept(this);
		MIPSOut.println("beqz $" + reg + ", " + label);
		return _ret;
	}

	/**
	 * f0 -> "JUMP"
	 * f1 -> Label()
	 */
	public String visit(JumpStmt n) {
		String _ret = null;
		String label = n.f1.accept(this);
		MIPSOut.println("b " + label);
		return _ret;
	}

	/**
	 * f0 -> "HSTORE"
	 * f1 -> Reg()
	 * f2 -> IntegerLiteral()
	 * f3 -> Reg()
	 */
	public String visit(HStoreStmt n) {
		String _ret = null;
		String regTo = n.f1.accept(this);
		String offset = n.f2.accept(this);
		String regFrom = n.f3.accept(this);
		MIPSOut.println("sw $" + regFrom + ", " + offset + "($" + regTo + ")");
		return _ret;
	}

	/**
	 * f0 -> "HLOAD"
	 * f1 -> Reg()
	 * f2 -> Reg()
	 * f3 -> IntegerLiteral()
	 */
	public String visit(HLoadStmt n) {
		String _ret = null;
		String regTo = n.f1.accept(this);
		String regFrom = n.f2.accept(this);
		String offset = n.f3.accept(this);
		MIPSOut.println("lw $" + regTo + ", " + offset + "($" + regFrom + ")");
		return _ret;
	}

	/**
	 * f0 -> "MOVE"
	 * f1 -> Reg()
	 * f2 -> Exp()
	 */
	public String visit(MoveStmt n) {
		String _ret = null;
		String regTo = n.f1.accept(this);
		String regFrom = n.f2.accept(this);
		MIPSOut.println("move $" + regTo + ", $" + regFrom);
		return _ret;
	}

	/**
	 * f0 -> "PRINT"
	 * f1 -> SimpleExp()
	 */
	public String visit(PrintStmt n) {
		String _ret = null;
		String reg = n.f1.accept(this);
		MIPSOut.println("move $a0, $" + reg);
		MIPSOut.println("jal _print");
		return _ret;
	}

	/**
	 * f0 -> "ALOAD"
	 * f1 -> Reg()
	 * f2 -> SpilledArg()
	 */
	public String visit(ALoadStmt n) {
		String _ret = null;
		String regTo = n.f1.accept(this);
		String spilled = n.f2.accept(this);
		MIPSOut.println("lw $" + regTo + ", " + spilled);
		return _ret;
	}

	/**
	 * f0 -> "ASTORE"
	 * f1 -> SpilledArg()
	 * f2 -> Reg()
	 */
	public String visit(AStoreStmt n) {
		String _ret = null;
		String spilled = n.f1.accept(this);
		String regFrom = n.f2.accept(this);
		MIPSOut.println("sw $" + regFrom + ", " + spilled);
		return _ret;
	}

	/**
	 * f0 -> "PASSARG"
	 * f1 -> IntegerLiteral()
	 * f2 -> Reg()
	 */
	public String visit(PassArgStmt n) {
		String _ret = null;
		// PASSARG starts from 1
		int offset = Integer.parseInt(n.f1.accept(this)) - 1;
		String regFrom = n.f2.accept(this);
		MIPSOut.println("sw $" + regFrom + ", " + 4 * offset + "($sp)");
		return _ret;
	}

	/**
	 * f0 -> "CALL"
	 * f1 -> SimpleExp()
	 */
	public String visit(CallStmt n) {
		String _ret = null;
		String label = n.f1.accept(this);
		MIPSOut.println("jalr $" + label);
		return _ret;
	}

	/**
	 * f0 -> HAllocate()
	 * | BinOp()
	 * | SimpleExp()
	 */
	public String visit(Exp n) {
		String _ret = n.f0.accept(this);
		return _ret;
	}

	/**
	 * f0 -> "HALLOCATE"
	 * f1 -> SimpleExp()
	 */
	public String visit(HAllocate n) {
		String _ret = "v0";
		String reg = n.f1.accept(this);
		MIPSOut.println("move $a0, $" + reg);
		MIPSOut.println("jal _halloc");
		return _ret;
	}

	/**
	 * f0 -> Operator()
	 * f1 -> Reg()
	 * f2 -> SimpleExp()
	 */
	public String visit(BinOp n) {
		String _ret = "v1";
		String op = n.f0.accept(this);
		String reg1 = n.f1.accept(this);
		String reg2 = n.f2.accept(this);
		MIPSOut.println(op + " $v1, $" + reg1 + ", $" + reg2);
		return _ret;
	}

	/**
	 * f0 -> "LT"
	 * | "PLUS"
	 * | "MINUS"
	 * | "TIMES"
	 */
	public String visit(Operator n) {
		String[] retValue = { "slt", "add", "sub", "mul" };
		String _ret = retValue[n.f0.which];
		return _ret;
	}

	/**
	 * f0 -> "SPILLEDARG"
	 * f1 -> IntegerLiteral()
	 */
	public String visit(SpilledArg n) {
		String _ret = null;
		int idx = Integer.parseInt(n.f1.accept(this));
		// SpilledArg starts from 0

		if (idx >= paramNum) {
			// is not parameter
			// is spilled register/saved register
			idx = paramNum - idx - 3;// below $fp [$ra] [$fp]
		}

		_ret = 4 * idx + "($fp)";
		return _ret;
	}

	/**
	 * f0 -> Reg()
	 * | IntegerLiteral()
	 * | Label()
	 */
	// returns a simple register
	public String visit(SimpleExp n) {
		String _ret = "v1";
		String str = n.f0.accept(this);
		if (n.f0.which == 0) {
			_ret = str;
		} else if (n.f0.which == 1) {
			MIPSOut.println("li $v1, " + str);
		} else {
			MIPSOut.println("la $v1, " + str);
		}
		return _ret;
	}

	/**
	 * f0 -> "a0"
	 * | "a1"
	 * | "a2"
	 * | "a3"
	 * | "t0"
	 * | "t1"
	 * | "t2"
	 * | "t3"
	 * | "t4"
	 * | "t5"
	 * | "t6"
	 * | "t7"
	 * | "s0"
	 * | "s1"
	 * | "s2"
	 * | "s3"
	 * | "s4"
	 * | "s5"
	 * | "s6"
	 * | "s7"
	 * | "t8"
	 * | "t9"
	 * | "v0"
	 * | "v1"
	 */
	public String visit(Reg n) {
		String[] retValue = { "a0", "a1", "a2", "a3", "t0", "t1", "t2", "t3", "t4", "t5", "t6", "t7", "s0", "s1", "s2",
				"s3", "s4", "s5", "s6", "s7", "t8", "t9", "v0", "v1" };
		String _ret = retValue[n.f0.which];
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
