package gov.nist.itl.ssd.wipp.backend.data.genericdata;

import org.springframework.data.repository.query.Param;

/**
*
* @author Mohamed Ouladi <mohamed.ouladi at labshare.org>
*/
public interface GenericDataRepositoryCustom {
	
	// not exported
    void updateGenericFilesCaches(@Param("genericDataId") String genericDataId);

}
