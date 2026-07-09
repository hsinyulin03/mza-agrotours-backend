package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.entities.BaseEntity;
import com.mza_agrotours.backend.repositories.BaseEntityRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

@Service
public abstract class BaseEntityServiceImpl<E extends BaseEntity, ID extends Serializable> implements BaseEntityService<E,ID> {
    @Autowired
    protected BaseEntityRepository<E, ID> baseEntityRepository;

    @Override
    @Transactional
    public List<E> findAll() throws Exception {
        try {
            return baseEntityRepository.findAll();
        } catch(Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    @Transactional
    public Page<E> findAll(Pageable pageable) throws Exception {
        try {
            return baseEntityRepository.findAll(pageable);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    @Transactional
    public E findById(ID id) throws Exception {
        try {
            Optional<E> entityOptional = baseEntityRepository.findById(id);
            return entityOptional.get();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    @Transactional
    public E save(E entity) throws Exception {
        try {
            return baseEntityRepository.save(entity);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    //TODO- Revisar, parece que no hace update correctamente, genera una nueva instancia
    @Override
    @Transactional
    public E update(ID id, E entity) throws Exception {
        try {
            Optional<E> entityOptional = baseEntityRepository.findById(id);
            if (entityOptional.isEmpty()) throw new Exception("No se encuentra la entidad");
            return baseEntityRepository.save(entity);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean delete(ID id) throws Exception {
        try {
            if(baseEntityRepository.existsById(id)) {
                baseEntityRepository.deleteById(id);
                return true;
            }
            throw new Exception();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
}
