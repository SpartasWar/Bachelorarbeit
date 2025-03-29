/* 
 * Copyright (c) 2013-2014, H.J. Sander Bruggink, University of Duisburg-Essen
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *   - Neither the names of its copyright holder(s) nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package de.uni_due.inf.ti.graph.io;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;

import de.uni_due.inf.ti.graph.condition.cospan.Quantifier;
import de.uni_due.inf.ti.util.StringUtils;

class SgfTerm {
	protected int line;
	protected int column;

	private SgfTerm() {
		line = -1;
		column = -1;
	}

	private SgfTerm(int line, int column) {
		this.line = line;
		this.column = column;
	}

	private SgfTerm(Token firstToken) {
		this.line = firstToken.beginLine;
		this.column = firstToken.beginColumn;
	}

	public int getLine() {
		return line;
	}

	public int getColumn() {
		return column;
	}

	/* ====================================================================== */

	static class NamedTerm extends SgfTerm {
		private String name;

		private NamedTerm(int line, int column, String name) {
			super(line, column);
			this.name = name;
		}

		private NamedTerm(Token firstToken, String name) {
			super(firstToken);
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	/* ====================================================================== */

	static class LetTerm extends NamedTerm {
		private SgfTerm value;

		private LetTerm(int l, int c, String name, SgfTerm value) {
			super(l, c, name);
			this.value = value;
		}

		public SgfTerm getValue() {
			return value;
		}

		@Override
		public String toString() {
			return String.format("%s = %s", getName(), value.toString());
		}
	}

	public static LetTerm createLetTerm(Token firstToken, String name, SgfTerm value) {
		return new LetTerm(firstToken.beginLine, firstToken.beginColumn, name, value);
	}

	public static LetTerm createLetTerm(int l, int c, String name, SgfTerm value) {
		return new LetTerm(l, c, name, value);
	}

	/* ====================================================================== */

	static class NodeDefTerm extends NamedTerm {
		private NodeDefTerm(Token firstToken, String name) {
			super(firstToken, name);
		}

		@Override
		public String toString() {
			return String.format("node %s", getName());
		}
	}

	public static NodeDefTerm createNodeDefTerm(Token firstToken, String name) {
		return new NodeDefTerm(firstToken, name);
	}

	/* ====================================================================== */

	static class EdgeDefTerm extends NamedTerm {
		private String label;
		private List<String> nodes;

		private EdgeDefTerm(int l, int c, String name, String label, List<String> nodes) {
			super(l, c, name);
			this.label = label;
			this.nodes = nodes;
		}

		public String getLabel() {
			return label;
		}

		public List<String> getNodes() {
			return Collections.unmodifiableList(nodes);
		}

		@Override
		public String toString() {
			String name = getName();
			if (name != null) {
				return String.format("%s:%s(%s)", name, label, StringUtils.listString(nodes, ", "));
			} else {
				return String.format("%s(%s)", label, StringUtils.listString(nodes, ", "));
			}
		}
	}

	public static EdgeDefTerm createEdgeDefTerm(Token firstToken, String name, String label, List<String> nodes) {
		return new EdgeDefTerm(firstToken.beginLine, firstToken.beginColumn, name, label, nodes);
	}

	public static EdgeDefTerm createEdgeDefTerm(int l, int c, String name, String label, List<String> nodes) {
		return new EdgeDefTerm(l, c, name, label, nodes);
	}

	/* ====================================================================== */

	static class GraphDefTerm extends SgfTerm {
		protected List<SgfTerm> elems;

		private GraphDefTerm(Token firstToken, List<SgfTerm> elems) {
			super(firstToken);
			this.elems = elems;
		}

		public List<SgfTerm> getElements() {
			return Collections.unmodifiableList(elems);
		}

		protected String blockString() {
			return StringUtils.listString(elems, "; ");
		}

		@Override
		public String toString() {
			return String.format("graph { %s }", blockString());
		}
	}

	public static GraphDefTerm createGraphDefTerm(Token firstToken, List<SgfTerm> elems) {
		return new GraphDefTerm(firstToken, elems);
	}

	/* ====================================================================== */

	static class IfGraphDefTerm extends GraphDefTerm {
		private List<RefTerm> leftInterface;
		private List<RefTerm> rightInterface;

		private IfGraphDefTerm(Token firstToken, List<SgfTerm> elems, List<RefTerm> left, List<RefTerm> right) {
			super(firstToken, elems);
			leftInterface = left;
			rightInterface = right;
		}

		public List<RefTerm> getLeftInterface() {
			return Collections.unmodifiableList(leftInterface);
		}

		public List<RefTerm> getRightInterface() {
			return Collections.unmodifiableList(rightInterface);
		}

		@Override
		public String toString() {
			return String.format("ifgraph { [%s] => { %s } <= [%s] }", StringUtils.listString(leftInterface, ", "),
					blockString(), StringUtils.listString(rightInterface, ", "));
		}
	}

	public static IfGraphDefTerm createIfGraphDefTerm(Token firstToken,
			List<SgfTerm> elems, List<RefTerm> left, List<RefTerm> right) {
		return new IfGraphDefTerm(firstToken, elems, left, right);
	}

	/* ====================================================================== */

	static class MapstoTerm extends SgfTerm {
		private String left;
		private String right;

		private MapstoTerm(int l, int c, String left, String right) {
			super(l, c);
			this.left = left;
			this.right = right;
		}

		public String getLeft() {
			return left;
		}

		public String getRight() {
			return right;
		}

		@Override
		public String toString() {
			return String.format("%s => %s", left, right);
		}
	}

	public static MapstoTerm createMapstoTerm(Token firstToken, String left, String right) {
		return new MapstoTerm(firstToken.beginLine, firstToken.beginColumn, left, right);
	}

	public static MapstoTerm createMapstoTerm(int l, int c, String left, String right) {
		return new MapstoTerm(l, c, left, right);
	}

	/* ====================================================================== */

	static class MorphismDefTerm extends SgfTerm {
		private List<MapstoTerm> maps;
		private SgfTerm from;
		private SgfTerm to;

		private MorphismDefTerm(Token firstToken, SgfTerm from, SgfTerm to, List<MapstoTerm> maps) {
			super(firstToken);
			this.maps = maps;
			this.from = from;
			this.to = to;
		}

		public List<MapstoTerm> getMaps() {
			return Collections.unmodifiableList(maps);
		}

		public SgfTerm getFrom() {
			return from;
		}

		public SgfTerm getTo() {
			return to;
		}

		@Override
		public String toString() {
			StringBuilder buf = new StringBuilder();
			buf.append("morphism ");
			if (from != null) {
				buf.append(" from ");
				buf.append(from.toString());
			}
			if (to != null) {
				buf.append(" to ");
				buf.append(to.toString());
			}
			buf.append(" { ");
			buf.append(StringUtils.listString(maps, "; "));
			buf.append(" }");
			return buf.toString();
		}
	}

	public static MorphismDefTerm createMorphismDefTerm(Token firstToken, SgfTerm from, SgfTerm to, List<MapstoTerm> maps) {
		return new MorphismDefTerm(firstToken, from, to, maps);
	}

	/* ====================================================================== */

	static class TraceDefTerm extends SgfTerm {
		private List<SgfTerm> morphisms;

		private TraceDefTerm(int line, int column, List<SgfTerm> morphisms) {
			super(line, column);
			this.morphisms = morphisms;
		}

		public List<SgfTerm> getMorphisms() {
			return morphisms;
		}
	}

	public static TraceDefTerm createTraceDefTerm(int line, int column, List<SgfTerm> morphisms) {
		return new TraceDefTerm(line, column, morphisms);
	}

	/* ====================================================================== */

	static class CospanDefTerm extends SgfTerm {
		private SgfTerm left;
		private SgfTerm right;

		private CospanDefTerm(Token firstToken, SgfTerm left, SgfTerm right) {
			super(firstToken);
			this.left = left;
			this.right = right;
		}

		public SgfTerm getLeft() {
			return left;
		}

		public SgfTerm getRight() {
			return right;
		}

		@Override
		public String toString() {
			return String.format("cospan { left=%s; right=%s; }", left.toString(), right.toString());
		}
	}

	public static CospanDefTerm createCospanDefTerm(Token firstToken, SgfTerm left, SgfTerm right) {
		return new CospanDefTerm(firstToken, left, right);
	}

	/* ====================================================================== */

	static class QuickCospanDefTerm extends SgfTerm {
		private SgfTerm left;
		private SgfTerm middle;
		private SgfTerm right;

		private QuickCospanDefTerm(Token firstToken, SgfTerm left, SgfTerm middle, SgfTerm right) {
			super(firstToken);
			this.left = left;
			this.middle = middle;
			this.right = right;
		}

		public SgfTerm getLeft() {
			return left;
		}

		public SgfTerm getMiddle() {
			return middle;
		}

		public SgfTerm getRight() {
			return right;
		}

		@Override
		public String toString() {
			return String.format("rule { %s => %s <= %s }", left, middle, right);
		}
	}

	public static QuickCospanDefTerm createQuickCospanDefTerm(Token firstToken,
			SgfTerm left, SgfTerm middle, SgfTerm right) {
		return new QuickCospanDefTerm(firstToken, left, middle, right);
	}

	/* ====================================================================== */

	static class RuleDefTerm extends SgfTerm {
		private SgfTerm left;
		private SgfTerm right;
		private SgfTerm morphism;
		private @Nullable List<SgfTerm> nacs;

		private RuleDefTerm(Token firstToken, SgfTerm left, SgfTerm right, SgfTerm morphism,
				@Nullable List<SgfTerm> nacs) {
			super(firstToken);
			this.left = left;
			this.right = right;
			this.morphism = morphism;
			this.nacs = nacs;
		}

		public SgfTerm getLeft() {
			return left;
		}

		public SgfTerm getRight() {
			return right;
		}

		public SgfTerm getMorphism() {
			return morphism;
		}

		public @Nullable List<SgfTerm> getNacs() {
			return nacs;
		}

		@Override
		public String toString() {
			return String.format("rule { left=%s; right=%s; morphism=%s; }", left.toString(), right.toString(),
					morphism.toString());
		}
	}

	public static RuleDefTerm createRuleDefTerm(Token firstToken, SgfTerm left, SgfTerm right, SgfTerm morph,
			@Nullable List<SgfTerm> nacs) {
		return new RuleDefTerm(firstToken, left, right, morph, nacs);
	}

	public static RuleDefTerm createRuleDefTerm(Token firstToken, SgfTerm left, SgfTerm right, SgfTerm morph) {
		return createRuleDefTerm(firstToken, left, right, morph, null);
	}

	/* ====================================================================== */

	static class QuickRuleDefTerm extends SgfTerm {
		private SgfTerm left;
		private SgfTerm right;
		private @Nullable List<SgfTerm> nacs;

		private QuickRuleDefTerm(Token firstToken, SgfTerm left, SgfTerm right, List<SgfTerm> nacs) {
			super(firstToken);
			this.left = left;
			this.right = right;
			this.nacs = nacs;
		}

		public SgfTerm getLeft() {
			return left;
		}

		public SgfTerm getRight() {
			return right;
		}

		public @Nullable List<SgfTerm> getNacs() {
			return nacs;
		}

		@Override
		public String toString() {
			if (nacs == null) {
				return String.format("rule { %s => %s }", left, right);
			} else {
				return String.format("rule { %s => %s, NOT %s}", left, right, nacs);
			}
		}
	}

	public static QuickRuleDefTerm createQuickRuleDefTerm(Token firstToken, SgfTerm left, SgfTerm right,
			@Nullable List<SgfTerm> nacs) {
		return new QuickRuleDefTerm(firstToken, left, right, nacs);
	}

	public static QuickRuleDefTerm createQuickRuleDefTerm(Token firstToken, SgfTerm left, SgfTerm right) {
		return createQuickRuleDefTerm(firstToken, left, right, null);
	}

	/* ====================================================================== */

	static class GtsDefTerm extends SgfTerm {
		private boolean isETS;
		private SgfTerm initial;
		private List<SgfTerm> rules;

		private GtsDefTerm(Token firstToken, SgfTerm initial, List<SgfTerm> rules, boolean isETS) {
			super(firstToken);
			this.isETS = isETS;
			this.initial = initial;
			this.rules = rules;
		}

		public SgfTerm getInitial() {
			return initial;
		}

		public List<SgfTerm> getRules() {
			return Collections.unmodifiableList(rules);
		}

		public boolean isExplicitTransitionSystem() {
			return isETS;
		}

		@Override
		public String toString() {
			if (isETS) {
				return String.format("system { initial = %s; morphisms = [%s] }", initial.toString(),
						StringUtils.listString(rules, ", "));
			} else {
				return String.format("gts { initial = %s; rules = [%s] }", initial.toString(),
						StringUtils.listString(rules, ", "));
			}
		}
	}

	static GtsDefTerm createGtsDefTerm(Token firstToken, SgfTerm initial, List<SgfTerm> rules) {
		return new GtsDefTerm(firstToken, initial, rules, false);
	}

	public static GtsDefTerm createEtsDefTerm(Token firstToken, SgfTerm initial, List<SgfTerm> rules) {
		return new GtsDefTerm(firstToken, initial, rules, true);
	}

	/* ====================================================================== */

	static class ConstantConditionTerm extends SgfTerm {
		private boolean value;
		private RefTerm over;
		private ConstantConditionTerm(Token beginToken, RefTerm over, boolean value) {
			super(beginToken);
			this.value = value;
			this.over = over;
		}
		public boolean getValue() { return value; }
		public RefTerm getRoot() { return over; }
	}

	public static ConstantConditionTerm createConstantConditionTerm(Token beginToken, RefTerm over, boolean value) {
		return new ConstantConditionTerm(beginToken, over, value);
	}

	static class CospanConditionTupleTerm extends SgfTerm {
		private SgfTerm morphism;
		private SgfTerm childCondition;

		private CospanConditionTupleTerm(int line, int column, SgfTerm morphism, SgfTerm childCondition) {
			super(line, column);
			this.morphism = morphism;
			this.childCondition = childCondition;
		}

		public SgfTerm getMorphism() { return morphism; }
		public SgfTerm getChildCondition() { return childCondition; }

		@Override
		public String toString() {
			return String.format("%s => %s", morphism.toString(), childCondition.toString());
		}
	}

	public static CospanConditionTupleTerm createCospanConditionTupleTerm(int l, int c, SgfTerm morphism, SgfTerm childCondition)
	{
		return new CospanConditionTupleTerm(l, c, morphism, childCondition);
	}

	static class ConditionNegTerm extends SgfTerm {
		private SgfTerm child;
		private ConditionNegTerm(Token beginToken, SgfTerm child) {
			super(beginToken);
			this.child = child;
		}
		public SgfTerm getChild() {
			return child;
		}
	}

	public static ConditionNegTerm createConditionNegTerm(Token beginToken, SgfTerm childCondition) {
		return new ConditionNegTerm(beginToken, childCondition);
	}

	static class ConditionAndOrTerm extends SgfTerm {
		private List<SgfTerm> children;
		private Quantifier quantifier;
		private ConditionAndOrTerm(Token beginToken, Quantifier quantifier, List<SgfTerm> children) {
			super(beginToken);
			this.quantifier = quantifier;
			this.children = children;
		}
		public List<SgfTerm> getChildren() { return children; }
		public Quantifier getQuantifier() { return quantifier; }
	}

	public static ConditionAndOrTerm createConditionAndOrTerm(Token beginToken, Quantifier quantifier, List<SgfTerm> childConditions) {
		return new ConditionAndOrTerm(beginToken, quantifier, childConditions);
	}

	static class CospanConditionDefTerm extends SgfTerm {
		private Quantifier quantifier;
		private RefTerm root;
		private List<CospanConditionTupleTerm> childrenTerms = new ArrayList<>();

		private CospanConditionDefTerm(Token beginToken, Quantifier quantifier,
				RefTerm root, List<CospanConditionTupleTerm> childrenTerms) {
			super(beginToken);
			this.quantifier = quantifier;
			this.root = root;
			this.childrenTerms = childrenTerms;
		}

		public Quantifier getQuantifier() { return quantifier; }
		public RefTerm getRoot() { return root; }
		public List<CospanConditionTupleTerm> getChildTuples() { return childrenTerms; }
	}

	public static CospanConditionDefTerm createCospanConditionDefTerm(
			Token beginToken, Quantifier quantifier, RefTerm root,
			List<CospanConditionTupleTerm> childrenTerms) {
		return new CospanConditionDefTerm(beginToken, quantifier, root, childrenTerms);
	}

	static class GraphConditionDefTerm extends SgfTerm {
		private Quantifier quantifier;
		private SgfTerm morphism;
		private SgfTerm childCondition;

		private GraphConditionDefTerm(Token beginToken, Quantifier quantifier,
				SgfTerm morphism, SgfTerm childCondition) {
			super(beginToken);
			this.quantifier = quantifier;
			this.morphism = morphism;
			this.childCondition = childCondition;
		}

		public Quantifier getQuantifier() { return quantifier; }
		public SgfTerm getMorphism() { return morphism; }
		public SgfTerm getChildCondition() { return childCondition; }
	}

	public static GraphConditionDefTerm createGraphConditionDefTerm(
			Token beginToken, Quantifier quantifier,
			SgfTerm morphism, SgfTerm childCondition) {
		return new GraphConditionDefTerm(beginToken, quantifier, morphism, childCondition);
	}

	/* ====================================================================== */

	static class RefTerm extends NamedTerm {
		private RefTerm(Token firstToken, String name) {
			super(firstToken, name);
		}

		@Override
		public String toString() {
			return String.format("<%s>", getName());
		}
	}

	public static RefTerm createRefTerm(Token firstToken, String name) {
		return new RefTerm(firstToken, name);
	}

	/* ====================================================================== */

	static class CopyTerm extends NamedTerm {
		private CopyTerm(Token firstToken, String name) {
			super(firstToken, name);
		}

		@Override
		public String toString() {
			return String.format("copy(%s)", getName());
		}
	}

	public static CopyTerm createCopyTerm(Token firstToken, String name) {
		return new CopyTerm(firstToken, name);
	}

}
