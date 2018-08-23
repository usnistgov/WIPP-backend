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

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;

import gov.nist.itl.ssd.wipp.backend.core.rest.annotation.ManualRef;

/**
 *
 * @author Antoine Vandecreme <antoine.vandecreme at nist.gov>
 */
@Component
public class ManualRefResourceProcessor
        implements ResourceProcessor<Resource<?>> {

    @Autowired
    private EntityLinks entityLinks;

    private static final Logger logger = Logger.getLogger(
            ManualRefResourceProcessor.class.getName());

    @Override
    public Resource<?> process(Resource<?> resource) {

        Class clazz = resource.getContent().getClass();
        Stream<Field> ownFields = Arrays.stream(clazz.getDeclaredFields());

        Stream<Class> classes = Arrays.stream(clazz.getClasses());
        Stream<Field> parentFields = classes.flatMap(c
                -> Arrays.stream(c.getDeclaredFields())
        );

        Stream<Field> fields = Stream.concat(ownFields, parentFields);

        fields.forEach(field -> {
            ManualRef manualRef = AnnotationUtils.getAnnotation(
                    field, ManualRef.class);
            if (manualRef != null) {
                try {
                    field.setAccessible(true);
                    Object fieldValue = field.get(resource.getContent());
                    if (fieldValue != null) {
                        Link link = entityLinks.linkToSingleResource(
                                manualRef.value(),
                                fieldValue
                        );
                        link = link.withRel(field.getName());
                        resource.add(link);
                    }
                } catch (IllegalArgumentException |
                        IllegalAccessException ex) {
                    logger.log(Level.SEVERE,
                            "Can not add link to resource "
                            + manualRef.value(),
                            ex);
                }
            }
        });
        return resource;
    }

}
