package kanga.kanga2mips;

/**
 * Print formatted MIPS code.
 * 
 * @author jeff
 *
 */
public class MIPSOut {
	public static int indent = 0;
	static boolean newLine = true;

	public static void print(String s) {
		if (newLine) {
			for (int i = 0; i < indent; i++)
				System.out.print("\t");
			newLine = false;
		}
		System.out.print(s + " ");
	}

	public static void printLabel(String s) {
		System.out.print(s + ":");
	}

	public static void println(String s) {
		if (newLine) {
			for (int i = 0; i < indent; i++)
				System.out.print("\t");
		}
		System.out.println(s);
		newLine = true;
	}

	public static void println() {
		System.out.println();
		newLine = true;
	}

	public static void begin(String method) {
		indent = 2;
		println(".text");
		println(".globl " + method);
		indent = 0;
		println(method + ":");
		indent = 2;
	}

	public static void end() {
		indent = 0;
		println();
	}
}
