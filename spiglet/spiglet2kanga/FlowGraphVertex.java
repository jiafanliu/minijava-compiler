package spiglet.spiglet2kanga;

import java.util.*;

public class FlowGraphVertex {
	public int vid;

	// Predecessor, Successor
	public HashSet<FlowGraphVertex> Pred = new HashSet<FlowGraphVertex>();
	public HashSet<FlowGraphVertex> Succ = new HashSet<FlowGraphVertex>();
	// Define x = ...
	// Use ... = x
	public HashSet<Integer> Def = new HashSet<Integer>();
	public HashSet<Integer> Use = new HashSet<Integer>();
	public HashSet<Integer> In = new HashSet<Integer>();
	public HashSet<Integer> Out = new HashSet<Integer>();

	public FlowGraphVertex(int vid) {
		this.vid = vid;
	}

}
