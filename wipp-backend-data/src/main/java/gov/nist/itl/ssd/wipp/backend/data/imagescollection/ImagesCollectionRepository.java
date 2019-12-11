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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 *
 * @author Antoine Vandecreme <antoine.vandecreme at nist.gov>
 */
@RepositoryRestResource
public interface ImagesCollectionRepository
        extends MongoRepository<ImagesCollection, String>,
        ImagesCollectionRepositoryCustom {

    Page<ImagesCollection> findByName(@Param("name") String name, Pageable p);

    Page<ImagesCollection> findByTags(@Param("tags") String tag, Pageable p);

    Page<ImagesCollection> findByTagsContainingIgnoreCase(@Param("tags") String tag, Pageable p);

    Page<ImagesCollection> findByNameContainingIgnoreCase(
            @Param("name") String name, Pageable p);

    Page<ImagesCollection> findByNameContainingIgnoreCaseAndNumberOfImages(
            @Param("name") String name,
            @Param("numberOfImages") Integer numberOfImages,
            Pageable p);

    long countByName(@Param("name") String name);

}
