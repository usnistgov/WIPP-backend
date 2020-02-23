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
package gov.nist.itl.ssd.wipp.backend.data.csvCollection.csv;


import com.mongodb.lang.NonNull;
import gov.nist.itl.ssd.wipp.backend.data.PrincipalFilteredRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Optional;

/**
 * @author Samia Benjida <samia.benjida at nist.gov>
 */
@RepositoryRestResource(exported = false)
public interface CsvRepository extends PrincipalFilteredRepository<Csv, String>, CsvRepositoryCustom {

    // We make sure the user has access to the csv collection before calling the findByCsvCollection method
    @PreAuthorize("@securityServiceData.checkAuthorizeCsvCollectionId(#csvCollection, false)")
    List<Csv> findByCsvCollection(@Param("csvCollection") String csvCollection);

    // We make sure the user has access to the csv collection before calling the findByCsvCollection method
    @PreAuthorize("@securityServiceData.checkAuthorizeCsvCollectionId(#csvCollection, false)")
    Page<Csv> findByCsvCollection(@Param("csvCollection") String csvCollection, Pageable p);

    // We make sure the user has access to the csv collection before calling the findByCsvCollectionAndFileNameRegex method
    @PreAuthorize("@securityServiceData.checkAuthorizeCsvCollectionId(#csvCollection, false)")
    List<Csv> findByCsvCollectionAndFileNameRegex(@Param("csvCollection") String csvCollection, @Param("filename") String fileName);

    // We make sure the user has access to the csv collection before calling the findByCsvCollectionAndFileNameRegex method
    @PreAuthorize("@securityServiceData.checkAuthorizeCsvCollectionId(#csvCollection, false)")
    Page<Csv> findByCsvCollectionAndFileNameRegex(@Param("csvCollection") String csvCollection, @Param("filename") String fileName, Pageable p);

    // TODO : add a @Query ?
    List<Csv> findByImporting(boolean importing);

    @Override
    // the findById method corresponds to a GET operation on a specific object. We can not use @PreAuthorize on the object's Id, as checkAuthorizeCsvId() in SecurityServiceData
    // calls the findById method. Therefore, we use a @PostAuthorize on the object returned by the findById method. If the user is not allowed to GET the object, the object won't be
    // returned and an ForbiddenException will be thrown
    @PostAuthorize("@securityServiceData.checkAuthorize(returnObject.get(), false)")
    @NonNull
    Optional<Csv> findById(@NonNull String imageId);

    @Override
    @NonNull
    // When calling the save method, which corresponds to a PUT/PATCH operation, we make sure that the user is logged in and has the right to access the object before calling the save method
    @PreAuthorize("@securityServiceData.hasUserRole() and @securityServiceData.checkAuthorize(#s, true)")
    <S extends Csv> S save(@NonNull @Param("s") S s);

    @Override
    // When calling the delete method, which corresponds to a DELETE operation, we make sure that the user is logged in and has the right to access the object before calling the delete method
    @PreAuthorize("@securityServiceData.hasUserRole() and @securityServiceData.checkAuthorize(#csv, true)")
    void delete(@NonNull @Param("csv") Csv csv);

    @Override
    // When calling the deletebyId method, which corresponds to a DELETE operation, we make sure that the user is logged in and has the right to access the object before calling the method
    // The checkAuthorizeCsvId() method inside securityServiceData will retrieve the object before checking that the user has the right to access it
    @PreAuthorize("@securityServiceData.hasUserRole() and @securityServiceData.checkAuthorizeCsvId(#s, true)")
    void deleteById(@NonNull @Param("s") String s);
}