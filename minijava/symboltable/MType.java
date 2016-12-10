package minijava.symboltable;

/**
 * All classes in the symbol table extends this class. Represent a symbol with
 * its name and location.
 * 
 * @author jeff
 *
 */
public class MType {
	public String symbolName;
	public int lineNo, colNo;

	public MType() {
	}

	public MType(String _symbolName, int _lineNo, int _colNo) {
		symbolName = _symbolName;
		lineNo = _lineNo;
		colNo = _colNo;
	}

	public MType(MType another) {
		symbolName = another.symbolName;
		lineNo = another.lineNo;
		colNo = another.colNo;
	}

}