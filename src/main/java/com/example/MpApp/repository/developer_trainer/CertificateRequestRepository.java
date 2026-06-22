package com.example.MpApp.repository.developer_trainer;

import com.example.MpApp.entity.developer_trainer_staff.CertificateRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CertificateRequestRepository
        extends JpaRepository<CertificateRequest,Long> {

}