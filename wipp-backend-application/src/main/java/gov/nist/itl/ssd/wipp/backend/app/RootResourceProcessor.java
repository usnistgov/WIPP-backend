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
package gov.nist.itl.ssd.wipp.backend.app;

import gov.nist.itl.ssd.wipp.backend.Application;
import gov.nist.itl.ssd.wipp.backend.core.rest.annotation.IdExposed;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;

/**
 * @author Mylene Simon <mylene.simon at nist.gov>
 *
 */
@Component
public class RootResourceProcessor
        implements ResourceProcessor<RepositoryLinksResource> {

    @Autowired
    private EntityLinks entityLinks;

    private Collection<Link> links;

    /**
     * Add /resource/{id} to root links
     *
     * @param resource
     * @return
     */
    @Override
    public RepositoryLinksResource process(RepositoryLinksResource resource) {

        resource.add(getLinks());
        return resource;
    }

    private synchronized Collection<Link> getLinks() {
        if (links == null) {
            ClassPathScanningCandidateComponentProvider scanner
                    = new ClassPathScanningCandidateComponentProvider(false);
            scanner.addIncludeFilter(new AnnotationTypeFilter(IdExposed.class));
            Set<BeanDefinition> bds = scanner.findCandidateComponents(
                    Application.class.getPackage().getName());
            links = new ArrayList<>(bds.size());
            for (BeanDefinition bd : bds) {
                String beanClassName = bd.getBeanClassName();
                try {
                    Class clazz = Class.forName(beanClassName);
                    URI uri = entityLinks.linkFor(clazz).toUri();
                    Link link = new Link(uri + "/{id}",
                            toCamelCase(clazz.getSimpleName()));
                    links.add(link);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(Application.class.getName()).log(
                            Level.SEVERE,
                            "Can not add root link for class " + beanClassName,
                            ex);
                }
            }
        }
        return links;
    }

    private static String toCamelCase(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        char[] array = str.toCharArray();
        array[0] = Character.toLowerCase(array[0]);
        return new String(array);
    }

}
