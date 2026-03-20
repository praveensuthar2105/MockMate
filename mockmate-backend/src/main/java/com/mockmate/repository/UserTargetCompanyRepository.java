package com.mockmate.repository;

import com.mockmate.model.UserTargetCompany;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserTargetCompanyRepository extends JpaRepository<UserTargetCompany, Long> {
    List<UserTargetCompany> findByUserId(Long userId);
}
