package com.example.demo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import org.springframework.stereotype.Service;

@Service
public class GzipService {

    public File compress(final InputStream is) {
        final String gzipFile = "src/main/resources/compressed.gz";
        try (final GZIPOutputStream gzipOS = new GZIPOutputStream(new FileOutputStream(gzipFile))) {
            write(is, gzipOS);
            gzipOS.finish();
            is.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new File(gzipFile);
    }

    private void write(final InputStream is, final OutputStream os) throws IOException {
        final byte[] buffer = new byte[1024];
        int len;
        while ((len = is.read(buffer)) != -1) {
            os.write(buffer, 0, len);
        }
    }
}
