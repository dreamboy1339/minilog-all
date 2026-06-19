package com.asdf.minilog.config;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * 메인 데이터소스(minilog_all_db) 구성.
 *
 * <p>{@code entity.main}의 엔티티(User/Article/Follow/TaskReport)와 {@code repository.main}의 리포지토리를 관리하는
 * 기본(primary) 데이터소스, EntityManagerFactory, 트랜잭션 매니저를 정의한다. 별도 한정자 없이 사용하는 {@code @Transactional}은 이
 * 트랜잭션 매니저를 사용한다.
 */
@Configuration
@EnableJpaRepositories(
    basePackages = "com.asdf.minilog.repository.main",
    entityManagerFactoryRef = "entityManagerFactory",
    transactionManagerRef = "transactionManager")
public class MainDataSourceConfig {

  /** {@code spring.datasource.*} 속성을 바인딩한다. */
  @Primary
  @Bean
  @ConfigurationProperties("spring.datasource")
  public DataSourceProperties mainDataSourceProperties() {
    return new DataSourceProperties();
  }

  /** 메인 데이터소스. {@code spring.datasource.hikari.*}로 커넥션 풀을 추가 설정할 수 있다. */
  @Primary
  @Bean
  @ConfigurationProperties("spring.datasource.hikari")
  public DataSource mainDataSource(
      @Qualifier("mainDataSourceProperties") DataSourceProperties properties) {
    return properties.initializeDataSourceBuilder().build();
  }

  /** {@code entity.main} 패키지를 스캔하는 기본 EntityManagerFactory. */
  @Primary
  @Bean(name = "entityManagerFactory")
  public LocalContainerEntityManagerFactoryBean entityManagerFactory(
      EntityManagerFactoryBuilder builder, @Qualifier("mainDataSource") DataSource dataSource) {
    return builder
        .dataSource(dataSource)
        .packages("com.asdf.minilog.entity.main")
        .persistenceUnit("main")
        .build();
  }

  /** 메인 EntityManagerFactory에 연결된 기본 트랜잭션 매니저. */
  @Primary
  @Bean(name = "transactionManager")
  public PlatformTransactionManager transactionManager(
      @Qualifier("entityManagerFactory") EntityManagerFactory entityManagerFactory) {
    return new JpaTransactionManager(entityManagerFactory);
  }
}
