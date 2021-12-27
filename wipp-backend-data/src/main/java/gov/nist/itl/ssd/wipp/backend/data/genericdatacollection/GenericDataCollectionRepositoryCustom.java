package gov.nist.itl.ssd.wipp.backend.data.genericdatacollection;

import org.springframework.data.repository.query.Param;

/**
*
* @author Mohamed Ouladi <mohamed.ouladi at labshare.org>
*/
public interface GenericDataCollectionRepositoryCustom {
	
	// not exported
    void updateGenericFilesCaches(@Param("genericDataCollectionId") String genericDataCollectionId);

}
