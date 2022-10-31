/*
 * This software was developed at the National Institute of Standards and
 * Technology by employees of the Federal Government in the course of
 * their official duties. Pursuant to title 17 Section 105 of the United
 * States Code this software is not subject to copyright protection and is
 * in the public domain. This software is an experimental system. NIST assumes
 * no responsibility whatsoever for its use by other parties, and makes no
 * guarantees, expressed or implied, about its quality, reliability, or
 * any other characteristic. We would appreciate acknowledgement if the
 * software is used.
 */
package gov.nist.itl.ssd.wipp.backend.core.model.computation;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import gov.nist.itl.ssd.wipp.backend.core.rest.exception.NotFoundException;

/**
 * Plugin Repository Event Handler
 * All creation/modification/deletion actions via REST are restricted to
 * users with role 'admin' or 'developer'
 * 
 * @author Mylene Simon <mylene.simon at nist.gov>
 *
 */
@Component
@RepositoryEventHandler
public class PluginEventHandler {
	
	@Autowired
	PluginRepository pluginRepository;
	
    @HandleBeforeCreate
	@PreAuthorize("hasRole('admin') or hasRole('developer')")
    public void handleBeforeCreate(Plugin plugin) {
        // TODO: Plugin JSON validation against schema
    }

    @HandleBeforeSave
    @PreAuthorize("hasRole('admin') or hasRole('developer')")
    public void handleBeforeSave(Plugin plugin) {
        Optional<Plugin> result = pluginRepository.findById(
                plugin.getId());
    	if (!result.isPresent()) {
        	throw new NotFoundException("Plugin with id " + plugin.getId() + " not found");
        }
    }

    @HandleBeforeDelete
    @PreAuthorize("hasRole('admin') or hasRole('developer')")
    public void handleBeforeDelete(Plugin plugin) {
    	Optional<Plugin> result = pluginRepository.findById(
                plugin.getId());
    	if (!result.isPresent()) {
        	throw new NotFoundException("Plugin with id " + plugin.getId() + " not found");
        }
    }

}
