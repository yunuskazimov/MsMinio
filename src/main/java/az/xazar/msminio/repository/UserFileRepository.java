package az.xazar.msminio.repository;

import az.xazar.msminio.entity.UsersFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserFileRepository extends JpaRepository<UsersFileEntity, Long> {
    Optional<UsersFileEntity> findAllByUserIdAndFileName(Long userId, String fileName);
}
