package com.persistentbit.robjects.rod;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.PSet;
import com.persistentbit.core.collections.PStream;
import com.persistentbit.robjects.rod.values.*;

/**
 * Created by petermuys on 17/09/16.
 */
public class RServiceValidator {
    private final RService  service;

    private RServiceValidator(RService service) {
        this.service = service;
    }

    private void validate() {
        checkClassesDefined();
    }

    private void checkClassesDefined(){
        PSet<RClass>   needed   =   PSet.empty();
        PSet<RClass>   defined  =   PSet.empty();
        needed = needed.plusAll(service.valueClasses.map(vc -> needed(vc)).flatten());
        needed = needed.plusAll(service.remoteClasses.map(rc -> needed(rc)).flatten());
        defined = defined.plusAll(service.enums.map(e -> e.name));
        defined = defined.plusAll(service.valueClasses.map(vc -> vc.typeSig.name));
        defined = defined.plusAll(service.remoteClasses.map(rc -> rc.name));
        PSet<RClass> buildIn = PSet.empty();
        buildIn = buildIn.plusAll(PSet.val("Byte","Short","Integer","Long","Float","Double","String","Boolean","List","Map","Set").map(n -> new RClass(service.packageName,n)));
        PSet<RClass> all = defined.plusAll(buildIn);
        PSet<RClass> undef = needed.filter(c -> all.contains(c) == false);
        if(undef.isEmpty() == false){
            throw new RServiceException("Following types are Undefined: " + undef.map(r -> r.packageName + "." + r.className).toString(", "));
        }
    }




    private PSet<RClass>   needed(RRemoteClass rc){
        PSet<RClass> res = PSet.empty();
        res = res.plusAll(rc.functions.map(f -> needed(f)).flatten());
        return res;
    }
    private PSet<RClass> needed(RFunction f){
        PSet<RClass> res = PSet.empty();
        if(f.resultType != null){
            res = res.plusAll(needed(f.resultType.typeSig));
        }
        PStream<RClass> ap =f.params.map(p -> needed(p.valueType.typeSig)).flatten();
        return res.plusAll(ap);
    }



    private PSet<RClass>   needed(RValueClass vc){
        PSet<RClass> res = PSet.empty();
        PSet<String> genNames = vc.typeSig.generics.map(sig -> sig.name.className).pset();
        return res.plusAll(vc.properties.map(p -> needed(p.valueType.typeSig)).flatten()).filter(c -> genNames.contains(c.className) == false);
    }

    private PSet<RClass> needed(RTypeSig sig){
        PSet<RClass> res = PSet.empty();
        res = res.plus(sig.name);
        return res.plusAll(sig.generics.map(g -> needed(g)).flatten());
    }

    static public RService  validate(RService service){
        new RServiceValidator(service).validate();
        return service;
    }
}
