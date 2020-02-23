package gov.nist.itl.ssd.wipp.backend.data;

import com.mongodb.lang.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.io.Serializable;

@NoRepositoryBean
public interface PrincipalFilteredRepository<T, ID extends Serializable>
        extends MongoRepository<T, ID> {

    /* Called when accessing the repository to get all objects
    * The @Query annotation is used to exclude objects which should not be accessible by the user doing the request
    *  */


    // The Query below is left as a showcase of how retrocompatibility could be achieved (owner == null)
    //@Query("{'$or':[{'owner':null},{'owner': ?#{ principal=='anonymousUser' ? '':principal.name}},{'publiclyAvailable':true}]}")
    @NonNull
    @Query("{'$or':[{'owner': ?#{ principal=='anonymousUser' ? '':principal.name}},{'publiclyAvailable':true}]}")
    Page<T> findAll(@NonNull Pageable p);

    @Query(" { '$and' : [{'$or':[{'owner': ?#{ principal=='anonymousUser' ? '':principal.name}},{'publiclyAvailable':true}]} , {'name' : {$eq : ?0}} ] }")
    Page<T> findByName(@Param("name") String name, Pageable p);

    @Query(" { '$and' : [{'$or':[{'owner': ?#{ principal=='anonymousUser' ? '':principal.name}},{'publiclyAvailable':true}]} , {'name' : {$regex : '?0', $options: 'i'} } ] }")
    Page<T> findByNameContainingIgnoreCase(
            @Param("name") String name, Pageable p);
}
