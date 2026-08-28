package cn.com.edtechhub.worktopicselection.aop;


import cn.hutool.core.lang.TypeReference;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class TypeBuilder {

    public static TypeReference<Page<?>> buildPageTypeReference(Class<?> modelClass) {
        return new TypeReference<Page<?>>() {
            @Override
            public Type getType() {
                return new ParameterizedType() {
                    @Override
                    public Type getRawType() {
                        return Page.class;
                    }

                    @Override
                    public Type getOwnerType() {
                        return null;
                    }

                    @Override
                    public Type[] getActualTypeArguments() {
                        return new Type[]{modelClass};
                    }
                };
            }
        };
    }
}
