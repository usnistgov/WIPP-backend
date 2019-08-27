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
package gov.nist.itl.ssd.wipp.backend;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ServiceLocatorFactoryBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.hateoas.config.EnableEntityLinks;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import gov.nist.itl.ssd.wipp.backend.core.model.data.DataHandlerFactory;
import gov.nist.itl.ssd.wipp.backend.core.rest.annotation.IdExposed;
import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;


/**
 *
 * @author Mylene Simon <mylene.simon at nist.gov>
 */
@Configuration
@ComponentScan(basePackages = {"gov.nist.itl.ssd.wipp.backend"})
@EnableAutoConfiguration
@EnableEntityLinks
@EnableWebMvc
@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
public class Application implements WebMvcConfigurer {
	
	@Autowired
    private CoreConfig coreConfig;

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(
                Application.class, args);
        initSpring(ctx);
    }

    private static void initSpring(ConfigurableApplicationContext ctx) {
        RepositoryRestConfiguration restConf = ctx.getBean(
                RepositoryRestConfiguration.class);

        // TODO: check this support in Angular 6
        // We want the POST requests to return the newly constructed object,
        // because Angular does not support reading from the Location header yet.
        restConf.setReturnBodyOnCreate(true);

        // Expose ids of classes annotated @IdExposed
        ClassPathScanningCandidateComponentProvider scanner
                = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(IdExposed.class));
        for (BeanDefinition bd : scanner.findCandidateComponents(
                Application.class.getPackage().getName())) {
            String beanClassName = bd.getBeanClassName();
            try {
                restConf.exposeIdsFor(Class.forName(beanClassName));
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(Application.class.getName()).log(Level.SEVERE,
                        "Can not expose ids for class " + beanClassName, ex);
            }
        }
    }

    @Bean
    public ServiceLocatorFactoryBean serviceLocatorBean(){
        ServiceLocatorFactoryBean bean = new ServiceLocatorFactoryBean();
        bean.setServiceLocatorInterface(DataHandlerFactory.class);
        return bean;
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
    	// Force creation of pyramids folder to avoid pyramids viewing being
    	// unavailable at first launch 
    	File pyramidsFolderFile = new File(coreConfig.getPyramidsFolder());
    	if(! pyramidsFolderFile.exists()) {
    		pyramidsFolderFile.mkdirs();
    	}
    	// Add pyramid resource handler
    	registry.addResourceHandler(
                CoreConfig.PYRAMIDS_BASE_URI + "/**").
                addResourceLocations(
                		pyramidsFolderFile
                        .toURI().toString());
    }

}
