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

import de.uni_due.inf.ti.graph.util.ThreeBool;

/**
 * This class keeps parameters about the transformation process in general.
 * 
 * @author H.J. Sander Bruggink
 */
public class TransformationParams
{
    private static TransformationParams defaults = new TransformationParams();
    
    private @Nullable TransformationParams owner = null;
    
    private ThreeBool injectiveMatches = ThreeBool.UNKNOWN;
    private ThreeBool requireDEC = ThreeBool.UNKNOWN;
    private ThreeBool requireCF = ThreeBool.UNKNOWN;

    /**
     * Creates a new, empty TransformationParams instance.
     */
    private TransformationParams()
    {
        // empty
    }
    
    /** 
     * Returns the TransformationParams object that own this 
     * TransformationParams object, or {@code null} of this
     * object has no owner.
     */
    public @Nullable TransformationParams getOwner()
    {
        return owner;
    }
    
    /**
     * Returns a new TransformationParams object which is a child of this
     * TransformationParams object.
     * The new TransformationParams object has this TransformationParams object
     * as parent and non of its parameters are set.
     * 
     * @return
     *      a new TransformationParams object which is a child of this
     *      TransformationParams object
     */
    public TransformationParams newChild()
    {
        TransformationParams params = new TransformationParams();
        params.owner = this;
        return params;
    }

    /**
     * Sets the "injective matches" parameter.
     * 
     * @param im
     *      new value of the "injective matches" parameter.
     */
    public void setInjectiveMatches(boolean im) 
    {
        injectiveMatches = ThreeBool.asThreeBool(im);       
    }
    
    /**
     * Resets the "injective matches" parameter.
     */
    public void resetInjectiveMatches() 
    {
        injectiveMatches = ThreeBool.UNKNOWN;
    }
    
    /**
     * Returns the "injective matches" parameter.
     */
    public boolean getInjectiveMatches()
    {
        if (injectiveMatches == ThreeBool.UNKNOWN) {
            if (owner != null) {
                return owner.getInjectiveMatches();
            }
            else {
                return false;
            }
        }
        else {
            return injectiveMatches.isTrue();
        }
    }
    
    /**
     * Sets the "require dangling edge condition" parameter.
     * This parameter controls whether the dangling edge condition is required
     * when transforming graphs.
     */
    public void setRequireDanglingEdgeCondition(boolean dec)
    {
        requireDEC = ThreeBool.asThreeBool(dec);
    }
    
    /**
     * Resets the "require dangling edge condition" parameter.
     * This parameter controls whether the dangling edge condition is required
     * when transforming graphs.
     */
    public void resetRequireDanglingEdgeCondition()
    {
        requireDEC = ThreeBool.UNKNOWN;
    }
    
    /**
     * Returns the value of the "require dangling edge condition" parameter.
     * This parameter controls whether the dangling edge condition is required
     * when transforming graphs.
     */
    public boolean getRequireDanglingEdgeCondition()
    {
        if (requireDEC == ThreeBool.UNKNOWN) {
            if (owner != null) {
                return owner.getRequireDanglingEdgeCondition();
            }
            else {
                return false;
            }
        }
        else {
            return requireDEC.isTrue();
        }
    }
    
    /**
     * Sets the "require conflict free" parameter.
     * This parameter controls whether matches must be conflict free when 
     * transforming.
     */
    public void setRequireConflictFree(boolean dec)
    {
        requireCF = ThreeBool.asThreeBool(dec);
    }
    
    /**
     * Resets the "require conflict free" parameter.
     * This parameter controls whether matches must be conflict free when 
     * transforming.
     */
    public void resetRequireConflictFree()
    {
        requireCF = ThreeBool.UNKNOWN;
    }
    
    /**
     * Returns the value of the "require conflict free" parameter.
     * This parameter controls whether matches must be conflict free when 
     * transforming.
     */
    public boolean getRequireConflictFree()
    {
        if (requireCF == ThreeBool.UNKNOWN) {
            if (owner != null) {
                return owner.getRequireConflictFree();
            }
            else {
                return false;
            }
        }
        else {
            return requireCF.isTrue();
        }
    }
    
    /**
     * Returns the default transformation parameters. New instances of most 
     * classes which use transformation params will initially use these
     * default parameters, until the user sets their transformation parameters
     * differently.
     * 
     * @return
     *      the default transformation parameters
     */
    public static TransformationParams getDefaults()
    {
        return defaults;
    }
    
    /**
     * Returns a new TransformationParams instance which is a child of the
     * default TransformationParams.
     */
    public static TransformationParams create()
    {
        return defaults.newChild();
    }
}
