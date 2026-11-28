package io.github.crazy.lonewolf.coders.omnij.datasource.annotation;

import org.springframework.transaction.annotation.Transactional;
import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Transactional("omnijTransactionManager")
public @interface OmniJTransactional {
}
