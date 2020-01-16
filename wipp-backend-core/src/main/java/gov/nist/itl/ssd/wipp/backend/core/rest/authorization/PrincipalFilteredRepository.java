package gov.nist.itl.ssd.wipp.backend.core.rest.authorization;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.io.Serializable;

/**
 * Base interface for protected repositories.
 * Authenticated users can only access their own data.
 *
 * @author Antoine Gerardin <antoine.gerardin@nist.gov> @2017
 */
@NoRepositoryBean
public interface PrincipalFilteredRepository<T, ID extends Serializable>
        extends MongoRepository<T, ID> {

    /* Called when accessing the repository to get all objects */
    @RestrictedAccess
    @Query(" { '$or' : [ {owner: ?#{ principal?.name }}, { 'publiclyAvailable' : true } ] }")
    @Override
    Page<T> findAll(Pageable p);

    @RestrictedAccess
    @Query(" { '$and' : [ {'owner' : ?#{ principal?.name }} , {'name' : {$regex : '?0', $options: 'i'} } ] }")
    Page<T> findByNameContainingIgnoreCase(
            @Param("name") String name, Pageable p);
    /*
     * Delete cannot be protected in this repository because it does not provide any access to the 44ImageCollection
     * for @PreAuthorize or @PostAuthorize. We need to rely on a separate handler.
     */

//    @Override
//    @Query(" { '$and' : [ {'ownerId' : ?#{ principal?.name }} , {'_id' : {$lt : ?0}} ] }")
//    T findOne(ID id);
}