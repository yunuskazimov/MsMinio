package az.xazar.msminio.repository;

import az.xazar.msminio.entity.UsersFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserFileRepository extends JpaRepository<UsersFileEntity, Long> {
}
