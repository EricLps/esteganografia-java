package com.example.steganography;

import java.awt.image.BufferedImage;

import com.example.steganography.exceptions.SteganographyException;

public class SteganographyUtils {

    private final ImageProcessor imageProcessor;
    private final DataConverter dataConverter;

    public SteganographyUtils(ImageProcessor imageProcessor, DataConverter dataConverter) {
        this.imageProcessor = imageProcessor;
        this.dataConverter = dataConverter;
    }

    public void embedData(String originalImagePath, String dataToHidePath, String zipTempPath, String outputImagePath)
            throws SteganographyException {
        System.out.println("Iniciando ocultação de dados...");

        //carrega a imagem original
        BufferedImage image = imageProcessor.loadImage(originalImagePath);
        System.out.println("Imagem original carregada: " + originalImagePath);

        // compacta em zip
        byte[] zipBytes = dataConverter.zipFolder(dataToHidePath, zipTempPath);
        System.out.println("Dados compactados em ZIP. Tamanho: " + zipBytes.length + " bytes.");

        // cabeçalho com o tamanho do zip
        byte[] headerBytes = dataConverter.generateHeader(zipBytes.length);
        System.out.println("Cabeçalho gerado. Tamanho: " + headerBytes.length + " bytes.");

        // concatenar cabeçalho e dados ZIP
        byte[] dataWithHeader = new byte[headerBytes.length + zipBytes.length];
        System.arraycopy(headerBytes, 0, dataWithHeader, 0, headerBytes.length);
        System.arraycopy(zipBytes, 0, dataWithHeader, headerBytes.length, zipBytes.length);

        // Converter os dados em bits
        boolean[] dataBits = dataConverter.bytesToBits(dataWithHeader);
        System.out.println("Dados (com cabeçalho) convertidos para " + dataBits.length + " bits.");

        // verificar a capacidade da imagem
        long imageCapacityBits = (long) image.getWidth() * image.getHeight() * 3; // os 3 canais RGB
        if (dataBits.length > imageCapacityBits) {
            throw new SteganographyException(
                    "A imagem é muito pequena para ocultar os dados. " +
                            "Bits necessários: " + dataBits.length +
                            ", Capacidade da imagem: " + imageCapacityBits
            );
        }
        System.out.println("Capacidade da imagem suficiente. Bits a ocultar: " + dataBits.length +
                        ", Capacidade disponível: " + imageCapacityBits);

        // ocultar os bits LSB da img
        int bitIndex = 0;
        outerLoop:
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = imageProcessor.getPixel(image, x, y);

                int alpha = (pixel >> 24) & 0xFF;
                int red = (pixel >> 16) & 0xFF;
                int green = (pixel >> 8) & 0xFF;
                int blue = pixel & 0xFF;

                // Ocultar BITS: 

                //vermelho
                if (bitIndex < dataBits.length) {
                    red = (red & 0xFE) | (dataBits[bitIndex] ? 1 : 0);
                    bitIndex++;
                } else 
                    break outerLoop;

                //verde
                if (bitIndex < dataBits.length) {
                    green = (green & 0xFE) | (dataBits[bitIndex] ? 1 : 0);
                    bitIndex++;
                } else 
                    break outerLoop;

                //azul
                if (bitIndex < dataBits.length) {
                    blue = (blue & 0xFE) | (dataBits[bitIndex] ? 1 : 0);
                    bitIndex++;
                } else 
                    break outerLoop;

                int newPixel = (alpha << 24) | (red << 16) | (green << 8) | blue;
                imageProcessor.setPixel(image, x, y, newPixel);
            }
        }
        System.out.println("Dados ocultados na imagem. Bits processados: " + bitIndex);

        //salva a imagem com os dados ocultos
        imageProcessor.saveImage(image, outputImagePath);
        System.out.println("Imagem com dados ocultos salva em: " + outputImagePath);
    }

    public void extractData(String stegoImagePath, String outputDirectory)
            throws SteganographyException {
        System.out.println("Iniciando extração de dados...");

        // carrega a imagem salva
        BufferedImage image = imageProcessor.loadImage(stegoImagePath);
        System.out.println("Imagem esteganografada carregada: " + stegoImagePath);

        // extrai os bits
        boolean[] headerBits = new boolean[dataConverter.getHeaderSizeInBits()];
        int bitCount = 0;
        extractLoop:
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = imageProcessor.getPixel(image, x, y);

                int red = (pixel >> 16) & 0xFF;
                int green = (pixel >> 8) & 0xFF;
                int blue = pixel & 0xFF;

                // vermelho
                if (bitCount < headerBits.length) {
                    headerBits[bitCount++] = (red & 1) == 1;
                } else break extractLoop;

                // verde
                if (bitCount < headerBits.length) {
                    headerBits[bitCount++] = (green & 1) == 1;
                } else break extractLoop;

                // azul
                if (bitCount < headerBits.length) {
                    headerBits[bitCount++] = (blue & 1) == 1;
                } else break extractLoop;
            }
        }

        if (bitCount < headerBits.length) {
            throw new SteganographyException("Não foi possível extrair o cabeçalho completo da imagem. Imagem muito pequena ou corrompida.");
        }
        System.out.println("Cabeçalho (bits) extraído.");

        // converter os bits para bytes
        byte[] headerBytes = dataConverter.bitsToBytes(headerBits);
        int zipSize = dataConverter.extractHeader(headerBytes);
        System.out.println("Tamanho do arquivo ZIP a ser extraído: " + zipSize + " bytes.");

        // calcular o total para extrair
        long totalBitsToExtract = (long) dataConverter.getHeaderSizeInBits() + (long) zipSize * 8;
        System.out.println("Total de bits a extrair: " + totalBitsToExtract);

        // extrair todos os dados ocultos
        boolean[] allDataBits = new boolean[(int) totalBitsToExtract];
        bitCount = 0;
        outerLoop:
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = imageProcessor.getPixel(image, x, y);

                int red = (pixel >> 16) & 0xFF;
                int green = (pixel >> 8) & 0xFF;
                int blue = pixel & 0xFF;

                // vermelho
                if (bitCount < allDataBits.length) {
                    allDataBits[bitCount++] = (red & 1) == 1;
                } else break outerLoop;

                // verde
                if (bitCount < allDataBits.length) {
                    allDataBits[bitCount++] = (green & 1) == 1;
                } else break outerLoop;

                // azul
                if (bitCount < allDataBits.length) {
                    allDataBits[bitCount++] = (blue & 1) == 1;
                } else break outerLoop;
            }
        }

        if (bitCount < totalBitsToExtract) {
            throw new SteganographyException("Não foi possível extrair todos os dados. Imagem muito pequena ou dados corrompidos.");
        }
        System.out.println("Todos os " + bitCount + " bits de dados (cabeçalho + ZIP) extraídos.");

        // converter a sequencia de bits de dados (menos cabeçalho)
        // pula o cabeçalho
        boolean[] zipDataBits = new boolean[zipSize * 8];
        System.arraycopy(allDataBits, dataConverter.getHeaderSizeInBits(), zipDataBits, 0, zipDataBits.length);

        byte[] extractedZipBytes = dataConverter.bitsToBytes(zipDataBits);
        System.out.println("Dados ZIP convertidos para bytes. Tamanho: " + extractedZipBytes.length + " bytes.");

        // descompactar ZIP
        dataConverter.unzipToFolder(extractedZipBytes, outputDirectory);
        System.out.println("Dados descompactados com sucesso para: " + outputDirectory);
    }
}