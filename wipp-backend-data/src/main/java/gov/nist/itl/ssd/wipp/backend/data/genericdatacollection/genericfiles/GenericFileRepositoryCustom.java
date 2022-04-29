package gov.nist.itl.ssd.wipp.backend.data.genericdatacollection.genericfiles;

import org.springframework.data.repository.query.Param;

/**
*
* @author Mohamed Ouladi <mohamed.ouladi at labshare.org>
*/
public interface GenericFileRepositoryCustom {
	
    void deleteByGenericDataCollection(
            @Param("genericDataCollection") String genericDataCollection);

    void deleteByGenericDataCollectionAndFileName(
            @Param("genericDataCollection") String genericDataCollection,
            @Param("fileName") String fileName);

}
