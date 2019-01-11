package com.example.demo;

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
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.sftp.inbound.SftpStreamingMessageSource;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;
import org.springframework.integration.transformer.StreamTransformer;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

@Configuration
@ConditionalOnExpression(value = "${transformerEnabled}")
public class SftpConfigForTransformers {

    private static final Logger LOGGER = LoggerFactory.getLogger(SftpConfigForTransformers.class);
    private final String remoteDirectoryDownload;

    @Autowired
    public SftpConfigForTransformers(@Value(value = "${sftp.remoteDirectoryDownload}") final String remoteDirectoryDownload) {
        this.remoteDirectoryDownload = remoteDirectoryDownload;
    }

    @Bean
    @InboundChannelAdapter(channel = "stream", poller = @Poller(cron = "0/5 * * * * *"))
    public MessageSource<InputStream> ftpMessageSource(SftpRemoteFileTemplate sftpRemoteFileTemplate) {
        SftpStreamingMessageSource messageSource = new SftpStreamingMessageSource(sftpRemoteFileTemplate);
        messageSource.setRemoteDirectory(remoteDirectoryDownload);
        messageSource.setMaxFetchSize(1);
        return messageSource;
    }

    @Bean
    @Transformer(inputChannel = "stream", outputChannel = "data")
    public org.springframework.integration.transformer.Transformer transformer() {
//        return new StreamTransformer(); // transforms to byte[]
        return new StreamTransformer("UTF-8"); // transforms to String
    }

    @ServiceActivator(inputChannel = "data")
    @Bean
    public MessageHandler handle() {
        return new MessageHandler() {
            @Override
            public void handleMessage(Message<?> message) throws MessagingException {
                LOGGER.info("\n");
                LOGGER.info(message.getPayload().toString());
                message.getHeaders().forEach((String key, Object value) -> {
                    LOGGER.info("key = {}, value = {}", key, value);
                });
                LOGGER.info("\n");
            }
        };
    }

}
