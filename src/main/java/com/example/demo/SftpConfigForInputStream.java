package com.example.demo;

import java.io.File;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.sftp.inbound.SftpStreamingMessageSource;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

@Configuration
@ConditionalOnExpression(value = "${inputStreamEnabled}")
public class SftpConfigForInputStream {

    private static final Logger LOGGER = LoggerFactory.getLogger(SftpConfigForInputStream.class);
    private final String remoteDirectoryDownload;
    private final GzipService gzipService;

    @Autowired
    public SftpConfigForInputStream(
            @Value(value = "${sftp.remoteDirectoryDownload}")
            final String remoteDirectoryDownload,
            final GzipService gzipService) {
        this.remoteDirectoryDownload = remoteDirectoryDownload;
        this.gzipService = gzipService;
    }

    @Bean
    @InboundChannelAdapter(channel = "stream", poller = @Poller(cron = "0/5 * * * * *"))
    public MessageSource<InputStream> ftpMessageSource(SftpRemoteFileTemplate sftpRemoteFileTemplate) {
        SftpStreamingMessageSource messageSource = new SftpStreamingMessageSource(sftpRemoteFileTemplate);
        messageSource.setRemoteDirectory(remoteDirectoryDownload);
        messageSource.setMaxFetchSize(1);
        return messageSource;
    }

    @ServiceActivator(inputChannel = "stream")
    @Bean
    public MessageHandler handle() {
        return new MessageHandler() {
            @Override
            public void handleMessage(Message<?> message) throws MessagingException {
                LOGGER.info("\n");
                InputStream is = (InputStream) message.getPayload();
                File compressed = gzipService.compress(is);
                LOGGER.info(compressed.getName());
                message.getHeaders().forEach((String key, Object value) -> {
                    LOGGER.info("key = {}, value = {}", key, value);
                });
                LOGGER.info("\n");
            }
        };
    }

}
