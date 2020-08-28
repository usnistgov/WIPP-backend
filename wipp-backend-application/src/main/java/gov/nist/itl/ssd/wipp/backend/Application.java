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
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import gov.nist.itl.ssd.wipp.backend.core.model.data.DataHandlerFactory;
import gov.nist.itl.ssd.wipp.backend.core.rest.annotation.IdExposed;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Tag;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;

/**
 *
 * @author Mylene Simon <mylene.simon at nist.gov>
 */
@Configuration
@ComponentScan(basePackages = {"gov.nist.itl.ssd.wipp.backend"})
@EnableAutoConfiguration
@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
@EnableWebMvc
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
    	// Add Swagger UI resource handler
        registry.
            addResourceHandler(CoreConfig.BASE_URI + "/swagger-ui/**")
            .addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/")
            .resourceChain(false);
    }
    
	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		// Add redirects to move Swagger UI under /api/
		registry.addRedirectViewController(CoreConfig.BASE_URI + "/v2/api-docs", 
				"/v2/api-docs");
		registry.addRedirectViewController(CoreConfig.BASE_URI + "/swagger-resources/configuration/ui",
				"/swagger-resources/configuration/ui");
		registry.addRedirectViewController(CoreConfig.BASE_URI + "/swagger-resources/configuration/security",
				"/swagger-resources/configuration/security");
		registry.addRedirectViewController(CoreConfig.BASE_URI + "/swagger-resources", 
				"/swagger-resources");
		
		registry.addViewController(CoreConfig.BASE_URI + "/swagger-ui/")
        .setViewName("forward:" + CoreConfig.BASE_URI + "/swagger-ui/index.html");
	}
    
    /**
     * Configure Swagger API documentation
     * @return API Documentation configuration
     */
    @Bean
    public Docket wippApi() {
      return new Docket(DocumentationType.SWAGGER_2)
          .select() 
          .apis(RequestHandlerSelectors.any())    
          .paths(PathSelectors.any()) 
          .paths(PathSelectors.regex("/error.*").negate())
          .paths(PathSelectors.regex("/api/profile").negate())
      	  // workaround to avoid duplicate entries for plugins
          .paths(PathSelectors.regex("/api/plugins").negate())
          .build() 
          // manually create tags to manage custom descriptions
          .tags(
              new Tag("CsvCollection Entity", "REST API for CSV Collections"),
              new Tag("ImagesCollection Entity", "REST API for Images Collections"),
              new Tag("Job Entity", "REST API for Jobs"),
              new Tag("Notebook Entity", "REST API for Notebooks"),
              new Tag("Plugin Entity", "REST API for Plugins"),
              new Tag("Pyramid Entity", "REST API for Pyramids"),
              new Tag("PyramidAnnotation Entity", "REST API for Pyramid Annotations"),
              new Tag("StitchingVector Entity", "REST API for Stitching Vectors"),
              new Tag("TensorboardLogs Entity", "REST API for Tensorboard Logs"),
              new Tag("TensorflowModel Entity", "REST API for Tensorflow Models"),
              new Tag("Visualization Entity", "REST API for Pyramid Visualizations"),
              new Tag("Workflow Entity", "REST API for Workflows"))
          .apiInfo(apiEndPointsInfo())
          .enableUrlTemplating(true);
    }
    
    /**
     * Configure Swagger API general information
     * @return API information
     */
    private ApiInfo apiEndPointsInfo() {
        return new ApiInfoBuilder().title("WIPP REST API Documentation")
            .description("Web Image Processing Pipeline REST API")
            .license("NIST Disclaimer")
            .licenseUrl("https://www.nist.gov/disclaimer")
            .version(coreConfig.getWippVersion())
            .build();
    }

}
