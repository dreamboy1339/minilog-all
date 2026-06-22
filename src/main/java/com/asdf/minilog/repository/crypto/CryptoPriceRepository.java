package com.asdf.minilog.repository.crypto;

import com.asdf.minilog.entity.crypto.CryptoPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** 암호화폐 시세(CryptoPrice) 엔티티를 관리하는 리포지토리. */
@Repository
public interface CryptoPriceRepository extends JpaRepository<CryptoPrice, Long> {}
