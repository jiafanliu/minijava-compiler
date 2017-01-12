package minijava.minijava2piglet;

import minijava.*;
import java.io.*;

import minijava.symboltable.*;
import minijava.syntaxtree.*;
import minijava.typecheck.*;
import minijava.visitor.*;

/**
 * Compile Minijava code to Piglet code.
 * 
 * @author jeff
 *
 */

public class Main {
	public static InputStream input = System.in;

	public static void main(String[] args) {
		try {
			new MiniJavaParser(input);
			Node AST = MiniJavaParser.Goal();
			MClasses allClasses = new MClasses();
			// First pass: Make a symbol table.
			AST.accept(new MakeSymbolTable(), allClasses);
			// Second pass: Complete other type checks.
			AST.accept(new TypeCheck(), allClasses);
			if (ErrorMsg.errorMsgs.size() > 0)
				System.out.println("Type error");
			else {
				// Third pass: Compile minijava code to piglet code
				AST.accept(new Minijava2Piglet(), allClasses);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
