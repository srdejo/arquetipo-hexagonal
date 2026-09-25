package co.com.srdejo.infrastructure.out.jpa.repository;

import co.com.srdejo.infrastructure.out.jpa.entity.ObjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IObjectRepository extends JpaRepository<ObjectEntity, Long> {

}