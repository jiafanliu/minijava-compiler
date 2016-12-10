package minijava.symboltable;

/**
 * The info of a variable in a class or a method.
 * 
 * @author jeff
 *
 */
public class MVariable extends MType {
	public String varType;
	public MType owner;
	public boolean init;

	public MVariable(String varType, MType varID, MType owner, boolean init) {
		super(varID);
		this.varType = varType;
		this.owner = owner;
		this.init = init;
	}

	public MVariable(MType another) {
		super(another);
	}
}
