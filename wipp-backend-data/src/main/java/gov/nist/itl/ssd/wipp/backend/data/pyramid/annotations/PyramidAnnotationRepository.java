package gov.nist.itl.ssd.wipp.backend.data.pyramid.annotations;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import gov.nist.itl.ssd.wipp.backend.data.pyramid.Pyramid;


@RepositoryRestResource
public interface PyramidAnnotationRepository extends MongoRepository<PyramidAnnotation, String> {

	@Override
    @RestResource(exported = false)
    <S extends PyramidAnnotation> S save(S s);

    @Override
    @RestResource(exported = false)
    void delete(PyramidAnnotation t);

    Page<Pyramid> findByNameContainingIgnoreCase(@Param("name") String name,
            Pageable p);
}
