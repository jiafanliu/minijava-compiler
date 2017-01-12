package kanga.kanga2mips;

import java.io.InputStream;

/**
 * Compile Kanga code to MIPS code.
 * 
 * @author jeff
 *
 */
import kanga.*;
import kanga.syntaxtree.*;
import kanga.visitor.*;

public class Main {

	public static InputStream input = System.in;

	public static void main(String[] args) {
		try {
			new KangaParser(input);
			Node AST = KangaParser.Goal();
			// Kanga to MIPS
			AST.accept(new Kanga2MIPS());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
