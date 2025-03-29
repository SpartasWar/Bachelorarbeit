/* 
 * Copyright (c) 2013-2014, H.J. Sander Bruggink, 
 * Copyright (c) 2013-2014, University of Duisburg-Essen
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

package de.uni_due.inf.ti.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import de.uni_due.inf.ti.util.StringUtils;

/**
 * This class represents rules with negative application conditions.
 *
 * @author H.J. Sander Bruggink
 */
public class NacRule extends Rule
{    
    private List<Morphism> nacs;
    
    /**
     * Creates a new NacRule with a reference to an existing parameter set.
     * 
     * @param corr          correspondence morphism of the new rule
     * @param params        parameter set of the new rule
     */
    public NacRule(Morphism corr, TransformationParams params)
    {
        super(corr, params);
        
        nacs = new ArrayList<Morphism>();
    }
    
    
    /**
     * Creates a new NacRule.
     * 
     * @param corr          correspondence morphism of the new rule
     */
    public NacRule(Morphism corr) 
    {
        this (corr, TransformationParams.create());
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public NacRule clone()
    {
        Morphism leftIso = Morphism.createIsomorphism(getCorrespondence().getDomain());
        Morphism leftIsoInv = leftIso.getInverse();
        Morphism rightIso = Morphism.createIsomorphism(getCorrespondence().getCodomain());
        Morphism newCorr = Morphism.createComposition(
                leftIsoInv, 
                getCorrespondence(), 
                rightIso);
        NacRule rule = new NacRule(newCorr);
        for (Morphism nac : nacs) {
            rule.addNac(leftIsoInv.compose(nac));
        }
        return rule;
    }
    

    /**
     * Adds a negative application condition to this rule.
     * 
     * @param nac
     *      the negative application condition which is to be added to this
     *      NacRule
     * @throws IllegalArgumentException
     *      if the domain of {@code nac} is different than this rule's
     *      left-hand side
     */
    public void addNac(Morphism nac) 
    {
        if (nac.getDomain() != getLeft())
            throw new IllegalArgumentException();
        
        nacs.add(nac);
    }
    
    
    /**
     * Removes a negative application condition from this rule.
     */
    public void removeNac(Morphism nac) 
    {
        nacs.remove(nac);
    }
    
    
    /**
     * Returns the list of negative application conditions for this rule.
     */
    public List<Morphism> getNacs()
    {
        return Collections.unmodifiableList(nacs);
    }
    
    
    /**
     * Checks whether an application condition is satisfied.
     * The application condition is satisfied when there exists a morphism
     * <tt><i>m</i></tt> such that <tt><i>m</i> o <i>ac</i> = <i>match</i></tt>.
     * 
     * @param match         
     *      the match of the left-hand side of this rule to the graph which is
     *      transformed
     * @param ac            
     *      application condition to test
     * @return
     *      {@code true} if the application condition {@code ac} is satisfied
     *      for the match {@code match}
     */
    private boolean checkApplicationCondition(Morphism match, Morphism ac) 
    {
        Graph domain = ac.getCodomain();
        Graph codomain = match.getCodomain();
        Morphism base = new Morphism(domain, codomain);
        
        for (Edge leftEdge : getLeft().getEdges()) {
            Edge nacEdge = ac.get(leftEdge);
            if (nacEdge != null) {
                Edge matchEdge = match.get(leftEdge);
                assert matchEdge != null : "@AssumeAssertion(nullness)"; // since match is total
                if (!base.checkAndPut(nacEdge, matchEdge))
                    return false;
            }
        }
        
        for (Node leftNode : getLeft().getNodes()) {
            Node nacNode = ac.get(leftNode);
            if (nacNode != null) {
                Node matchNode = match.get(leftNode);
                assert matchNode != null : "@AssumeAssertion(nullness)"; // since match is total
                if (!base.checkAndPut(nacNode, matchNode))
                    return false;
            }
        }
        
        boolean injective = getTransformationParams().getInjectiveMatches(); 
        Morphism ext = Morphism.getMatch(domain, codomain, injective, base);
        return ext != null;
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean checkMatch(Morphism match)
    {
        if (!super.checkMatch(match)) return false;
        for (Morphism nac : nacs) {
            if (checkApplicationCondition(match, nac)) return false;
        }
        
        return true;
    }
    
    
    /**
     * This method tests whether two collections of negative application
     * conditions are equivalent. It is meant for internal use by 
     * {@link #isIsomorphic(Rule)}; using it in another context is not
     * recommended.
     * <p>
     * <strong>Preconditions:</strong> 
     *   all morphisms of {@code nc1} have the same domain {@code g},
     *   all morphisms of {@code nc2} have the same domain {@code h}, and
     *   {@code leftIso} is an isomorphism with domain {@code g} and
     *   codomain {@code h}.
     * <p>  
     * <strong>Warning:</strong>
     *   the collection referred to by {@code nc2} is modified.
     */
    private static boolean testNacs(Collection<Morphism> nc1, Collection<Morphism> nc2, Morphism leftIso)
    {
        for (Morphism nac1 : nc1) {

            boolean fnd = false;
            for (Iterator<Morphism> it = nc2.iterator();
                    !fnd && it.hasNext();) {
                Morphism nac2 = it.next();
                
                for (Morphism nacIso : Morphism.getIsomorphisms(nac2.getCodomain(), nac1.getCodomain())) {
                    Morphism nacComp = Morphism.createComposition(
                            leftIso,
                            nac2,
                            nacIso);
                    if (nacComp.equals(nac1)) {
                        it.remove();
                        fnd = true;
                        break;
                    }
                }
            }
            if (!fnd) return false;
        }
        
        return nc2.isEmpty();
    }
    
    
    /**
     * Checks whether this NacRule is isomorphic to another Rule.
     * This is the case, if the other rule is also a NacRule, which has 
     * an isomorphic left-hand side, isomorphic right-hand side, and 
     * isomorphic negative application conditions (such that all morphism
     * commute).
     * 
     * @param other         the rule to check isomorphism of
     * @return              {@code true} if this NacRule is isomorphic to
     *                      {@code other}, {@code false} otherwise
     */
    @Override
    public boolean isIsomorphic(Rule other)
    {
        if (!(other instanceof NacRule)) return false;
        NacRule nother = (NacRule)other;
        
        for (Morphism leftIso : Morphism.getIsomorphisms(getLeft(), other.getLeft())) {
            for (Morphism rightIso : Morphism.getIsomorphisms(other.getRight(), getRight())) {
                
                Morphism ruleComp = Morphism.createComposition(
                        leftIso, 
                        other.getCorrespondence(), 
                        rightIso);
                
                if (ruleComp.equals(getCorrespondence())) {
                    List<Morphism> otherNacs = new ArrayList<Morphism>(nother.nacs);
                    
                    if (testNacs(nacs, otherNacs, leftIso)) 
                        return true;
                }
            }
        }
        
        return false;
    }    
    
    
    /**
     * Counts the number of unique nodes in this rule.
     * The number of unique nodes in this rule is equal to the number of 
     * nodes in the left-hand side, plus the number of nodes in the right-hand
     * side which have no corresponding node in the left-hand side, plus for
     * each negative application condition the number of nodes that have no
     * corresponding node in the left-hand side.
     * 
     * @return
     *      the number of unique nodes in this rule
     */
    @Override
    public int countUniqueNodes()
    {
        int n = super.countUniqueNodes();
        for (Morphism nac : nacs) {
            for (Node node : nac.getCodomain().getNodes()) {
                if (!nac.inRange(node)) n++;
            }
        }
        return n;
    }

    
    /**
     * Counts the number of unique edges in this rule.
     * The number of unique edges in this rule is equal to the number of 
     * edges in the left-hand side, plus the number of edges in the right-hand
     * side which have no corresponding edge in the left-hand side, plus for
     * each negative application condition the number of edges that have no
     * corresponding edge in the left-hand side.
     * 
     * @return
     *      the number of unique edges in this rule
     */
    @Override
    public int countUniqueEdges()
    {
        int n = super.countUniqueEdges();
        for (Morphism nac : nacs) {
            for (Edge edge : nac.getCodomain().getEdges()) {
                if (!nac.inRange(edge)) n++;
            }
        }
        return n;
    }

    
    /**
     * {@inheritDoc}
     */
    public String toString(String indent) 
    {
        String newIndent = indent + "  ";
        
        List<String> nacStrings = new ArrayList<String>();
        for (Morphism nac : nacs) {
            String morphismString = nac.toString();
            String targetString = "with target: \n" + nac.getCodomain().toString("  ");
            nacStrings.add(StringUtils.columnize(newIndent + "  ", 2, morphismString, targetString));
        }
        
        String my = StringUtils.listString(
                nacStrings, 
                newIndent + "nacs = [\n", 
                "\n  " + newIndent,
                newIndent + "]\n");
        
        return toString(indent, my);
    }
}
