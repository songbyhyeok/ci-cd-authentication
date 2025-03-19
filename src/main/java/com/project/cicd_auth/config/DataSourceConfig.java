package com.project.cicd_auth.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;

import javax.sql.DataSource;

@Configuration
@Order(2)
public class DataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);

    private final SshTunnelingConfig sshTunnelingConfig;

    @Value("${RDS_SCHEMA}")
    private String schemaName;

    /**
     * 생성자를 통한 의존성 주입
     *
     * @param sshTunnelingConfig SSH 터널링 설정 객체
     */
    public DataSourceConfig(SshTunnelingConfig sshTunnelingConfig) {
        this.sshTunnelingConfig = sshTunnelingConfig;
    }

    /**
     * SSH 터널링을 통해 접근 가능한 데이터소스를 구성합니다.
     *
     * @param properties 데이터소스 속성
     * @return 구성된 DataSource 객체
     */
    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties properties) {
        try {
            // SSH 터널 설정 및 로컬로 포워딩된 포트 가져오기
            int forwardedPort = sshTunnelingConfig.setupSshTunnel();

            // 올바른 포워딩 포트로 JDBC URL 구성
            String jdbcUrl = String.format("jdbc:mysql://localhost:%d/%s?useSSL=false&serverTimezone=UTC",
                    forwardedPort, schemaName);

            log.info("SSH 터널을 통한 JDBC URL 생성: {}", jdbcUrl);

            // DataSource 구성 및 반환
            return DataSourceBuilder.create()
                    .url(jdbcUrl)
                    .username(properties.getUsername())
                    .password(properties.getPassword())
                    .driverClassName(properties.getDriverClassName())
                    .build();
        } catch (Exception e) {
            log.error("데이터베이스 연결을 위한 SSH 터널 설정 실패", e);
            throw new RuntimeException("데이터베이스 연결 실패: " + e.getMessage(), e);
        }
    }
}
