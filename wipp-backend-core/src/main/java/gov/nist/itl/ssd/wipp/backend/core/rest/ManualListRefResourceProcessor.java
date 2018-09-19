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

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.nist.itl.ssd.wipp.backend.core.rest.annotation.ManualListRef;
import gov.nist.itl.ssd.wipp.backend.core.rest.annotation.ManualRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.hateoas.*;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.hal.HalConfiguration;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 *
 */
@Component
public class ManualListRefResourceProcessor
        implements ResourceProcessor<Resource<?>> {

    @Autowired
    private EntityLinks entityLinks;

    private static final Logger logger = Logger.getLogger(
            ManualListRefResourceProcessor.class.getName());

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
            ManualListRef manualListRef = AnnotationUtils.getAnnotation(
                    field, ManualListRef.class);

            if (manualListRef != null) {
                try {
                    field.setAccessible(true);
                    Object fieldValues = field.get(resource.getContent());
                    if (fieldValues != null) {
                        for(Object fieldValue: (List<?>) fieldValues) {
                            Link link = entityLinks.linkToSingleResource(
                                manualListRef.value(),
                                fieldValue
                            );
                            link = link.withRel(field.getName());
                            resource.add(link);
                        }
                    }
                } catch (IllegalArgumentException |
                        IllegalAccessException ex) {
                    logger.log(Level.SEVERE,
                            "Can not add link to resource "
                            + manualListRef.value(),
                            ex);
                }
            }
        });

        return resource;
    }
}
