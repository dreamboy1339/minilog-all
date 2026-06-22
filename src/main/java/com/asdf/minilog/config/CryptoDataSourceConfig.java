package com.asdf.minilog.config;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * 암호화폐 시세 데이터소스(crypto_db) 구성.
 *
 * <p>{@code entity.crypto}의 엔티티(CryptoPrice)와 {@code repository.crypto}의 리포지토리를 관리하는 별도 데이터소스,
 * EntityManagerFactory, 트랜잭션 매니저를 정의한다. crypto 리포지토리에 쓰기를 수행하는 스케줄러는
 * {@code @Transactional("cryptoTransactionManager")}로 이 트랜잭션 매니저를 명시해야 한다.
 */
@Configuration
@EnableJpaRepositories(
    basePackages = "com.asdf.minilog.repository.crypto",
    entityManagerFactoryRef = "cryptoEntityManagerFactory",
    transactionManagerRef = "cryptoTransactionManager")
public class CryptoDataSourceConfig {

  /** {@code crypto.datasource.*} 속성을 바인딩한다. */
  @Bean
  @ConfigurationProperties("crypto.datasource")
  public DataSourceProperties cryptoDataSourceProperties() {
    return new DataSourceProperties();
  }

  /** crypto_db 데이터소스. {@code crypto.datasource.hikari.*}로 커넥션 풀을 추가 설정할 수 있다. */
  @Bean
  @ConfigurationProperties("crypto.datasource.hikari")
  public DataSource cryptoDataSource(
      @Qualifier("cryptoDataSourceProperties") DataSourceProperties properties) {
    return properties.initializeDataSourceBuilder().build();
  }

  /** {@code entity.crypto} 패키지를 스캔하는 EntityManagerFactory. */
  @Bean(name = "cryptoEntityManagerFactory")
  public LocalContainerEntityManagerFactoryBean cryptoEntityManagerFactory(
      EntityManagerFactoryBuilder builder, @Qualifier("cryptoDataSource") DataSource dataSource) {
    return builder
        .dataSource(dataSource)
        .packages("com.asdf.minilog.entity.crypto")
        .persistenceUnit("crypto")
        .build();
  }

  /** crypto EntityManagerFactory에 연결된 트랜잭션 매니저. */
  @Bean(name = "cryptoTransactionManager")
  public PlatformTransactionManager cryptoTransactionManager(
      @Qualifier("cryptoEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
    return new JpaTransactionManager(entityManagerFactory);
  }
}
