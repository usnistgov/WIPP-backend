package gov.nist.itl.ssd.wipp.backend.core.model;

import com.mongodb.lang.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;

import java.io.Serializable;

@NoRepositoryBean
public interface PrincipalFilteredRepository<T, ID extends Serializable>
        extends MongoRepository<T, ID> {

    /* Called when accessing the repository to get all objects
     * The @Query annotation is used to exclude objects which should not be accessible by the user doing the request
     *  */
    @Query("{'owner': ?#{ principal=='anonymousUser' ? '':principal.name}}")
    @NonNull
    Page<T> findAll(@NonNull Pageable p);

    @Query(" { '$and' : [{'owner': ?#{ principal=='anonymousUser' ? '':principal.name}} , {'name' : {$eq : ?0}} ] }")
    Page<T> findByName(@Param("name") String name, Pageable p);

    @Query(" { '$and' : [{'owner': ?#{ principal=='anonymousUser' ? '':principal.name}} , {'name' : {$regex : '?0', $options: 'i'} } ] }")
    Page<T> findByNameContainingIgnoreCase(
            @Param("name") String name, Pageable p);

    @Query(" { '$and' : [{'owner': ?#{ principal=='anonymousUser' ? '':principal.name}}, {'name' : {$regex : '?0', $options: 'i'} } , {'numberOfImages' : {$eq : ?1}} ] }")
    Page<T> findByNameContainingIgnoreCaseAndNumberOfImages(
            @Param("name") String name,
            @Param("numberOfImages") Integer numberOfImages,
            Pageable p);
}
