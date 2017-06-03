package edu.illinois.cs.cogcomp.lbj.coref.filters;

import java.util.ArrayList;
import java.util.List;

import edu.illinois.cs.cogcomp.lbj.coref.ir.Mention;


//TODO: Faster version for single type(s).
/** Filters EntityMentions by type */
public class TypeMFilter extends MFilter {
    protected List<String> m_includeMTypes = null;
    protected List<String> m_includeETypes = null;

    
    /**
     * Constructs a filter that allows only mentions
     * having a mention type.
     * @param includeMType The allowed mention type;
     * if null, all mention types are allowed.
     */
    public TypeMFilter(String includeMType) {
	this(includeMType, null);
    }
    
    /** 
     * Constructs a filter that allows only mentions having a given
     * mention type and a given entity type.
     * @param includeMType The allowed mention type; if null, all mention types
     * are allowed.
     * @param includeEType The allowed entity type; if null, all entity types
     * are allowed. 
     */
    public TypeMFilter(String includeMType, String includeEType) {
	setMType(includeMType);
	setEType(includeEType);
    }

    /** 
     * Constructs a filter that allows only mentions having a given
     * mention type and a given entity type.
     * Parameters are backed internally.
     * @param includeMTypes The allowed mention types;
     * if null, all mention types
     * are allowed; if empty, no mention types are allowed.
     * @param includeETypes The allowed entity types; if null, all entity types
     * are allowed; if empty, no entity types are allowed.
     */
    public TypeMFilter(List<String> includeMTypes, List<String> includeETypes) {
	setMTypes(includeMTypes);
	setETypes(includeETypes);
    }
    
    /** 
     * Constructs a filter that allows only mentions having a given
     * mention type.
     * @param includeMTypes The allowed mention types;
     * if null, all mention types
     * are allowed; if empty, no mention types are allowed. Backed internally.
     */
    public TypeMFilter(List<String> includeMTypes) {
	this(includeMTypes, null);
    }

    /**
     * Determines whether {@code m} should be accepted by this filter.
     */
    public boolean accept(Mention m) {
	if (m_includeMTypes != null) {
	    boolean found = false;
	    for (String mType : m_includeMTypes) {
		if ( m.getType().equals(mType) )
		    found = true;
	    }
	    if (!found)
		return false;
	}
	if (m_includeETypes != null) {
	    boolean found = false;
	    for (String eType : m_includeETypes) {
		if ( m.getEntityType().equals(eType) )
		    found = true;
	    }
	    if (!found)
		return false;
	}
	return true; //All available filters accept
    }

    /** Make {@code eType} the only accepted entity type. */
    public void setEType(String eType) {
	if (eType != null) {
	    m_includeETypes = new ArrayList<String>();
	    m_includeETypes.add(eType);
	} else {
	    m_includeETypes = null;
	}
    }

    /**
     * Sets the allowed entity types.
     * @param eTypes The allowed entity types; if null, all entity types
     * are allowed; if empty, no entity types are allowed.
     */
    public void setETypes(List<String> eTypes) {
	if (eTypes == null)
	    m_includeETypes = null;
	else
	    m_includeETypes = new ArrayList<String>(eTypes);
    }

    /**
     * Make {@code mType} the only accepted mention type.
     */
    public void setMType(String mType) {
	if (mType != null) {
	    m_includeMTypes = new ArrayList<String>();
	    m_includeMTypes.add(mType);
	} else {
	    m_includeMTypes = null;
	}
    }

    /**
     * Sets the allowed mention types.
     * @param mTypes The allowed mention types; if null, all mention types
     * are allowed; if empty, no mention types are allowed. Backed internally.
     */
    public void setMTypes(List<String> mTypes) {
	if (mTypes == null)
	    m_includeMTypes = null;
	else
	    m_includeMTypes = new ArrayList<String>(mTypes);
    }
}
