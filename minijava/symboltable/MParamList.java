package minijava.symboltable;

import java.util.*;

/**
 * The list of all parameters in a method.
 * 
 * @author jeff
 *
 */
public class MParamList extends MType {
	public Vector<MType> paramList = new Vector<MType>();
	public MType owner;

	public MParamList(MType owner) {
		this.owner = owner;
	}
}
