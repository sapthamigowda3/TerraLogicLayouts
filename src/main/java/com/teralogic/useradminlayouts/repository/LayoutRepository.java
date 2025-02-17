package com.teralogic.useradminlayouts.repository;

import com.teralogic.useradminlayouts.models.Layout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LayoutRepository extends JpaRepository<Layout, Long> {

    Optional<Layout> findAllById(Integer id);
    // Additional query methods can be defined here if needed
}