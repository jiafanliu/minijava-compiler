package spiglet.visitor;

import java.util.*;
import spiglet.syntaxtree.*;
import spiglet.spiglet2kanga.*;

public class GetFlowGraphVertex extends GJNoArguDepthFirst<String> {
	// methodName -> Method
	HashMap<String, Method> mMethod = Main.mMethod;
	// labelName -> vid
	HashMap<String, Integer> mLabel = Main.mLabel;
	Method currMethod;
	int vid = 0;

	// StmtList ::= ( (Label)?Stmt)*
	// get Labels
	public String visit(NodeOptional n) {
		if (n.present()) {
			mLabel.put(n.node.accept(this), vid);
		}
		return null;
	}

	/**
	 * f0 -> "MAIN"
	 * f1 -> StmtList()
	 * f2 -> "END"
	 * f3 -> ( Procedure() )*
	 * f4 -> <EOF>
	 */
	public String visit(Goal n) {
		currMethod = new Method("MAIN", 0);
		mMethod.put("MAIN", currMethod);
		vid = 0;
		// begin
		currMethod.flowGraph.addVertex(vid);
		vid++;
		n.f1.accept(this);
		// end
		currMethod.flowGraph.addVertex(vid);
		n.f3.accept(this);
		return null;
	}

	/**
	 * f0 -> Label()
	 * f1 -> "["
	 * f2 -> IntegerLiteral()
	 * f3 -> "]"
	 * f4 -> StmtExp()
	 */
	public String visit(Procedure n) {
		vid = 0;
		String methodName = n.f0.f0.toString();
		int paramNum = Integer.parseInt(n.f2.accept(this));
		currMethod = new Method(methodName, paramNum);
		mMethod.put(methodName, currMethod);
		n.f4.accept(this);
		return null;
	}

	/**
	 * f0 -> NoOpStmt()
	 * | ErrorStmt()
	 * | CJumpStmt()
	 * | JumpStmt()
	 * | HStoreStmt()
	 * | HLoadStmt()
	 * | MoveStmt()
	 * | PrintStmt()
	 */
	public String visit(Stmt n) {
		// Every Statement -> Vertex
		currMethod.flowGraph.addVertex(vid);
		n.f0.accept(this);
		vid++;
		return null;
	}

	/**
	 * f0 -> "CALL"
	 * f1 -> SimpleExp()
	 * f2 -> "("
	 * f3 -> ( Temp() )*
	 * f4 -> ")"
	 */
	public String visit(Call n) {
		n.f1.accept(this);
		n.f3.accept(this);
		currMethod.flowGraph.callPos.add(vid);
		// callParamNum uses the MAX
		if (currMethod.callParamNum < n.f3.size())
			currMethod.callParamNum = n.f3.size();
		return null;
	}

	/**
	 * f0 -> "BEGIN"
	 * f1 -> StmtList()
	 * f2 -> "RETURN"
	 * f3 -> SimpleExp()
	 * f4 -> "END"
	 */
	public String visit(StmtExp n) {
		// begin
		currMethod.flowGraph.addVertex(vid);
		vid++;
		n.f1.accept(this);
		// return
		currMethod.flowGraph.addVertex(vid);
		vid++;
		n.f3.accept(this);
		// end
		currMethod.flowGraph.addVertex(vid);
		return null;
	}

	/**
	 * f0 -> "TEMP"
	 * f1 -> IntegerLiteral()
	 */
	public String visit(Temp n) {
		Integer tempNo = Integer.parseInt(n.f1.accept(this));
		if (!currMethod.mTemp.containsKey(tempNo)) {
			if (tempNo < currMethod.paramNum)
				// parameter
				currMethod.mTemp.put(tempNo, new LiveInterval(tempNo, 0, vid));
			else
				// local Temp (first shows up at vid)
				currMethod.mTemp.put(tempNo, new LiveInterval(tempNo, vid, vid));
		}
		return (tempNo).toString();
	}

	/**
	 * f0 -> <INTEGER_LITERAL>
	 */
	public String visit(IntegerLiteral n) {
		return n.f0.toString();
	}

	/**
	 * f0 -> <IDENTIFIER>
	 */
	public String visit(Label n) {
		return n.f0.toString();
	}

}
