package minijava.symboltable;

import java.util.*;

/**
 * The table of all classes.
 * 
 * @author jeff
 *
 */
public class MClasses extends MType {
	public Hashtable<String, MClass> classesTable = new Hashtable<String, MClass>();

	public boolean insertClass(MClass someClass) {
		if (classesTable.containsKey(someClass.symbolName)) {
			return false;
		} else {
			classesTable.put(someClass.symbolName, someClass);
			return true;
		}
	}

	public MClass queryClass(String className) {
		if (classesTable.containsKey(className))
			return classesTable.get(className);
		return null;
	}
}