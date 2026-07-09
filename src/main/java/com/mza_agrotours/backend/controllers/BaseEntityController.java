package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.entities.BaseEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.Serializable;

public interface BaseEntityController<E extends BaseEntity, ID extends Serializable> {
    ResponseEntity<?> getAll();
    ResponseEntity<?> getAll(Pageable pageable);
    ResponseEntity<?> getOne(@PathVariable ID id);
    ResponseEntity<?> save(@RequestBody E entity);
    ResponseEntity<?> update(@PathVariable ID id, @RequestBody E entity);
    ResponseEntity<?> delete(@PathVariable ID id);
}
