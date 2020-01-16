package gov.nist.itl.ssd.wipp.backend.core.rest.authorization;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Marker annotations to keep track of secured elements.
 *
 * @author Antoine Gerardin <antoine.gerardin@nist.gov> @2017
 */
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface RestrictedAccess {
    String note() default "";
}
