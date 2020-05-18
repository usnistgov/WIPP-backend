package gov.nist.itl.ssd.wipp.backend.core.model.auth;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PostAuthorize;

import java.io.Serializable;
import java.util.Optional;

/**
 * PrincipalFilteredRepository based on {@link MongoRepository}
 * Exposed Read methods are secured
 * Other exposed actions are secured via Repository Event Handlers
 * 
 * @author Mylene Simon <mylene.simon at nist.gov>
 */
@NoRepositoryBean
public interface PrincipalFilteredRepository<T, ID extends Serializable>
        extends MongoRepository<T, ID> {

	/* (non-Javadoc)
	 * @see org.springframework.data.repository.CrudRepository#findById(java.lang.Object)
	 * 
	 * Restrict single resource access to admin and owner if private
	 */
	@Override
	@PostAuthorize("hasRole('admin') "
			+ "or (isAuthenticated() and returnObject?.get()?.owner == principal.name) "
			+ "or returnObject?.get()?.publiclyShared == true")
	Optional<T> findById(ID id);

	/* (non-Javadoc)
	 * @see org.springframework.data.repository.PagingAndSortingRepository#findAll(org.springframework.data.domain.Pageable)
	 * Filter collection resources access depending on user
	 */
	@Override
	@Query("{'$or':["
			+ "{'owner': ?#{ hasRole('admin') ? {$exists:true} : (hasRole('ANONYMOUS') ? '':principal.name)}},"
			+ "{'publiclyShared':true}"
			+ "]}")
	Page<T> findAll(Pageable page);

	/*
	 * Filter collection resources access by object name and depending on user
	 */
    @Query(" { '$and' : ["
    		+ "{'$or':["
    		+ "{'owner': ?#{ hasRole('admin') ? {$exists:true} : (hasRole('ANONYMOUS') ? '':principal.name)}},"
    		+ "	{'publiclyShared':true}"
    		+ "]} , "
    		+ "{'name' : {$regex : '?0', $options: 'i'} } "
    		+ "]}")
    Page<T> findByNameContainingIgnoreCase(
            @Param("name") String name, Pageable p);
	
}
