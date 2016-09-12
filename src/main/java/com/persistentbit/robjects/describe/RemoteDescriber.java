package com.persistentbit.robjects.describe;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.PMap;
import com.persistentbit.core.collections.PStream;
import com.persistentbit.jjson.mapping.JJMapper;
import com.persistentbit.jjson.mapping.description.JJClass;
import com.persistentbit.jjson.mapping.description.JJPropertyDescription;
import com.persistentbit.jjson.mapping.description.JJTypeDescription;
import com.persistentbit.jjson.mapping.description.JJTypeSignature;
import com.persistentbit.robjects.RemotableClasses;
import com.persistentbit.robjects.annotations.Remotable;
import com.persistentbit.robjects.annotations.RemoteCache;
import sun.rmi.rmic.RemoteClass;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.logging.Logger;


/**
 * @author Peter Muys
 * @since 31/08/2016
 */
public class RemoteDescriber {
    static private final Logger log = Logger.getLogger(RemoteDescriber.class.getName());
    private final JJMapper  mapper;

    public RemoteDescriber(JJMapper mapper) {
        this.mapper = mapper;
    }
    public RemoteDescriber() {
        this(new JJMapper());
    }

    private class Describer{
        private Class<?>    root;
        private PMap<Class<?>,RemoteClassDescription>  remotables = PMap.empty();
        private PMap<Class<?>,JJTypeDescription> valueClasses = PMap.empty();
        Describer(Class<?> root){
            this.root = root;
            addDependencies(root);
        }
        RemoteServiceDescription getRemoteServiceDescription(){
            return new RemoteServiceDescription(remotables.get(root).getType(),remotables.values().pset(),valueClasses.values().pset());
        }

        private void addDependencies(Class<?> cls){
            cls = asRemotable(cls,cls).orElse(null);
            if(remotables.containsKey(cls)){
                return;
            }
            PList<RemoteMethodDescription> methods = PList.empty();
            for(Method m : cls.getDeclaredMethods()){
                log.fine("Adding method " + m);
                PList<JJPropertyDescription> params =
                        PStream.from(m.getParameters()).map(p-> new JJPropertyDescription(p.getName(),mapper.describe(p.getType()).getTypeSignature(),PList.empty())).plist();
                boolean returnsRemote = RemotableClasses.getRemotableClass(m.getReturnType()) != null;
                RemoteMethodDescription rem = new RemoteMethodDescription(m.getName(),
                        mapper.describe(m.getReturnType(),m.getGenericReturnType()).getTypeSignature(),
                        params,m.getDeclaredAnnotation(RemoteCache.class) != null,returnsRemote,PList.empty());
                methods  = methods.plus(rem);
            }


            remotables = remotables.put(cls,new RemoteClassDescription(new JJTypeSignature(new JJClass(cls), JJTypeSignature.JsonType.jsonObject),methods));
            for(Method m : cls.getDeclaredMethods()){
                for(Parameter p : m.getParameters()){
                    addValueClass(p.getType(),p.getParameterizedType());
                }
                Class<?> rem = asRemotable(m.getReturnType(),m.getGenericReturnType()).orElse(null);
                if(rem == null){
                    addValueClass(m.getReturnType(),m.getGenericReturnType());
                } else {
                    addDependencies(rem);
                }
            }
        }



        private void addValueClass(Class<?> cls,Type t){
            if(cls == void.class){
                return;
            }
            if(cls.isPrimitive()){
                return;
            }
            if(valueClasses.containsKey(cls)){
                return;
            }

            log.fine("adding value class " + t);


            JJTypeDescription ts = mapper.describe(cls,t);
            valueClasses = valueClasses.put(cls,ts);
            ts.getAllUsedClassNames().map(jjClass -> {
                try {
                    return Class.forName(jjClass.getFullJavaName());
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }).forEach(c -> addValueClass(c,c));
        }

        private Optional<Class<?>>  asRemotable(Class<?> cls, Type type){
            if(isRemotable(cls)){
                return Optional.of(cls);
            }
            return Optional.empty();
        }

        private boolean isRemotable(Class<?> cls) {
            return cls.isInterface() && cls.getDeclaredAnnotation(Remotable.class) != null;
        }
    }
    public RemoteServiceDescription    descripeRemoteService(Class<?> root){
        return new Describer(root).getRemoteServiceDescription();
    }
}
