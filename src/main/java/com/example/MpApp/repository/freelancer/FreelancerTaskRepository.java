package com.example.MpApp.repository.freelancer;

import com.example.MpApp.entity.freelancer.FreelancerTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface FreelancerTaskRepository extends JpaRepository<FreelancerTask, Long> {

    @Query("SELECT ft FROM FreelancerTask ft JOIN ft.freelancers f WHERE f.id = :freelancerId")
    List<FreelancerTask> findByFreelancerId(@Param("freelancerId") Long freelancerId);
}