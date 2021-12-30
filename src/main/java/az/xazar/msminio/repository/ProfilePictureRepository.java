package az.xazar.msminio.repository;

import az.xazar.msminio.entity.ProfilePictureEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfilePictureRepository extends JpaRepository<ProfilePictureEntity, Long> {
    Optional<ProfilePictureEntity> findByUserIdAndImageName(Long userId, String imageName);

    Optional<ProfilePictureEntity> findByUserId(Long userId);
}
