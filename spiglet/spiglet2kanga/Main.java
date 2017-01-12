package spiglet.spiglet2kanga;

import java.io.InputStream;
import java.util.*;

import spiglet.syntaxtree.*;
import spiglet.visitor.*;
import spiglet.*;

public class Main {
	public static InputStream input = System.in; 
	public static HashMap<String, Method> mMethod = new HashMap<String, Method>();
	public static HashMap<String, Integer> mLabel = new HashMap<String, Integer>();

	public static void main(String[] args) {
		try {
			new SpigletParser(input);
			Node AST = SpigletParser.Goal();
			// visit 1: Get Flow Graph Vertex
			AST.accept(new GetFlowGraphVertex());
			// visit 2: Get Flow Graph
			AST.accept(new GetFlowGraph());
			// Linear Scan Algorithm on Flow Graph
			new Temp2Reg().LinearScan();
			// visit 3: Spiglet->Kanga
			AST.accept(new Spiglet2Kanga());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
