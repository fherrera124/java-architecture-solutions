package ar.gov.arba.emisiones.repository;

import ar.gov.arba.emisiones.model.DeudaAutomotor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DeudaAutomotorRepository extends JpaRepository<DeudaAutomotor, String> {

    @Query("SELECT d FROM DeudaAutomotor d WHERE d.fechaVencimiento BETWEEN :startDate AND :endDate")
    List<DeudaAutomotor> findDeudasProximasAVencer(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}