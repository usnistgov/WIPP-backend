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
package gov.nist.itl.ssd.wipp.backend.data.imagescollection;

import gov.nist.itl.ssd.wipp.backend.core.model.auth.PrincipalFilteredRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface ImagesCollectionRepository
        extends PrincipalFilteredRepository<ImagesCollection, String>,
        ImagesCollectionRepositoryCustom {

	/*
	 * Filter collection resources access by object name depending on user
	 */
	@Query(" { '$and' : ["
			+ "{'$or':["
			+ "{'owner': ?#{ hasRole('admin') ? {$exists:true} : (hasRole('ANONYMOUS') ? '':principal.name)}},"
			+ "{'publiclyShared':true}"
			+ "]} , "
			+ "{'name' : {$eq : ?0}}"
			+ "]}")
    Page<ImagesCollection> findByName(@Param("name") String name, Pageable p);
	
	/*
	 * Filter collection resources access by name, number of images and depending on user
	 */
	@Query(" { '$and' : ["
    		+ "{'$or':["
    		+ "{'owner': ?#{ hasRole('admin') ? {$exists:true} : (hasRole('ANONYMOUS') ? '':principal.name)}},"
    		+ "	{'publiclyShared':true}"
    		+ "]} , "
    		+ "{'name' : {$regex : '?0', $options: 'i'}}, {'numberOfImages' : {$eq : ?1}}"
    		+ "]}")
    Page<ImagesCollection> findByNameContainingIgnoreCaseAndNumberOfImages(
            @Param("name") String name,
            @Param("numberOfImages") Integer numberOfImages,
            Pageable p);

    // Not exported
    long countByName(@Param("name") String name);

}
