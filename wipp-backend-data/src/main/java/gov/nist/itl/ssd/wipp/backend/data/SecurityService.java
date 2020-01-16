package gov.nist.itl.ssd.wipp.backend.data;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.ClientException;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.NotFoundException;
import gov.nist.itl.ssd.wipp.backend.data.csvCollection.CsvCollection;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.ImagesCollection;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.ImagesCollectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;


@Service
public class SecurityService {
    @Autowired
    private ImagesCollectionRepository imagesCollectionRepository;

    public boolean hasAccess(int parameter) { //test
        return parameter == 1;
    }
    public boolean checkAuthorize(String imagesCollectionId){
        System.out.println(imagesCollectionId);
        Optional<ImagesCollection> result = imagesCollectionRepository.findById(
                imagesCollectionId);
        if (!result.isPresent()) {
            return false;
        }
        boolean var1 = !result.get().isPubliclyAvailable();
        String var2 = result.get().getOwner();
        String var3 = SecurityContextHolder.getContext().getAuthentication().getName();
        if (var1 && var2 != null && !var2.equals(var3)){

            return false;

        }
        return true;

    }

    public static boolean checkAuthorize(ImagesCollection imagesCollection){
        boolean var1 = !imagesCollection.isPubliclyAvailable();
        String var2 = imagesCollection.getOwner();
        String var3 = SecurityContextHolder.getContext().getAuthentication().getName();
        if (var1 && var2 != null && !var2.equals(var3)){
            return false;
        }
        return true;
    }

    public static boolean checkAuthorize(CsvCollection csvCollection){
        boolean var1 = !csvCollection.isPubliclyAvailable();
        String var2 = csvCollection.getOwner();
        String var3 = SecurityContextHolder.getContext().getAuthentication().getName();
        if (var1 && var2 != null && !var2.equals(var3)){
            return false;
        }
        return true;
    }
}