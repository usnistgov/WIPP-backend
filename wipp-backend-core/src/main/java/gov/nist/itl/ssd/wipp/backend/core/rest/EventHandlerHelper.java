/*
 * This software was developed at the National Institute of Standards and
 * Technology by employees of the Federal Government in the course of
 * their official duties. Pursuant to title 17 Section 105 of the United
 * States Code this software is not subject to copyright protection and is
 * in the public domain. This software is an experimental system. NIST assumes
 * no responsibility whatsoever for its use by other parties, and makes no
 * guarantees, expressed or implied, about its quality, reliability, or
 * any other characteristic. We would appreciate acknowledgement if the
 * software is used.
 */
package gov.nist.itl.ssd.wipp.backend.core.rest;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

import org.springframework.beans.BeanUtils;
import org.springframework.util.ReflectionUtils;

import gov.nist.itl.ssd.wipp.backend.core.rest.annotation.Updatable;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.ForbiddenException;

/**
 *
 * @author Antoine Vandecreme
 */
public class EventHandlerHelper {

    private EventHandlerHelper() {
    }

    /**
     * Verifies that the updated fields are annotated with @Updatable
     *
     * @param updated the updated object
     * @param original the original object
     * @throws ForbiddenException If a not updatable field has been updated.
     */
    public static void assertUpdatedFieldsUpdatables(
            Object updated, Object original) {
        PropertyDescriptor[] descriptors = BeanUtils.getPropertyDescriptors(
                updated.getClass());
        for (PropertyDescriptor pd : descriptors) {
            try {
                Method getter = pd.getReadMethod();
                if (!Objects.equals(
                        getter.invoke(updated),
                        getter.invoke(original))) {
                    Field field = ReflectionUtils.findField(
                            updated.getClass(), pd.getName());
                    if (!field.isAnnotationPresent(Updatable.class)) {
                    	System.out.println("The field '" + pd.getDisplayName()
                                + "' can not be modified.");
                        throw new ForbiddenException(
                                "The field '" + pd.getDisplayName()
                                + "' can not be modified.");
                    }
                }
            } catch (IllegalAccessException | IllegalArgumentException |
                    InvocationTargetException ex) {
            	ex.printStackTrace();
                throw new RuntimeException(ex);
            }
        }
    }

}
