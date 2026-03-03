package tr.org.lider.repositories;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tr.org.lider.entities.RefreshTokenImpl;
import java.util.Date;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenImpl, Long> {
    RefreshTokenImpl findByToken(String token);

    @Transactional
    void deleteByToken(String token);

    Page<RefreshTokenImpl> findByExpiryDateBefore(Date date, Pageable pageable);

    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshTokenImpl r WHERE r.username = :username")
    void deleteByUsername(@Param("username") String username);
}