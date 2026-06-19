/*
 * 版权所有 2026 Tweea。
 * 保留所有权利。
 */
package cn.tweea.filecrypt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;

import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.core.util.Separators;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import net.matrix.security.CryptoAlgorithm;
import net.matrix.security.CryptoMx;

import static net.matrix.data.convert.BinaryStringConverter.HEX;

public final class Main {
    private static final int KEY_SIZE = 16;

    private static final int BUFFER_SIZE = 8192;

    public static void main(String[] args)
        throws IOException {
        if (args.length < 2) {
            throw new IllegalArgumentException("缺少参数");
        }

        String mode = args[0];
        String fileName = args[1];

        if ("encrypt".equals(mode)) {
            encrypt(fileName);
        } else if ("decrypt".equals(mode)) {
            decrypt(fileName);
        } else {
            throw new IllegalArgumentException("参数 1：encrypt/decrypt");
        }
    }

    private static void encrypt(String plainFileName)
        throws IOException {
        File plainFile = new File(plainFileName);
        if (!plainFile.exists()) {
            throw new IllegalArgumentException("参数 2 文件不存在");
        }
        if (!plainFile.isFile()) {
            throw new IllegalArgumentException("参数 2 不是文件");
        }

        plainFileName = FilenameUtils.getName(plainFileName);
        File parentFile = plainFile.getParentFile();

        String cipherFileName = getCipherFileName(plainFile);
        File cipherFile = new File(parentFile, cipherFileName);
        writeCipherFile(plainFile, cipherFile);

        FileMeta fileMeta = new FileMeta();
        fileMeta.setPlainFileName(plainFileName);
        fileMeta.setCipherFileName(cipherFileName);
        File metaFile = new File(parentFile, plainFileName + ".json");
        newObjectMapper().writeValue(metaFile, fileMeta);
    }

    private static ObjectMapper newObjectMapper() {
        return new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT).setDefaultPrettyPrinter(
            new DefaultPrettyPrinter().withSeparators(PrettyPrinter.DEFAULT_SEPARATORS.withObjectFieldValueSpacing(Separators.Spacing.AFTER)));
    }

    private static String getCipherFileName(File plainFile)
        throws IOException {
        try (InputStream is = IOUtils.buffer(new FileInputStream(plainFile))) {
            return HEX.toString(CryptoMx.digest(is, CryptoMx.getMessageDigest(CryptoAlgorithm.Digest.SM3))) + ".dat";
        }
    }

    private static void writeCipherFile(File plainFile, File cipherFile)
        throws IOException {
        try (InputStream is = IOUtils.buffer(new FileInputStream(plainFile)); OutputStream os = IOUtils.buffer(new FileOutputStream(cipherFile))) {
            byte[] key = RandomUtils.nextBytes(KEY_SIZE);
            os.write(key);

            byte[] buffer = new byte[BUFFER_SIZE];
            int read;
            while (IOUtils.EOF != (read = is.read(buffer))) {
                for (int i = 0; i < read; ++i) {
                    buffer[i] ^= key[i % KEY_SIZE];
                }
                os.write(buffer, 0, read);
            }
        }
    }

    private static void decrypt(String metaFileName)
        throws IOException {
        File metaFile = new File(metaFileName);
        if (!metaFile.exists()) {
            throw new IllegalArgumentException("参数 2 文件不存在");
        }
        if (!metaFile.isFile()) {
            throw new IllegalArgumentException("参数 2 不是文件");
        }
        if (!"json".equals(FilenameUtils.getExtension(metaFileName))) {
            throw new IllegalArgumentException("参数 2 不是 JSON 文件");
        }

        File parentFile = metaFile.getParentFile();
        FileMeta fileMeta = newObjectMapper().readValue(metaFile, FileMeta.class);
        String plainFileName = fileMeta.getPlainFileName();
        String cipherFileName = fileMeta.getCipherFileName();
        File plainFile = new File(parentFile, plainFileName);
        File cipherFile = new File(parentFile, cipherFileName);
        writePlainFile(cipherFile, plainFile);
    }

    private static void writePlainFile(File cipherFile, File plainFile)
        throws IOException {
        try (InputStream is = IOUtils.buffer(new FileInputStream(cipherFile)); OutputStream os = IOUtils.buffer(new FileOutputStream(plainFile))) {
            byte[] key = new byte[KEY_SIZE];
            if (is.read(key) < KEY_SIZE) {
                throw new IllegalArgumentException("密文文件长度不足");
            }

            byte[] buffer = new byte[BUFFER_SIZE];
            int read;
            while (IOUtils.EOF != (read = is.read(buffer))) {
                for (int i = 0; i < read; ++i) {
                    buffer[i] ^= key[i % KEY_SIZE];
                }
                os.write(buffer, 0, read);
            }
        }
    }
}
