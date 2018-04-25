package com.amazon.df.object.provider;

import com.amazon.df.object.ObjectFactory;
import com.amazon.df.object.resolver.Resolver;
import com.amazon.df.object.util.Inspector;

public interface WithResolver {

    /**
     * Resolve concrete type for a interface or abstract class, by using resolvers provided by ObjectFactory.
     *
     * @param objectFactory object factory
     * @param clazz a class to resolve
     * @return resolved concrete type or null if not found from all resolvers.
     */
    default Class<?> resolveConcreteType(ObjectFactory objectFactory, Class<?> clazz) {
        for (Resolver resolver : objectFactory.getResolvers()) {
            Class<?> resolved = resolver.resolve(clazz);
            if (resolved != null && !Inspector.isInterface(resolved) && !Inspector.isAbstract(resolved)) {
                return resolved;
            }
        }

        return null;
    }

}
