package edu.illinois.cs.cogcomp.finer.components.mention;

import edu.illinois.cs.cogcomp.finer.datastructure.types.FinerType;
import edu.illinois.cs.cogcomp.finer.datastructure.types.TypeSystem;

import java.util.Map;

/**
 * Created by haowu4 on 5/17/17.
 */
public class TypeMapper {
    TypeSystem types;
    Map<String, String> mapping;

    public TypeMapper(TypeSystem types, Map<String, String> mapping) {
        this.types = types;
        this.mapping = mapping;
    }

    public FinerType getType(String label) {
        String tname = mapping.getOrDefault(label, null);
        if (tname != null)
            return types.getTypeOrFail(tname);
        else {
            return null;
        }
    }

}