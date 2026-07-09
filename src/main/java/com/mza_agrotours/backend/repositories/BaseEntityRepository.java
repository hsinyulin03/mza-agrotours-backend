package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.entities.BaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

@NoRepositoryBean
public interface BaseEntityRepository<E extends BaseEntity, ID extends Serializable> extends JpaRepository<E, ID> {
}
