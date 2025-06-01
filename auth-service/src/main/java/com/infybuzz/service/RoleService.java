package com.infybuzz.service;

import com.infybuzz.entity.Role;
import com.infybuzz.exceptions.AuthAPIException;
import com.infybuzz.exceptions.ResourceNotFoundException;
import com.infybuzz.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService {

    private final RoleRepository roleRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Role createRole(Role role) {
        Role newRole = roleRepository.findById(role.getId())
                .orElseThrow(() -> new AuthAPIException(HttpStatus.NOT_FOUND, "Role not found"));

        return roleRepository.save(newRole);
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public Role getRoleById(Long id) {
       return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));
    }

    public void deleteRole(Long id) {
       Role role = roleRepository.findById(id)
               .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));

       roleRepository.delete(role);
    }
}
