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
package gov.nist.itl.ssd.wipp.backend.data.pyramidannotation;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.prepost.PostAuthorize;
import gov.nist.itl.ssd.wipp.backend.core.model.auth.PrincipalFilteredRepository;


/**
 * @author Mohamed Ouladi <mohamed.ouladi at nist.gov>
 * @author Mylene Simon <mylene.simon at nist.gov>
 */
@Tag(name="PyramidAnnotation Entity")
@RepositoryRestResource
public interface PyramidAnnotationRepository extends PrincipalFilteredRepository<PyramidAnnotation, String>, PyramidAnnotationRepositoryCustom {

    @Override
    @RestResource(exported = false)
    void delete(PyramidAnnotation t);

    @PostAuthorize("hasRole('admin') "
			+ "or (isAuthenticated() and returnObject?.owner == authentication.name) "
			+ "or returnObject?.publiclyShared == true")
    PyramidAnnotation findByPyramid(@Param("pyramid") String pyramid);

}
