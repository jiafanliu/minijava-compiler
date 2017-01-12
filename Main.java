import java.io.*;

public class Main {
	public static void main(String[] args) {
		PrintStream old = System.out;

		// minijava to piglet
		ByteArrayOutputStream pigletOut = new ByteArrayOutputStream();
		System.setOut(new PrintStream(pigletOut));
		minijava.minijava2piglet.Main.main(null);

		// piglet to spiglet
		piglet.piglet2spiglet.Main.input = new ByteArrayInputStream(pigletOut.toByteArray());
		ByteArrayOutputStream spigletOut = new ByteArrayOutputStream();
		System.setOut(new PrintStream(spigletOut));
		piglet.piglet2spiglet.Main.main(null);

		// spiglet to kanga
		spiglet.spiglet2kanga.Main.input = new ByteArrayInputStream(spigletOut.toByteArray());
		ByteArrayOutputStream kangaOut = new ByteArrayOutputStream();
		System.setOut(new PrintStream(kangaOut));
		spiglet.spiglet2kanga.Main.main(null);

		// kanga to mips
		kanga.kanga2mips.Main.input = new ByteArrayInputStream(kangaOut.toByteArray());
		System.setOut(old);
		kanga.kanga2mips.Main.main(null);

	}

}
