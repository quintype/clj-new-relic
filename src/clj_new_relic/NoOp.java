package clj_new_relic;

import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.METHOD;

@Target(METHOD)
public @interface NoOp {}
