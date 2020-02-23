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
package gov.nist.itl.ssd.wipp.backend.data.imagescollection.images;

import java.util.List;
import java.util.Optional;

import com.mongodb.lang.NonNull;
import gov.nist.itl.ssd.wipp.backend.data.PrincipalFilteredRepository;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.metadatafiles.MetadataFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 *
 * @author Antoine Vandecreme
 */
@RepositoryRestResource(exported = false)
public interface ImageRepository extends PrincipalFilteredRepository<Image, String>,
        ImageRepositoryCustom {

    // We make sure the user has access to the image collection before calling the findByImagesCollection method
    @PreAuthorize("@securityServiceData.checkAuthorizeImagesCollectionId(#imagesCollection)")
    List<Image> findByImagesCollection(@Param("imagesCollection") String imagesCollection);

    // We make sure the user has access to the image collection before calling the findByImagesCollection method
    @PreAuthorize("@securityServiceData.checkAuthorizeImagesCollectionId(#imagesCollection)")
    Page<Image> findByImagesCollection(@Param("imagesCollection") String imagesCollection, Pageable p);

    // We make sure the user has access to the image collection before calling the findByImagesCollectionAndFileNameRegex method
    @PreAuthorize("@securityServiceData.checkAuthorizeImagesCollectionId(#imagesCollection)")
    List<Image> findByImagesCollectionAndFileNameRegex(@Param("imagesCollection") String imagesCollection, @Param("filename") String fileName);

    // We make sure the user has access to the image collection before calling the findByImagesCollectionAndFileNameRegex method
    @PreAuthorize("@securityServiceData.checkAuthorizeImagesCollectionId(#imagesCollection)")
    Page<Image> findByImagesCollectionAndFileNameRegex(@Param("imagesCollection") String imagesCollection, @Param("filename") String fileName, Pageable p);

    // TODO : add a @Query ?
    List<Image> findByImporting(boolean importing);

    @Override
    // the findById method corresponds to a GET operation on a specific object. We can not use @PreAuthorize on the object's Id, as checkAuthorizeImageId() in SecurityServiceData
    // calls the findById method. Therefore, we use a @PostAuthorize on the object returned by the findById method. If the user is not allowed to GET the object, the object won't be
    // returned and an ForbiddenException will be thrown
    @PostAuthorize("@securityServiceData.checkAuthorize(returnObject.get())")
    @NonNull
    Optional<Image> findById(@NonNull String imageId);

    @Override
    @NonNull
    // When calling the save method, which corresponds to a PUT/PATCH operation, we make sure that the user is logged in and has the right to access the object before calling the save method
    @PreAuthorize("@securityServiceData.hasUserRole() and @securityServiceData.checkAuthorize(#s)")
    <S extends Image> S save(@NonNull @Param("s") S s);

    @Override
    // When calling the delete method, which corresponds to a DELETE operation, we make sure that the user is logged in and has the right to access the object before calling the delete method
    @PreAuthorize("@securityServiceData.hasUserRole() and @securityServiceData.checkAuthorize(#image)")
    void delete(@NonNull @Param("image") Image image);

    @Override
    // When calling the deletebyId method, which corresponds to a DELETE operation, we make sure that the user is logged in and has the right to access the object before calling the method
    // The checkAuthorizeImageId() method inside securityServiceData will retrieve the object before checking that the user has the right to access it
    @PreAuthorize("@securityServiceData.hasUserRole() and @securityServiceData.checkAuthorizeImageId(#s)")
    void deleteById(@NonNull @Param("s") String s);

    @Override
    // We make sure the user has access to the image collection and is logged in before calling the deleteByImagesCollectionAndFileName method
    @PreAuthorize("@securityServiceData.hasUserRole() and @securityServiceData.checkAuthorizeImagesCollectionId(#imagesCollection)")
    void deleteByImagesCollectionAndFileName(@Param("imagesCollection") String imagesCollection, @Param("fileName") String fileName);

    @Override
    // We make sure the user has access to the image collection and is logged in before calling the deleteByImagesCollection method
    @PreAuthorize("@securityServiceData.hasUserRole() and @securityServiceData.checkAuthorizeImagesCollectionId(#imagesCollection)")
    void deleteByImagesCollection(@Param("imagesCollection") String imagesCollection);
}
