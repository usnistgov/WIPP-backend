package gov.nist.itl.ssd.wipp.backend.data.genericdata.genericfiles;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
*
* @author Mohamed Ouladi <mohamed.ouladi at labshare.org>
*/
@RepositoryRestResource(exported = false)
public interface GenericFileRepository extends MongoRepository<GenericFile, String>, GenericFileRepositoryCustom {
	
    List<GenericFile> findByGenericData(String genericData);

    Page<GenericFile> findByGenericData(String genericData, Pageable p);

    List<GenericFile> findByGenericDataAndFileNameRegex(String genericData, String fileName);

    Page<GenericFile> findByGenericDataAndFileNameRegex(String genericData, String fileName, Pageable p);

    List<GenericFile> findByImporting(boolean importing);

}
