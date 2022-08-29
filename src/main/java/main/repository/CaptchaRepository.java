package main.repository;

import main.model.CaptchaCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CaptchaRepository extends JpaRepository<CaptchaCode, Integer> {
    Optional<CaptchaCode> findByCode (String code);
}
