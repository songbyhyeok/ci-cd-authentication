package com.project.cicd_auth.config;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import java.util.Properties;

@Component
public class SshTunnelingConfig {

    private static final Logger log = LoggerFactory.getLogger(SshTunnelingConfig.class);

    private Session sshSession;

    // application.yml에 구성된 특정 로컬 포트 사용
    @Value("${LOCAL_DB_PORT}")
    private int localPort;

    @Value("${spring.ssh.remote_jump_host}")
    private String remoteHost;

    @Value("${spring.ssh.user}")
    private String sshUser;

    @Value("${spring.ssh.ssh_port}")
    private int sshPort;

    @Value("${spring.ssh.private_key}")
    private String privateKeyPath;

    @Value("${spring.ssh.database_url}")
    private String databaseHost;

    @Value("${spring.ssh.database_port}")
    private int databasePort;

    /**
     * SSH 터널링을 설정하고 로컬 포워딩 포트를 반환합니다.
     *
     * @return 로컬에서 포워딩된 포트 번호
     * @throws JSchException SSH 연결 중 문제가 발생한 경우
     */
    public int setupSshTunnel() throws JSchException {
        JSch jsch = new JSch();

        String formattedKey = privateKeyPath.replace("\\n", "\n");
        jsch.addIdentity("sshKey", formattedKey.getBytes(), null, null);

        log.info("SSH 연결 설정: {}:{} (사용자: {})", remoteHost, sshPort, sshUser);
        Session session = jsch.getSession(sshUser, remoteHost, sshPort);

        // 엄격한 호스트 키 검사 비활성화
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);

        log.info("SSH 서버에 연결 중...");
        session.connect(30000); // 30초 타임아웃

        // 나중에 정리를 위해 세션 저장
        this.sshSession = session;

        log.info("포트 포워딩 설정: localhost:{} -> {}:{}", localPort, databaseHost, databasePort);
        session.setPortForwardingL(localPort, databaseHost, databasePort);

        log.info("SSH 터널이 로컬 포트 {}에 성공적으로 설정되었습니다", localPort);
        return localPort;
    }

    /**
     * 애플리케이션 종료 시 SSH 세션을 정리합니다.
     */
    @PreDestroy
    public void closeSSH() {
        if (sshSession != null && sshSession.isConnected()) {
            log.info("SSH 터널 닫는 중...");
            sshSession.disconnect();
            log.info("SSH 터널이 닫혔습니다");
        }
    }
}
