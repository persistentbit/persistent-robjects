package com.persistentbit.substema.compiler.values.expr;

import com.persistentbit.substema.compiler.SubstemaException;
import com.persistentbit.substema.compiler.values.expr.*;

/**
 * Created by petermuys on 29/09/16.
 */
public interface RConstVisitor<R> {

    default R visit(RConst value){
        if(value instanceof RConstArray){
            return visit((RConstArray)value);
        } else if (value instanceof RConstBoolean){
            return visit((RConstBoolean)value);

        } else if(value instanceof RConstNull){
            return visit((RConstNull)value);

        } else if(value instanceof RConstNumber){
            return visit((RConstNumber)value);

        } else if(value instanceof RConstEnum){
            return visit((RConstEnum)value);

        } else if(value instanceof RConstString){
            return visit((RConstString)value);

        } else if(value instanceof RConstValueObject){
            return visit((RConstValueObject)value);

        } else {
            throw new SubstemaException("Unknown RConst type: " + value.getClass().getName());
        }

    }
    R visit(RConstBoolean c);
    R visit(RConstNull c);
    R visit(RConstNumber c);
    R visit(RConstEnum c);
    R visit(RConstString c);
    R visit(RConstValueObject c);
    R visit(RConstArray c);
}
