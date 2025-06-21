package com.example.steganography;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.example.steganography.exceptions.SteganographyException;

public class DataConverter {

    private static final int HEADER_SIZE_BYTES = 4; // tamanho do cabeçalho em bytes

    public byte[] zipFolder(String sourcePath, String outputPath) throws SteganographyException {
        Path source = Paths.get(sourcePath);
        Path output = Paths.get(outputPath);

        if (!Files.exists(source)) {
            throw new SteganographyException("A pasta/arquivo a ser compactado não existe: " + sourcePath);
        }

        try (FileOutputStream fos = new FileOutputStream(output.toFile());
            ZipOutputStream zipOut = new ZipOutputStream(fos)) {

            if (Files.isRegularFile(source)) {
                addToZip(source, source.getFileName().toString(), zipOut);
            } else if (Files.isDirectory(source)) {
                Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        String entryName = source.relativize(dir).toString();
                        if (!entryName.isEmpty()) { // nao cria entrada para o próprio diretório raiz
                            zipOut.putNextEntry(new ZipEntry(entryName + "/"));
                            zipOut.closeEntry();
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        String entryName = source.relativize(file).toString();
                        addToZip(file, entryName, zipOut);
                        return FileVisitResult.CONTINUE;
                    }
                });
            } else {
                throw new SteganographyException("Caminho inválido: " + sourcePath + ". Deve ser um arquivo ou diretório.");
            }

        } catch (IOException e) {
            throw new SteganographyException("Erro ao compactar o arquivo/pasta em ZIP: " + sourcePath, e);
        }

        // depois de compactar, le o os bits em um array
        try {
            return Files.readAllBytes(output);
        } catch (IOException e) {
            throw new SteganographyException("Erro ao ler o arquivo ZIP gerado: " + outputPath, e);
        }
    }

    private void addToZip(Path file, String entryName, ZipOutputStream zipOut) throws IOException {
        ZipEntry zipEntry = new ZipEntry(entryName);
        zipOut.putNextEntry(zipEntry);
        Files.copy(file, zipOut);
        zipOut.closeEntry();
    }

    public void unzipToFolder(byte[] zipBytes, String outputDirectory) throws SteganographyException {
        File destDir = new File(outputDirectory);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        try (ByteArrayInputStream bais = new ByteArrayInputStream(zipBytes);
            ZipInputStream zipIn = new ZipInputStream(bais)) {

            ZipEntry entry = zipIn.getNextEntry();
            while (entry != null) {
                String filePath = outputDirectory + File.separator + entry.getName();
                if (!entry.isDirectory()) {
                    new File(filePath).getParentFile().mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(filePath)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zipIn.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                } else {
                    File dir = new File(filePath);
                    dir.mkdir();
                }
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
        } catch (IOException e) {
            throw new SteganographyException("Erro ao descompactar o arquivo ZIP.", e);
        }
    }

    /**
     * @param bytes array de bytes a ser convertido.
     * @return array de booleanos representando a sequência de bits.
     */
    public boolean[] bytesToBits(byte[] bytes) {
        boolean[] bits = new boolean[bytes.length * 8];
        for (int i = 0; i < bytes.length; i++) {
            for (int j = 0; j < 8; j++) {
                // extrai o bit j (0 a 7, da esquerda para a direita) do byte i
                // (byte >> (7 - j)) & 1 extrai o bit mais significativo primeiro
                bits[i * 8 + j] = ((bytes[i] >> (7 - j)) & 1) == 1;
            }
        }
        return bits;
    }

    //onverte uma sequência de bits em um array de bytes
    public byte[] bitsToBytes(boolean[] bits) throws SteganographyException {
        if (bits.length % 8 != 0) {
            throw new SteganographyException("A sequência de bits não tem um número de bits que é um múltiplo de 8.");
        }

        // coloca o bit na posição correta
        byte[] bytes = new byte[bits.length / 8];
        for (int i = 0; i < bytes.length; i++) {
            byte currentByte = 0;
            for (int j = 0; j < 8; j++) {
                if (bits[i * 8 + j]) {
                    currentByte |= (1 << (7 - j));
                }
            }
            bytes[i] = currentByte;
        }
        return bytes;
    }

    // gera um cabeçalho de 4 bytes com o tamanho dos dados
    public byte[] generateHeader(int dataSize) {
        //converter int para 4 bytes
        return ByteBuffer.allocate(HEADER_SIZE_BYTES).putInt(dataSize).array();
    }

    //extrai o tamanho dos dados do array de bytes que seria o cabecalho
    public int extractHeader(byte[] headerBytes) throws SteganographyException {
        if (headerBytes.length != HEADER_SIZE_BYTES) {
            throw new SteganographyException("O cabeçalho tem um tamanho inválido. Esperado " + HEADER_SIZE_BYTES + " bytes.");
        }
        return ByteBuffer.wrap(headerBytes).getInt();
    }

    //tamanho do cabecalho em bits
    public int getHeaderSizeInBits() {
        return HEADER_SIZE_BYTES * 8;
    }
}