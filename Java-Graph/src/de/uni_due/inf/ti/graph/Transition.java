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

package de.uni_due.inf.ti.graph;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * This class represents a transition in a transition system.
 * 
 * @author H.J. Sander Bruggink
 */
public class Transition extends AttributeObject
{
    private Morphism morphism;
    private @Nullable Morphism match;
    private @Nullable Rule rule;
    private @Nullable String customLabel;
    private int weight;
    
    /**
     * Private constructor which sets all properties.
     */
    private Transition(
            Morphism morphism, 
            @Nullable Morphism match, 
            @Nullable Rule rule, 
            @Nullable String customLabel,
            int weight)
    {
        if (morphism == null) throw new NullPointerException();
        
        this.morphism = morphism;
        this.match = match;
        this.rule = rule;
        this.customLabel = customLabel;
        this.weight = weight;
    }
    
    /**
     * Creates a new transition.
     * 
     * @throws NullPointerException
     *      if {@code morphism} is {@code null}
     */
    public Transition(Morphism morphism)
    {
        this(morphism, null, null, null, 1);
    }

    /**
     * Creates a new transition which is labeled by a rule.
     * 
     * @throws NullPointerException
     *      if {@code morphism} is {@code null}
     */
    public Transition(
            Morphism morphism, 
            @Nullable Morphism match,
            @Nullable Rule rule)
    {
        this(morphism, match, rule, null, 1);
    }

    
    /**
     * Creates a new transition which is labeled by a custom label.
     * 
     * @throws NullPointerException
     *      if {@code morphism} is {@code null}
     */
    public Transition(
            Morphism morphism, 
            @Nullable String customLabel)
    {
        this(morphism, null, null, customLabel, 1);
    }
    
    /**
     * Creates a new transition which is a copy of another transition.
     * 
     * @param orig
     *      the original Transition of which the new Transition is a copy
     * @throws NullPointerException
     *      when {@code orig} is {@code null}
     */
    public Transition(Transition orig) 
    {
        super(orig);
        this.morphism = new Morphism(orig.morphism);
        // since the rest is basically only a label, do a shallow copy for
        // performance reasons
        this.match = orig.match;
        this.rule = orig.rule;
        this.weight = orig.weight;
        this.customLabel = orig.customLabel; 
    }
    
    /**
     * Returns the rule of this transition.
     * 
     * @return              
     *      the rule of this transition, or {@code null} if this transition
     *      does not have a rule
     */
    public @Nullable Rule getRule()
    {
        return rule;
    }
    
    /**
     * Sets the rule of this transition.
     * Additionally, if a match is set for this transition, it is set to 
     * {@code null}.
     * 
     * @param rule
     *      the new rule of this transition, or {@code null} to remove the
     *      rule from this transition
     */
    public void setRule(@Nullable Rule rule)
    {
        match = null;
        this.rule = rule;
    }
    
    /**
     * Returns the match of this transition.
     * 
     * @return 
     *      the match of this transition, or {@code null} if this transition
     *      does not have a rule or the match is not specified
     */
    public @Nullable Morphism getMatch()
    {
        if (rule == null) 
            return null;
        else 
            return match;
    }
    
    /**
     * Sets the match of this transition. The match must be a morphism from
     * the transition's rule to the source of the transition.
     * 
     * @param
     *      match the new match of this transition, or {@code null} to remove
     *      the match from this transition
     * @throws IllegalStateException
     *      if {@code match} is non-null, and the transition does not have a
     *      rule or the match is not a morphism from the rule's left-hand side
     *      to the transition's source graph
     */
    public void setMatch(@Nullable Morphism match) 
    {
        if (match == null) {
            this.match = null;
        }
        else {
            Rule r = rule;
            if (r == null) 
                throw new IllegalStateException("Transition's rule must be non-null.");
            else if (match.getDomain() != r.getLeft())
                throw new IllegalStateException("Match does not fit to the transition's rule.");
            else if (match.getCodomain() != morphism.getDomain()) 
                throw new IllegalStateException("Match does not fit transition morphism.");
            this.match = match;
        }
    }
    
    /**
     * Returns the custom label of this transition.
     * 
     * @return              
     *      the custom label of this transition, or {@code null} if this
     *      transition does not have a custom label
     */
    public @Nullable String getCustomLabel()
    {
        return customLabel;
    }
    
    /**
     * Sets the custom label of this transition. If {@code customLabel == null},
     * the custom label of this transition is removed. 
     */
    public void setCustomLabel(@Nullable String customLabel)
    {
        this.customLabel = customLabel;
    }
    
    /**
     * Sets the weight of this transition.
     */
    public void setWeight(int weight)
    {
        this.weight = weight;
    }
    
    /**
     * Returns the weight of this transition.
     */
    public int getWeight()
    {
        return weight;
    }
    
    /**
     * Returns the morphism of this transition.
     */
    public Morphism getMorphism()
    {
        return morphism;
    }
    
    /**
     * Returns the source graph of this transition.
     */
    public Graph getSource()
    {
        return morphism.getDomain();
    }
    
    /**
     * Returns the target graph of this transition.
     */
    public Graph getTarget()
    {
        return morphism.getCodomain();
    }
    
    /**
     * Returns the label of this transition.
     * The label is the custom label if it is set, or the name of the rule
     * if the rule is set. In all other cases, the label is {@code null}.
     * 
     * @return              
     *      the label of this transition, or {@code null} if it doesn't have 
     *      a label
     */
    public @Nullable String getLabel()
    {
        if (customLabel != null) {
            return customLabel;
        }
        else if (rule != null) {
            return rule.getName();
        }
        else {
            return null;
        }
    }
    
    /**
     * Creates a new transition which is the same as this transition except
     * that the source is changed to an isomorphic graph.
     * The argument is an isomorphism which has this transitions's source as
     * domain and the isomorphic new source as codomain.
     * 
     * @param iso
     *      isomorphism 
     * @return
     *      new transition with isomorphic source
     * @throws NullPointerException 
     *      if {@code iso} is {@code null}
     * @throws IllegalArgumentException
     *      if the domain of {@code iso} is not this transition's source,
     *      or if {@code iso} is not an isomorphism
     */
    public Transition changeSource(Morphism iso)
    {
        if (iso.getDomain() != getSource() || !iso.isIsomorphism()) 
            throw new IllegalArgumentException();
                
        Morphism newMorphism = iso.getInverse().compose(morphism);
        Morphism newMatch = match == null ? null : 
            match.compose(iso);
        
        return new Transition(newMorphism, newMatch, rule, customLabel, weight);
    }
    
    /**
     * Creates a new transition which is the same as this transition except
     * that the target is changed to an isomorphic graph.
     * The argument is an isomorphism which has this transitions's source as
     * domain and the isomorphic new source as codomain.
     * 
     * @param iso
     *      isomorphism 
     * @return
     *      new transition with isomorphic target
     * @throws NullPointerException
     *      if {@code iso} is {@code null}
     * @throws IllegalArgumentException
     *      if the domain of {@code iso} is not this transition's target,
     *      or if {@code iso} is not an isomorphism
     */
    public Transition changeTarget(Morphism iso)
    {
        if (iso.getDomain() != getTarget() || !iso.isIsomorphism()) 
            throw new IllegalArgumentException();
                
        Morphism newMorphism = morphism.compose(iso);
        
        return new Transition(newMorphism, match, rule, customLabel, weight);
    }
    
    /**
     * Creates a new transition which is the same as this transition except
     * that the source and target are changed to isomorphic graphs.
     * 
     * @param srcIso
     *      isomorphism with this transition's source as domain
     * @param tgtIso 
     *      isomorphism with this transition's target as domain  
     * @return
     *      new transition with isomorphic source and target
     * @throws NullPointerException
     *      if {@code srcIso} or {@code tgtIso} is {@code null}
     * @throws IllegalArgumentException
     *      if {@code srcIso} is no isomorphism with this transition's source
     *      as domain, or {@code tgtIso} is no isomorphism with this
     *      transitions target as domain.
     */
    public Transition changeCoordinates(Morphism srcIso, Morphism tgtIso)
    {
        if (srcIso.getDomain() != getSource() || !srcIso.isIsomorphism()) 
            throw new IllegalArgumentException();
        if (tgtIso.getDomain() != getTarget() || !tgtIso.isIsomorphism())
            throw new IllegalArgumentException();
                
        Morphism newMorphism = Morphism.createComposition(
                srcIso.getInverse(), 
                morphism, 
                tgtIso);
        Morphism newMatch = match == null ? null : 
            match.compose(srcIso);
        
        return new Transition(newMorphism, newMatch, rule, customLabel, weight);
    }
}
