package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;

import com.jcraft.jsch.ChannelSftp.LsEntry;

@Configuration
public class SftpConfig {

    private final SftpParams sftpParams;

    @Autowired
    public SftpConfig(final SftpParams sftpParams) {
        this.sftpParams = sftpParams;
    }

    @Bean
    public String check() {
        System.out.println(sftpParams.getUser());
        System.out.println(sftpParams.getPassword());
        System.out.println(sftpParams.getLocalDirectoryDownload());
        System.out.println(sftpParams.getRemoteDirectoryDownload());
        System.out.println(sftpParams.getRemoteDirectoryDownloadFilter());
        return null;
    }

    @Bean
    public SessionFactory<LsEntry> sftpSessionFactory() {
        final DefaultSftpSessionFactory factory = new DefaultSftpSessionFactory(true);
        factory.setHost(sftpParams.getHost());
        factory.setPort(sftpParams.getPort());
        factory.setUser(sftpParams.getUser());
        factory.setPassword(sftpParams.getPassword());
        factory.setAllowUnknownKeys(true);
        return new CachingSessionFactory<LsEntry>(factory);
    }

    @Bean
    public SftpRemoteFileTemplate sftpRemoteFileTemplate() {
        return new SftpRemoteFileTemplate(sftpSessionFactory());
    }
//    This advice removes files at remote directory on SFTP server
//    @Bean
//    public ExpressionEvaluatingRequestHandlerAdvice after() {
//        final ExpressionEvaluatingRequestHandlerAdvice advice = new ExpressionEvaluatingRequestHandlerAdvice();
//        advice.setOnSuccessExpressionString("@template.remove(headers['file_remoteDirectory'] + headers['file_remoteFile'])");
//        advice.setPropagateEvaluationFailures(true);
//        return advice;
//    }

}
