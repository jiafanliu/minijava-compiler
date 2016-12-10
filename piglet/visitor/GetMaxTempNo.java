package piglet.visitor;

import piglet.syntaxtree.Temp;

public class GetMaxTempNo extends DepthFirstVisitor {
	static int maxTempNo = 0;

	/**
	 * f0 -> "TEMP"
	 * f1 -> IntegerLiteral()
	 */
	public void visit(Temp n) {
		n.f0.accept(this);
		int tempNo = Integer.parseInt(n.f1.f0.toString());
		maxTempNo = Math.max(maxTempNo, tempNo);
		n.f1.accept(this);
	}
}
