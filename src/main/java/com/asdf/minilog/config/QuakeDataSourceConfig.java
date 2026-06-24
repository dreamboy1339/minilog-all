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
 * 지진 데이터소스(quake_db) 구성.
 *
 * <p>{@code entity.quake}의 엔티티(Earthquake)와 {@code repository.quake}의 리포지토리를 관리하는 별도 데이터소스,
 * EntityManagerFactory, 트랜잭션 매니저를 정의한다. quake 리포지토리에 쓰기를 수행하는 배치 Step은 {@code
 * quakeTransactionManager}를 사용해야 한다.
 */
@Configuration
@EnableJpaRepositories(
    basePackages = "com.asdf.minilog.repository.quake",
    entityManagerFactoryRef = "quakeEntityManagerFactory",
    transactionManagerRef = "quakeTransactionManager")
public class QuakeDataSourceConfig {

  /** {@code quake.datasource.*} 속성을 바인딩한다. */
  @Bean
  @ConfigurationProperties("quake.datasource")
  public DataSourceProperties quakeDataSourceProperties() {
    return new DataSourceProperties();
  }

  /** quake_db 데이터소스. {@code quake.datasource.hikari.*}로 커넥션 풀을 추가 설정할 수 있다. */
  @Bean
  @ConfigurationProperties("quake.datasource.hikari")
  public DataSource quakeDataSource(
      @Qualifier("quakeDataSourceProperties") DataSourceProperties properties) {
    return properties.initializeDataSourceBuilder().build();
  }

  /** {@code entity.quake} 패키지를 스캔하는 EntityManagerFactory. */
  @Bean(name = "quakeEntityManagerFactory")
  public LocalContainerEntityManagerFactoryBean quakeEntityManagerFactory(
      EntityManagerFactoryBuilder builder, @Qualifier("quakeDataSource") DataSource dataSource) {
    return builder
        .dataSource(dataSource)
        .packages("com.asdf.minilog.entity.quake")
        .persistenceUnit("quake")
        .build();
  }

  /** quake EntityManagerFactory에 연결된 트랜잭션 매니저. */
  @Bean(name = "quakeTransactionManager")
  public PlatformTransactionManager quakeTransactionManager(
      @Qualifier("quakeEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
    return new JpaTransactionManager(entityManagerFactory);
  }
}
