package gov.nist.itl.ssd.wipp.backend.data.genericdata.genericfiles;

import org.springframework.data.repository.query.Param;

/**
*
* @author Mohamed Ouladi <mohamed.ouladi at labshare.org>
*/
public interface GenericFileRepositoryCustom {
	
    void deleteByGenericData(
            @Param("genericData") String genericData);

    void deleteByGenericDataAndFileName(
            @Param("genericData") String genericData,
            @Param("fileName") String fileName);

}
