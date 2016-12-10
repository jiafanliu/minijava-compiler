package minijava.typecheck;

import minijava.*;
import minijava.symboltable.*;
import minijava.syntaxtree.*;
import minijava.visitor.*;

/**
 * Type check for minijava.
 * 
 * @author jeff
 *
 */
public class Main {
	public static void main(String[] args) {
		try {
			/**
			 * usage:
			 * (default)args = "-v 0" : Print "Program type checked
			 * successfully" or "Type error".
			 * args = "-v 1" Print all error messages with line and column
			 * numbers.
			 * args = "-v 2" Print all error messages and all symbols in the
			 * symbol table with line and column numbers.
			 */
			if (args != null && args.length >= 2 && args[0].equals("-v")) {
				if (args[1].equals("0"))
					ErrorMsg.verbose = 0;
				else if (args[1].equals("1"))
					ErrorMsg.verbose = 1;
				else if (args[1].equals("2"))
					ErrorMsg.verbose = 2;
			}

			new MiniJavaParser(System.in);
			Node AST = MiniJavaParser.Goal();
			MClasses allClasses = new MClasses();
			// First pass: Make a symbol table.
			AST.accept(new MakeSymbolTable(), allClasses);
			// Second pass: Complete other type checks.
			AST.accept(new TypeCheck(), allClasses);

			if (ErrorMsg.verbose == 0) {
				if (ErrorMsg.errorMsgs.size() > 0)
					System.out.println("Type error");
				else
					System.out.println("Program type checked successfully");
			} else if (ErrorMsg.verbose == 1) {
				for (String msg : ErrorMsg.errorMsgs)
					System.out.println(msg);
			} else if (ErrorMsg.verbose == 2) {
				for (String msg : ErrorMsg.errorMsgs)
					System.out.println(msg);
				ErrorMsg.print(allClasses);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
