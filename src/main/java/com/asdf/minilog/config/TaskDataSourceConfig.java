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
 * 보조 데이터소스(task_db) 구성.
 *
 * <p>{@code entity.task}의 엔티티(Device/Task)와 {@code repository.task}의 리포지토리를 관리하는 별도 데이터소스,
 * EntityManagerFactory, 트랜잭션 매니저를 정의한다. task 리포지토리에 쓰기를 수행하는 서비스는
 * {@code @Transactional("taskTransactionManager")}로 이 트랜잭션 매니저를 명시해야 한다.
 */
@Configuration
@EnableJpaRepositories(
    basePackages = "com.asdf.minilog.repository.task",
    entityManagerFactoryRef = "taskEntityManagerFactory",
    transactionManagerRef = "taskTransactionManager")
public class TaskDataSourceConfig {

  /** {@code task.datasource.*} 속성을 바인딩한다. */
  @Bean
  @ConfigurationProperties("task.datasource")
  public DataSourceProperties taskDataSourceProperties() {
    return new DataSourceProperties();
  }

  /** task_db 데이터소스. {@code task.datasource.hikari.*}로 커넥션 풀을 추가 설정할 수 있다. */
  @Bean
  @ConfigurationProperties("task.datasource.hikari")
  public DataSource taskDataSource(
      @Qualifier("taskDataSourceProperties") DataSourceProperties properties) {
    return properties.initializeDataSourceBuilder().build();
  }

  /** {@code entity.task} 패키지를 스캔하는 EntityManagerFactory. */
  @Bean(name = "taskEntityManagerFactory")
  public LocalContainerEntityManagerFactoryBean taskEntityManagerFactory(
      EntityManagerFactoryBuilder builder, @Qualifier("taskDataSource") DataSource dataSource) {
    return builder
        .dataSource(dataSource)
        .packages("com.asdf.minilog.entity.task")
        .persistenceUnit("task")
        .build();
  }

  /** task EntityManagerFactory에 연결된 트랜잭션 매니저. */
  @Bean(name = "taskTransactionManager")
  public PlatformTransactionManager taskTransactionManager(
      @Qualifier("taskEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
    return new JpaTransactionManager(entityManagerFactory);
  }
}
