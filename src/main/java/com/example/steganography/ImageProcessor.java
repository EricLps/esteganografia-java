package com.example.steganography;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import com.example.steganography.exceptions.SteganographyException;

public class ImageProcessor {
    public BufferedImage loadImage(String imagePath) throws SteganographyException {
        Path imageFilePath = Paths.get(imagePath); // usar path para files.newInputStream
        File imageFile = imageFilePath.toFile(); // manter file para verificação de existência/tipo

        if (!imageFile.exists()) {
            throw new SteganographyException("Arquivo de imagem não encontrado: " + imagePath);
        }
        if (!imageFile.isFile()) {
            throw new SteganographyException("O caminho não aponta para um arquivo, é um diretório: " + imagePath);
        }
        if (!imageFile.canRead()) {
            throw new SteganographyException("Sem permissão de leitura para o arquivo: " + imagePath);
        }

        try (InputStream is = Files.newInputStream(imageFilePath)) {
            // se a imagem for null, significa que não foi possível ler como imagem.
            BufferedImage image = ImageIO.read(is);
            if (image == null) {
                throw new SteganographyException("O arquivo não pôde ser lido como uma imagem PNG válida. Formato não reconhecido ou arquivo corrompido: " + imagePath);
            }
            return image;
        } catch (IOException e) {
            throw new SteganographyException("Erro de IO ao carregar a imagem: " + imagePath, e);
        } catch (Exception e) {
            throw new SteganographyException("Erro inesperado ao carregar a imagem: " + imagePath, e);
        }
    }

    public void saveImage(BufferedImage image, String outputPath) throws SteganographyException {
        try {
            File outputFile = new File(outputPath);
            // garante que o diretório de saída exista
            File parentDir = outputFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs(); // cria diretórios pais se não existirem
            }
            ImageIO.write(image, "PNG", outputFile);
        } catch (IOException e) {
            throw new SteganographyException("Erro ao salvar a imagem em: " + outputPath, e);
        }
    }

    public int getPixel(BufferedImage image, int x, int y) {
        return image.getRGB(x, y);
    }

    public void setPixel(BufferedImage image, int x, int y, int rgb) {
        image.setRGB(x, y, rgb);
    }
}