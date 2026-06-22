package com.example.MpApp.repository.developer_trainer;

import com.example.MpApp.entity.developer_trainer_staff.CashFeeConfirmation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CashFeeConfirmationRepository
        extends JpaRepository<CashFeeConfirmation,Long> {

}